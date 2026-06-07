package de.hablijack.greenhouse.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.AskAiRequest;
import de.hablijack.greenhouse.ai.api.dto.RelayDecision;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse.SensorAnalysis;
import de.hablijack.greenhouse.ai.llm.LlmService;
import de.hablijack.greenhouse.ai.rag.service.PromptEnrichmentService;
import de.hablijack.greenhouse.service.SensorService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AiService {

  private static final Logger LOG = LoggerFactory.getLogger(AiService.class);

  private final LlmService llmService;
  private final PromptEnrichmentService promptEnrichmentService;
  private final GreenhouseAnalyzer greenhouseAnalyzer;
  private final SensorHistoryService sensorHistoryService;
  private final SensorService sensorService;
  private final ObjectMapper objectMapper;

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public AiService(LlmService llmService,
      PromptEnrichmentService promptEnrichmentService,
      GreenhouseAnalyzer greenhouseAnalyzer,
      SensorHistoryService sensorHistoryService,
      SensorService sensorService,
      ObjectMapper objectMapper) {
    this.llmService = llmService;
    this.promptEnrichmentService = promptEnrichmentService;
    this.greenhouseAnalyzer = greenhouseAnalyzer;
    this.sensorHistoryService = sensorHistoryService;
    this.sensorService = sensorService;
    this.objectMapper = objectMapper;
  }

  public AiRecommendationResponse analyzeSensorData(SensorDataRequest sensorData) {
    LOG.info("Analyzing sensor data for plant: {}", sensorData.plantType);

    TimeContext time = TimeContext.from(sensorData.currentHour, sensorData.currentMonth);
    HistoryData history = sensorHistoryService.fetchHistory(time);

    AiRecommendationResponse localAnalysis = greenhouseAnalyzer.analyze(sensorData, history);

    try {
      String enrichedQuery = promptEnrichmentService.enrichPrompt(
          buildSensorAnalysisPrompt(sensorData, history, time), sensorData.plantType);

      String systemPrompt = promptEnrichmentService.buildSystemPrompt(
          sensorData.plantType, time.timeOfDayLabel(), time.seasonLabel());

      AiRecommendationResponse llmAnalysis = llmService.chatAsJson(
          systemPrompt, enrichedQuery, AiRecommendationResponse.class);

      return mergeAnalyses(localAnalysis, llmAnalysis, sensorData);
    } catch (Exception e) {
      LOG.warn("LLM analysis failed, falling back to local analysis: {}", e.getMessage());
      return localAnalysis;
    }
  }

  public String askQuestion(AskAiRequest request) {
    LOG.info("Processing AI question: {} (plant: {})", request.question, request.plantType);

    TimeContext time = TimeContext.now();
    String enrichedQuery = promptEnrichmentService.enrichPrompt(
        request.question, request.plantType);

    String systemPrompt = promptEnrichmentService.buildSystemPrompt(
        request.plantType, time.timeOfDayLabel(), time.seasonLabel());

    return llmService.chat(systemPrompt, enrichedQuery);
  }

  public Map<String, AiRecommendationResponse> analyzeBatch(List<SensorDataRequest> requests) {
    LOG.info("Analyzing {} plants in a single batch request", requests.size());

    Map<String, AiRecommendationResponse> localAnalyses = new HashMap<>();
    List<String> plantTypes = requests.stream()
        .map(r -> r.plantType)
        .collect(Collectors.toList());

    TimeContext time = TimeContext.from(
        requests.isEmpty() ? null : requests.get(0).currentHour,
        requests.isEmpty() ? null : requests.get(0).currentMonth);
    HistoryData history = sensorHistoryService.fetchHistory(time);

    for (SensorDataRequest req : requests) {
      localAnalyses.put(req.plantType, greenhouseAnalyzer.analyze(req, history));
    }

    try {
      String batchPrompt = buildBatchPrompt(requests, localAnalyses, history, time);
      String enrichedQuery = promptEnrichmentService.enrichBatchPrompt(batchPrompt, plantTypes);
      String systemPrompt = promptEnrichmentService.buildBatchSystemPrompt(
          plantTypes, time.timeOfDayLabel(), time.seasonLabel());

      String llmJson = llmService.chat(systemPrompt, enrichedQuery, true);
      Map<String, AiRecommendationResponse> llmResults = objectMapper.readValue(
          llmJson, new TypeReference<Map<String, AiRecommendationResponse>>() { });

      Map<String, AiRecommendationResponse> merged = new HashMap<>();
      for (SensorDataRequest req : requests) {
        AiRecommendationResponse llmResult = llmResults.get(req.plantType);
        if (llmResult != null) {
          llmResult.analysis = localAnalyses.get(req.plantType).analysis;
          merged.put(req.plantType, llmResult);
        } else {
          merged.put(req.plantType, localAnalyses.get(req.plantType));
        }
      }

      LOG.info("Batch analysis completed for {} plants", merged.size());
      return merged;
    } catch (Exception e) {
      LOG.warn("Batch LLM analysis failed, falling back to local analysis: {}", e.getMessage());
      return localAnalyses;
    }
  }

  public AiRecommendationResponse analyzeWithLocalOnly(SensorDataRequest sensorData) {
    TimeContext time = TimeContext.from(sensorData.currentHour, sensorData.currentMonth);
    HistoryData history = sensorHistoryService.fetchHistory(time);
    return greenhouseAnalyzer.analyze(sensorData, history);
  }

  @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
  public RelayDecision decideRelayStates() {
    LOG.info("Running AI shadow relay decision");

    TimeContext time = TimeContext.now();
    HistoryData history = sensorHistoryService.fetchHistory(time);
    var sensorValues = sensorService.getCurrentSensorValues();

    List<de.hablijack.greenhouse.entity.Relay> relays = de.hablijack.greenhouse.entity.Relay.listAll();
    StringBuilder relayStateBuilder = new StringBuilder();
    for (var relay : relays) {
      String targetInfo = relay.target != null ? " (" + relay.target + ")" : "";
      relayStateBuilder.append(String.format("  - %s%s: %s\n",
          relay.identifier, targetInfo, relay.value ? "EIN" : "AUS"));
    }

    StringBuilder sensorStateBuilder = new StringBuilder();
    for (var entry : sensorValues.entrySet()) {
      sensorStateBuilder.append(String.format("  - %s: %.1f\n", entry.getKey(), entry.getValue()));
    }

    String prompt = String.format("""
        Aktuelle Gewächshaus-Sensorwerte:
        %s
        Aktuelle Relay-Zustände:
        %s
        Zeitkontext: %s (Stunde %d), %s (Monat %d)
        
        Trends der letzten 24h:
        - Temperatur: %s, %+.1f°C Änderung, Spanne [%.1f-%.1f]°C
        - Luftfeuchte: %s, Spanne [%.1f-%.1f]%%
        - Bodenfeuchte: %s, Spanne [%.1f-%.1f]%%
        - Licht: %s, Spanne [%.1f-%.1f] lux
        - CO2: %s, Spanne [%.1f-%.1f] ppm
        
        Entscheide für jedes Relay, ob es EIN oder AUS sein soll.
        Berücksichtige Tageszeit, Jahreszeit, Trends und kombinierte Risiken.
        """,
        sensorStateBuilder.toString(),
        relayStateBuilder.toString(),
        time.timeOfDayLabel(), time.hourOfDay,
        time.seasonLabel(), time.month,
        history.temperature.direction.name().toLowerCase(),
        history.temperature.rateOfChange,
        history.temperature.min, history.temperature.max,
        history.humidity.direction.name().toLowerCase(),
        history.humidity.min, history.humidity.max,
        history.soilMoisture.direction.name().toLowerCase(),
        history.soilMoisture.min, history.soilMoisture.max,
        history.light.direction.name().toLowerCase(),
        history.light.min, history.light.max,
        history.co2.direction.name().toLowerCase(),
        history.co2.min, history.co2.max);

    String enrichedQuery = promptEnrichmentService.enrichPrompt(prompt, "general");
    String systemPrompt = promptEnrichmentService.buildSystemPrompt(
        "general", time.timeOfDayLabel(), time.seasonLabel());

    try {
      RelayDecision decision = llmService.chatAsJson(systemPrompt, enrichedQuery, RelayDecision.class);
      LOG.info("AI shadow decision: {} relay actions proposed", 
          decision.relays != null ? decision.relays.size() : 0);
      return decision;
    } catch (Exception e) {
      LOG.warn("LLM shadow decision failed: {}", e.getMessage());
      return new RelayDecision();
    }
  }

  @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
  private String buildBatchPrompt(List<SensorDataRequest> requests,
      Map<String, AiRecommendationResponse> localAnalyses,
      HistoryData history, TimeContext time) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("""
        Zeitkontext: %s, %s (Stunde %d, Monat %d)
        Sensor-Trends der letzten 24 Stunden:
        - Temperatur: Trend=%s, Min=%.1f°C, Max=%.1f°C, Änderung=%.1f°C/24h
        - Luftfeuchte: Trend=%s, Min=%.1f%%, Max=%.1f%%
        - Bodenfeuchte: Trend=%s, Min=%.1f%%, Max=%.1f%%
        - Licht: Trend=%s, Min=%.1f lux, Max=%.1f lux
        - CO2: Trend=%s, Min=%.1f ppm, Max=%.1f ppm

        Sensorwerte und lokale Analyse für mehrere Pflanzen:

        """,
        time.timeOfDayLabel(), time.seasonLabel(), time.hourOfDay, time.month,
        history.temperature.direction.name().toLowerCase(),
        history.temperature.min, history.temperature.max, history.temperature.rateOfChange,
        history.humidity.direction.name().toLowerCase(),
        history.humidity.min, history.humidity.max,
        history.soilMoisture.direction.name().toLowerCase(),
        history.soilMoisture.min, history.soilMoisture.max,
        history.light.direction.name().toLowerCase(),
        history.light.min, history.light.max,
        history.co2.direction.name().toLowerCase(),
        history.co2.min, history.co2.max));

    for (SensorDataRequest data : requests) {
      AiRecommendationResponse local = localAnalyses.get(data.plantType);
      sb.append(String.format("""
          === %s ===
          - Temperatur: %.1f°C (aktuell) | %.1f°C (Min) | %.1f°C (Max)
          - Luftfeuchtigkeit: %.1f%%
          - Bodenfeuchte: %.1f%%
          - Licht: %.1f lux
          - CO2: %.1f ppm
          - Lokale Analyse: %s
          - Dringlichkeit: %s

          """,
          data.plantType,
          data.temperature, history.temperature.min, history.temperature.max,
          data.humidity,
          data.soilMoisture,
          data.lightIntensity,
          data.co2Level,
          local != null ? local.summary : "Keine",
          local != null ? local.urgency : "unbekannt"));
    }

    sb.append("""
        Berücksichtige bei der Analyse die Tageszeit, Jahreszeit und die gemessenen Trends.
        Fasse für jede Pflanze die Analyse und Pflegeempfehlung in einem Satz auf Deutsch zusammen.
        Gib keine einzelnen Messwerte aus.
        """);
    return sb.toString();
  }

  @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
  private String buildSensorAnalysisPrompt(SensorDataRequest data,
      HistoryData history, TimeContext time) {
    return String.format("""
        Sensorwerte für %s:
        - Temperatur: %.1f°C
        - Luftfeuchtigkeit: %.1f%%
        - Bodenfeuchte: %.1f%%
        - Lichtstärke: %.1f lux
        - CO2: %.1f ppm

        Zeitkontext: %s (Stunde %d), %s (Monat %d)

        Trends der letzten 24h:
        - Temperatur: Trend=%s, %+.1f°C Änderung, Spanne [%.1f-%.1f]°C
        - Luftfeuchte: Trend=%s, Spanne [%.1f-%.1f]%%
        - Bodenfeuchte: Trend=%s, Spanne [%.1f-%.1f]%%
        - Licht: Trend=%s, Spanne [%.1f-%.1f] lux
        - CO2: Trend=%s, Spanne [%.1f-%.1f] ppm

        Berücksichtige bei der Analyse die Tageszeit und Jahreszeit.
        Fasse die Analyse und Pflegeempfehlung in einem Satz auf Deutsch zusammen.
        """,
        data.plantType,
        data.temperature,
        data.humidity,
        data.soilMoisture,
        data.lightIntensity,
        data.co2Level,
        time.timeOfDayLabel(), time.hourOfDay,
        time.seasonLabel(), time.month,
        history.temperature.direction.name().toLowerCase(),
        history.temperature.rateOfChange,
        history.temperature.min, history.temperature.max,
        history.humidity.direction.name().toLowerCase(),
        history.humidity.min, history.humidity.max,
        history.soilMoisture.direction.name().toLowerCase(),
        history.soilMoisture.min, history.soilMoisture.max,
        history.light.direction.name().toLowerCase(),
        history.light.min, history.light.max,
        history.co2.direction.name().toLowerCase(),
        history.co2.min, history.co2.max);
  }

  private AiRecommendationResponse mergeAnalyses(
      AiRecommendationResponse local,
      AiRecommendationResponse llm,
      SensorDataRequest sensorData) {
    if (llm.recommendations == null || llm.recommendations.isEmpty()) {
      llm.recommendations = local.recommendations;
    }
    if (llm.summary == null || llm.summary.isBlank()) {
      llm.summary = local.summary;
    }
    if (llm.urgency == null || llm.urgency.isBlank()) {
      llm.urgency = local.urgency;
    }
    if (llm.riskAssessment == null || llm.riskAssessment.isBlank()) {
      llm.riskAssessment = local.riskAssessment;
    }
    llm.plantType = sensorData.plantType;
    llm.analysis = local.analysis;
    return llm;
  }
}

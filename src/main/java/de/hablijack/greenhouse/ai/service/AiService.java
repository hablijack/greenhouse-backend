package de.hablijack.greenhouse.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.AskAiRequest;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import de.hablijack.greenhouse.ai.llm.LlmService;
import de.hablijack.greenhouse.ai.rag.service.PromptEnrichmentService;
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
  private final ObjectMapper objectMapper;

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public AiService(LlmService llmService,
      PromptEnrichmentService promptEnrichmentService,
      GreenhouseAnalyzer greenhouseAnalyzer,
      ObjectMapper objectMapper) {
    this.llmService = llmService;
    this.promptEnrichmentService = promptEnrichmentService;
    this.greenhouseAnalyzer = greenhouseAnalyzer;
    this.objectMapper = objectMapper;
  }

  public AiRecommendationResponse analyzeSensorData(SensorDataRequest sensorData) {
    LOG.info("Analyzing sensor data for plant: {}", sensorData.plantType);

    AiRecommendationResponse localAnalysis = greenhouseAnalyzer.analyze(sensorData);

    try {
      String enrichedQuery = promptEnrichmentService.enrichPrompt(
          buildSensorAnalysisPrompt(sensorData), sensorData.plantType);

      String systemPrompt = promptEnrichmentService.buildSystemPrompt(sensorData.plantType);

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

    String enrichedQuery = promptEnrichmentService.enrichPrompt(
        request.question, request.plantType);

    String systemPrompt = promptEnrichmentService.buildSystemPrompt(request.plantType);

    return llmService.chat(systemPrompt, enrichedQuery);
  }

  public Map<String, AiRecommendationResponse> analyzeBatch(List<SensorDataRequest> requests) {
    LOG.info("Analyzing {} plants in a single batch request", requests.size());

    Map<String, AiRecommendationResponse> localAnalyses = new HashMap<>();
    List<String> plantTypes = requests.stream()
        .map(r -> r.plantType)
        .collect(Collectors.toList());

    for (SensorDataRequest req : requests) {
      localAnalyses.put(req.plantType, greenhouseAnalyzer.analyze(req));
    }

    try {
      String batchPrompt = buildBatchPrompt(requests, localAnalyses);
      String enrichedQuery = promptEnrichmentService.enrichPrompt(batchPrompt, null);
      String systemPrompt = promptEnrichmentService.buildBatchSystemPrompt(plantTypes);

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
    return greenhouseAnalyzer.analyze(sensorData);
  }

  @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
  private String buildBatchPrompt(List<SensorDataRequest> requests,
      Map<String, AiRecommendationResponse> localAnalyses) {
    StringBuilder sb = new StringBuilder("Sensorwerte und lokale Analyse für mehrere Pflanzen:\n\n");
    for (SensorDataRequest data : requests) {
      AiRecommendationResponse local = localAnalyses.get(data.plantType);
      sb.append(String.format("""
          === %s ===
          - Temperatur: %.1f°C
          - Luftfeuchtigkeit: %.1f%%
          - Bodenfeuchte: %.1f%%
          - Licht: %.1f lux
          - CO2: %.1f ppm
          - Lokale Analyse: %s
          - Dringlichkeit: %s

          """,
          data.plantType,
          data.temperature,
          data.humidity,
          data.soilMoisture,
          data.lightIntensity,
          data.co2Level,
          local != null ? local.summary : "Keine",
          local != null ? local.urgency : "unbekannt"));
    }
    sb.append("Fasse für jede Pflanze die Analyse und Pflegeempfehlung in einem einzigen Satz auf Deutsch zusammen. Gib keine einzelnen Messwerte aus.");
    return sb.toString();
  }

  @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
  private String buildSensorAnalysisPrompt(SensorDataRequest data) {
    return String.format("""
    Sensor values for %s:
        - Temperature: %.1fC
    - Humidity: %.1f%%
        - Soil moisture: %.1f%%
        - Light intensity: %.1f lux
    - CO2 level: %.1f ppm

    Fasse die Analyse und Pflegeempfehlung in einem einzigen Satz auf Deutsch zusammen.
    Gib keine einzelnen Messwerte aus.
    """,
        data.plantType,
        data.temperature,
        data.humidity,
        data.soilMoisture,
        data.lightIntensity,
        data.co2Level);
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

package de.hablijack.greenhouse.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.AskAiRequest;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import de.hablijack.greenhouse.ai.llm.LlmService;
import de.hablijack.greenhouse.ai.rag.service.PromptEnrichmentService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
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

  public AiRecommendationResponse analyzeWithLocalOnly(SensorDataRequest sensorData) {
    return greenhouseAnalyzer.analyze(sensorData);
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

    Fasse die Analyse und Pflegeempfehlung in einem einzigen Satz auf Deutsch zusammen. Gib keine einzelnen Messwerte aus.
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

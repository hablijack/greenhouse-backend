package de.hablijack.greenhouse.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import de.hablijack.greenhouse.ai.config.AiConfig;
import de.hablijack.greenhouse.ai.llm.LlmService;
import de.hablijack.greenhouse.ai.rag.service.PromptEnrichmentService;
import de.hablijack.greenhouse.ai.rag.service.VectorSearchService;
import de.hablijack.greenhouse.ai.service.AiService;
import de.hablijack.greenhouse.ai.service.GreenhouseAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

  @Mock
  LlmService llmService;

  @Mock
  PromptEnrichmentService promptEnrichmentService;

  @Mock
  VectorSearchService vectorSearchService;

  @Mock
  AiConfig config;

  @Mock
  AiConfig.RagConfig ragConfig;

  private GreenhouseAnalyzer greenhouseAnalyzer;
  private AiService aiService;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    greenhouseAnalyzer = new GreenhouseAnalyzer();
    aiService = new AiService(llmService, promptEnrichmentService,
        greenhouseAnalyzer, objectMapper);
  }

  @Test
  void testAnalyzeSensorDataFallsBackToLocalWhenLlmFails() {
    SensorDataRequest request = new SensorDataRequest(
        "tomato", 25.0, 65.0, 55.0, 500.0, 800.0);

    when(promptEnrichmentService.enrichPrompt(anyString(), anyString()))
        .thenReturn("test enriched query");
    when(promptEnrichmentService.buildSystemPrompt(anyString()))
        .thenReturn("test system prompt");
    when(llmService.chatAsJson(anyString(), anyString(), eq(AiRecommendationResponse.class)))
        .thenThrow(new RuntimeException("LLM unavailable"));

    AiRecommendationResponse result = aiService.analyzeSensorData(request);
    assertNotNull(result);
    assertEquals("low", result.urgency);
    assertEquals("tomato", result.plantType);
  }

  @Test
  void testAnalyzeSensorDataWithLocalOnly() {
    SensorDataRequest request = new SensorDataRequest(
        "tomato", 35.0, 90.0, 55.0, 500.0, 800.0);

    AiRecommendationResponse result = aiService.analyzeWithLocalOnly(request);
    assertEquals("high", result.urgency);
    assertNotNull(result.recommendations);
    assertTrue(result.recommendations.size() > 0);
  }
}

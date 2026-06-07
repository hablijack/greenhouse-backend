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
import de.hablijack.greenhouse.ai.service.HistoryData;
import de.hablijack.greenhouse.ai.service.SensorHistoryService;
import de.hablijack.greenhouse.ai.service.SensorTrend;
import de.hablijack.greenhouse.ai.service.SensorTrend.TrendDirection;
import de.hablijack.greenhouse.ai.service.TimeContext;
import de.hablijack.greenhouse.service.SensorService;
import java.util.Map;
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
  SensorHistoryService sensorHistoryService;

  @Mock
  SensorService sensorService;

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

    TimeContext time = new TimeContext(14, 6);
    HistoryData mockHistory = new HistoryData(
        new SensorTrend(24, 20, 28, 24, 0.1, TrendDirection.STABLE, 100),
        new SensorTrend(65, 50, 70, 62, 0.2, TrendDirection.STABLE, 100),
        new SensorTrend(55, 40, 60, 52, -0.1, TrendDirection.STABLE, 100),
        new SensorTrend(500, 200, 800, 450, 10, TrendDirection.RISING, 100),
        new SensorTrend(800, 500, 1000, 750, 15, TrendDirection.STABLE, 100),
        time);

    when(sensorHistoryService.fetchHistory(any())).thenReturn(mockHistory);

    lenient().when(sensorService.getCurrentSensorValues()).thenReturn(Map.of(
        "air_temp_inside", 25.0, "air_humidity_inside", 65.0,
        "brightness", 500.0, "co2", 800.0,
        "soil_humidity_line1", 55.0));

    aiService = new AiService(llmService, promptEnrichmentService,
        greenhouseAnalyzer, sensorHistoryService, sensorService, objectMapper);
  }

  @Test
  void testAnalyzeSensorDataFallsBackToLocalWhenLlmFails() {
    SensorDataRequest request = new SensorDataRequest(
        "tomato", 25.0, 65.0, 55.0, 500.0, 800.0, 14, 6);

    when(promptEnrichmentService.enrichPrompt(anyString(), anyString()))
        .thenReturn("test enriched query");
    when(promptEnrichmentService.buildSystemPrompt(anyString(), anyString(), anyString()))
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
        "tomato", 35.0, 90.0, 55.0, 500.0, 800.0, 14, 6);

    AiRecommendationResponse result = aiService.analyzeWithLocalOnly(request);
    assertEquals("high", result.urgency);
    assertNotNull(result.recommendations);
    assertTrue(result.recommendations.size() > 0);
  }
}

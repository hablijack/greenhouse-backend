package de.hablijack.greenhouse.ai;

import static org.junit.jupiter.api.Assertions.*;

import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import de.hablijack.greenhouse.ai.service.GreenhouseAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GreenhouseAnalyzerTest {

  private GreenhouseAnalyzer analyzer;

  @BeforeEach
  void setUp() {
    analyzer = new GreenhouseAnalyzer();
  }

  @Test
  void testOptimalConditions() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 55.0, 500.0, 800.0);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("low", result.urgency);
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("optimal")));
  }

  @Test
  void testHighHumidityFungalRisk() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 25.0, 92.0, 60.0, 500.0, 800.0);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("high", result.urgency);
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("Pilz")
        || r.contains("Luftzirkulation")));
  }

  @Test
  void testOverwateringDetection() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 95.0, 500.0, 800.0);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("high", result.urgency);
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("Überwässerung")
        || r.contains("Gießen")));
  }

  @Test
  void testHighTemperature() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 35.0, 60.0, 55.0, 500.0, 800.0);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertTrue(result.urgency.equals("high") || result.urgency.equals("medium"));
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("Belüftung")
        || r.contains("Beschattung")
        || r.contains("Temperatur")));
  }

  @Test
  void testLowLight() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 55.0, 50.0, 800.0);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("Licht")
        || r.contains("Beleuchtung")
        || r.contains("helleren")));
  }

  @Test
  void testCucumberOptimalConditions() {
    SensorDataRequest data = new SensorDataRequest(
        "cucumber", 25.0, 70.0, 60.0, 600.0, 800.0);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("low", result.urgency);
  }

  @Test
  void testMultipleWarningsCombine() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 12.0, 90.0, 90.0, 50.0, 200.0);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("high", result.urgency);
    assertNotNull(result.summary);
    assertFalse(result.recommendations.isEmpty());
  }

  @Test
  void testAnalysisStructure() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 55.0, 500.0, 800.0);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertNotNull(result.summary);
    assertNotNull(result.recommendations);
    assertFalse(result.recommendations.isEmpty());
    assertNotNull(result.urgency);
    assertTrue(result.urgency.matches("low|medium|high"));
    assertNotNull(result.riskAssessment);
    assertNotNull(result.plantType);
    assertEquals("tomato", result.plantType);
    assertNotNull(result.analysis);
  }
}

package de.hablijack.greenhouse.ai;

import static org.junit.jupiter.api.Assertions.*;

import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import de.hablijack.greenhouse.ai.service.GreenhouseAnalyzer;
import de.hablijack.greenhouse.ai.service.HistoryData;
import de.hablijack.greenhouse.ai.service.SensorTrend;
import de.hablijack.greenhouse.ai.service.SensorTrend.TrendDirection;
import de.hablijack.greenhouse.ai.service.TimeContext;
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
        "tomato", 24.0, 65.0, 55.0, 500.0, 800.0, 14, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("low", result.urgency);
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("optimal")));
  }

  @Test
  void testHighHumidityFungalRisk() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 25.0, 92.0, 60.0, 500.0, 800.0, 14, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("high", result.urgency);
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("Pilz")
        || r.contains("Luftzirkulation")));
  }

  @Test
  void testOverwateringDetection() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 95.0, 500.0, 800.0, 14, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("high", result.urgency);
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("Überwässerung")
        || r.contains("Gießen")));
  }

  @Test
  void testHighTemperature() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 35.0, 60.0, 55.0, 500.0, 800.0, 14, 6);
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
        "tomato", 24.0, 65.0, 55.0, 50.0, 800.0, 14, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertTrue(result.recommendations.stream()
        .anyMatch(r -> r.contains("Licht")
        || r.contains("Beleuchtung")
        || r.contains("Zusatzbeleuchtung")));
  }

  @Test
  void testCucumberOptimalConditions() {
    SensorDataRequest data = new SensorDataRequest(
        "cucumber", 25.0, 70.0, 60.0, 600.0, 800.0, 14, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("low", result.urgency);
  }

  @Test
  void testMultipleWarningsCombine() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 12.0, 90.0, 90.0, 50.0, 200.0, 14, 7);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("high", result.urgency);
    assertNotNull(result.summary);
    assertFalse(result.recommendations.isEmpty());
  }

  @Test
  void testAnalysisStructure() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 55.0, 500.0, 800.0, 14, 6);
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

  @Test
  void testTimeOfDayInAnalysis() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 55.0, 500.0, 800.0, 14, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertNotNull(result.analysis.timeOfDay);
    assertNotNull(result.analysis.season);
    assertTrue(result.summary.contains("Mittag") || result.summary.contains("Sommer"));
  }

  @Test
  void test12CAtMorningIsSlightlyLow() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 12.0, 60.0, 50.0, 500.0, 800.0, 6, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    String tempStatus = result.analysis.temperatureStatus;
    assertTrue("slightly_low".equals(tempStatus) || "too_low".equals(tempStatus),
        "12°C at 6am should not be full 'too_low', was: " + tempStatus);
  }

  @Test
  void test12CAtNoonIsTooLow() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 12.0, 60.0, 50.0, 500.0, 800.0, 13, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("too_low", result.analysis.temperatureStatus,
        "12°C at 1pm should be critically too_low");
  }

  @Test
  void testNightTimeLightIsFlagged() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 20.0, 65.0, 55.0, 300.0, 800.0, 2, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertEquals("high", result.analysis.lightStatus,
        "Light at night should be flagged as too high");
  }

  @Test
  void testNightTimeTemperatureIsLenient() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 16.0, 65.0, 55.0, 0.0, 800.0, 2, 6);
    AiRecommendationResponse result = analyzer.analyze(data);
    assertNotEquals("too_low", result.analysis.temperatureStatus,
        "16°C at night should not be too_low");
  }

  @Test
  void testSeasonalTipInWinter() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 55.0, 500.0, 800.0, 14, 1);
    AiRecommendationResponse result = analyzer.analyze(data);
    boolean hasWinterTip = result.recommendations.stream()
        .anyMatch(r -> r.contains("Winter") || r.contains("Zusatzbeleuchtung"));
    assertTrue(hasWinterTip, "Should include winter tip in January");
  }

  @Test
  void testSeasonalTipInSummer() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 24.0, 65.0, 55.0, 500.0, 800.0, 14, 7);
    AiRecommendationResponse result = analyzer.analyze(data);
    boolean hasSummerTip = result.recommendations.stream()
        .anyMatch(r -> r.contains("Sommer") || r.contains("Beschattung"));
    assertTrue(hasSummerTip, "Should include summer tip in July");
  }

  @Test
  void testCombinedHeatAndDrySoilRisk() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 35.0, 50.0, 25.0, 800.0, 800.0, 14, 7);
    AiRecommendationResponse result = analyzer.analyze(data);
    boolean hasCombinedWarning = result.riskAssessment.contains("Hitzestress")
        || result.recommendations.stream().anyMatch(r -> r.contains("bewässern")
        && r.contains("Lüftung"));
    assertTrue(hasCombinedWarning,
        "Should detect combined heat + dry soil risk, was: " + result.riskAssessment);
  }

  @Test
  void testHistoryDataEnrichesAnalysis() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 28.0, 65.0, 55.0, 500.0, 800.0, 14, 6);
    TimeContext time = new TimeContext(14, 6);
    HistoryData history = new HistoryData(
        new SensorTrend(28, 20, 35, 27, 5.0, TrendDirection.RISING_FAST, 100),
        new SensorTrend(65, 50, 70, 60, 0.5, TrendDirection.STABLE, 100),
        new SensorTrend(55, 40, 60, 50, -0.2, TrendDirection.STABLE, 100),
        new SensorTrend(500, 100, 800, 400, 10, TrendDirection.RISING, 100),
        new SensorTrend(800, 400, 1000, 700, 20, TrendDirection.STABLE, 100),
        time);
    AiRecommendationResponse result = analyzer.analyze(data, history);
    assertNotNull(result.analysis.temperatureTrend);
    assertEquals("rising_fast", result.analysis.temperatureTrend);
  }

  @Test
  void testTrendWorseningBoostsUrgency() {
    SensorDataRequest data = new SensorDataRequest(
        "tomato", 32.0, 65.0, 55.0, 500.0, 800.0, 14, 6);
    TimeContext time = new TimeContext(14, 6);
    HistoryData worseningHistory = new HistoryData(
        new SensorTrend(32, 22, 32, 26, 6.0, TrendDirection.RISING_FAST, 100),
        new SensorTrend(65, 50, 70, 60, 0.5, TrendDirection.STABLE, 100),
        new SensorTrend(55, 40, 60, 50, -0.2, TrendDirection.STABLE, 100),
        new SensorTrend(500, 100, 800, 400, 10, TrendDirection.RISING, 100),
        new SensorTrend(800, 400, 1000, 700, 20, TrendDirection.STABLE, 100),
        time);
    HistoryData stableHistory = new HistoryData(
        new SensorTrend(32, 30, 32, 31, 0.1, TrendDirection.STABLE, 100),
        new SensorTrend(65, 60, 70, 65, 0.1, TrendDirection.STABLE, 100),
        new SensorTrend(55, 50, 60, 55, 0.1, TrendDirection.STABLE, 100),
        new SensorTrend(500, 400, 600, 500, 5, TrendDirection.STABLE, 100),
        new SensorTrend(800, 700, 900, 800, 10, TrendDirection.STABLE, 100),
        time);

    AiRecommendationResponse worsening = analyzer.analyze(data, worseningHistory);
    AiRecommendationResponse stable = analyzer.analyze(data, stableHistory);
    assertTrue(worsening.recommendations.stream()
            .anyMatch(r -> r.contains("schnell") || r.contains("Überhitzung")),
        "Rising fast trend should add warning about overheating");
  }
}

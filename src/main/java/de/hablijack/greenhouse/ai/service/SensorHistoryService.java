package de.hablijack.greenhouse.ai.service;

import de.hablijack.greenhouse.ai.service.SensorTrend.TrendDirection;
import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Sensor;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SensorHistoryService {

  private static final Logger LOG = LoggerFactory.getLogger(SensorHistoryService.class);
  private static final int HISTORY_HOURS = 24;
  private static final int SOIL_SENSOR_COUNT = 6;
  private static final String SOIL_HUMIDITY_PREFIX = "soil_humidity_line";
  private static final String AIR_TEMP_INSIDE = "air_temp_inside";
  private static final String AIR_HUMIDITY_INSIDE = "air_humidity_inside";
  private static final String BRIGHTNESS = "brightness";
  private static final String CO2 = "co2";

  public HistoryData fetchHistory(TimeContext timeContext) {
    LOG.debug("Fetching sensor history for last {} hours", HISTORY_HOURS);

    SensorTrend tempTrend = computeTrend(AIR_TEMP_INSIDE, HISTORY_HOURS);
    SensorTrend humidityTrend = computeTrend(AIR_HUMIDITY_INSIDE, HISTORY_HOURS);
    SensorTrend soilTrend = computeAverageSoilTrend(HISTORY_HOURS);
    SensorTrend lightTrend = computeTrend(BRIGHTNESS, HISTORY_HOURS);
    SensorTrend co2Trend = computeTrend(CO2, HISTORY_HOURS);

    return new HistoryData(tempTrend, humidityTrend, soilTrend, lightTrend, co2Trend, timeContext);
  }

  private SensorTrend computeTrend(String sensorIdentifier, int hours) {
    Sensor sensor = Sensor.findByIdentifier(sensorIdentifier);
    if (sensor == null) {
      LOG.warn("Sensor {} not found, returning empty trend", sensorIdentifier);
      return SensorTrend.empty(0);
    }

    List<Measurement> measurements = sensor.findMeasurementsWithinHours(hours);
    if (measurements.isEmpty()) {
      return SensorTrend.empty(0);
    }

    return aggregateMeasurements(measurements);
  }

  private SensorTrend computeAverageSoilTrend(int hours) {
    List<Measurement> allSoilMeasurements = new ArrayList<>();
    for (int i = 1; i <= SOIL_SENSOR_COUNT; i++) {
      Sensor sensor = Sensor.findByIdentifier(SOIL_HUMIDITY_PREFIX + i);
      if (sensor != null) {
        List<Measurement> ms = sensor.findMeasurementsWithinHours(hours);
        allSoilMeasurements.addAll(ms);
      }
    }

    if (allSoilMeasurements.isEmpty()) {
      return SensorTrend.empty(0);
    }

    return aggregateMeasurements(allSoilMeasurements);
  }

  private SensorTrend aggregateMeasurements(List<Measurement> measurements) {
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    double sum = 0;
    double current = measurements.get(measurements.size() - 1).value;

    for (Measurement m : measurements) {
      min = Math.min(min, m.value);
      max = Math.max(max, m.value);
      sum += m.value;
    }

    double average = sum / measurements.size();
    double rateOfChange = computeRateOfChange(measurements);
    TrendDirection direction = classifyTrend(rateOfChange);

    return new SensorTrend(current, min, max, average, rateOfChange, direction, measurements.size());
  }

  private double computeRateOfChange(List<Measurement> measurements) {
    if (measurements.size() < 10) {
      return 0;
    }

    int n = measurements.size();
    List<Measurement> firstHalf = measurements.subList(0, n / 2);
    List<Measurement> secondHalf = measurements.subList(n / 2, n);

    double firstAvg = firstHalf.stream().mapToDouble(m -> m.value).average().orElse(0);
    double secondAvg = secondHalf.stream().mapToDouble(m -> m.value).average().orElse(0);

    return secondAvg - firstAvg;
  }

  private TrendDirection classifyTrend(double rateOfChange) {
    if (rateOfChange > 3.0) return TrendDirection.RISING_FAST;
    if (rateOfChange > 0.5) return TrendDirection.RISING;
    if (rateOfChange < -3.0) return TrendDirection.FALLING_FAST;
    if (rateOfChange < -0.5) return TrendDirection.FALLING;
    return TrendDirection.STABLE;
  }
}

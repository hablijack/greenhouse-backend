package de.hablijack.greenhouse.ai.service;

import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse.SensorAnalysis;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class GreenhouseAnalyzer {

  private static final Logger LOG = LoggerFactory.getLogger(GreenhouseAnalyzer.class);

  private static final double TEMP_TOMATO_MIN = 18.0;
  private static final double TEMP_TOMATO_MAX = 30.0;
  private static final double TEMP_CUCUMBER_MIN = 20.0;
  private static final double TEMP_CUCUMBER_MAX = 28.0;

  private static final double HUMIDITY_IDEAL_MIN = 50.0;
  private static final double HUMIDITY_IDEAL_MAX = 75.0;
  private static final double HUMIDITY_FUNGAL_RISK = 85.0;

  private static final double SOIL_MOISTURE_IDEAL_MIN = 40.0;
  private static final double SOIL_MOISTURE_IDEAL_MAX = 75.0;
  private static final double SOIL_OVERWATERED = 85.0;

  private static final double LIGHT_IDEAL_MIN = 200.0;
  private static final double LIGHT_IDEAL_MAX = 1000.0;

  private static final double CO2_IDEAL_MIN = 400.0;
  private static final double CO2_IDEAL_MAX = 1200.0;

  private static final int SEVERITY_HIGH_THRESHOLD = 3;
  private static final int SEVERITY_CRITICAL_WEIGHT = 3;

  public AiRecommendationResponse analyze(SensorDataRequest data) {
    List<String> recommendations = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    int highestSeverity = 0;

    SensorAnalysis analysis = new SensorAnalysis();

    analysis.temperatureStatus = analyzeTemperature(data.temperature, data.plantType, recommendations, warnings);
    analysis.humidityStatus = analyzeHumidity(data.humidity, data.plantType, recommendations, warnings);
    analysis.soilMoistureStatus = analyzeSoilMoisture(data.soilMoisture, data.plantType, recommendations, warnings);
    analysis.lightStatus = analyzeLight(data.lightIntensity, data.plantType, recommendations, warnings);
    analysis.co2Status = analyzeCo2(data.co2Level, data.plantType, recommendations, warnings);

    highestSeverity = determineHighestSeverity(
        analysis.temperatureStatus,
        analysis.humidityStatus,
        analysis.soilMoistureStatus,
        analysis.lightStatus,
        analysis.co2Status
    );

    String urgency;
    if (highestSeverity >= SEVERITY_HIGH_THRESHOLD) {
      urgency = "high";
    } else if (highestSeverity >= 2) {
      urgency = "medium";
    } else {
      urgency = "low";
    }

    String summary = buildSummary(data, warnings, recommendations);

    if (recommendations.isEmpty()) {
      recommendations.add("All sensor values are within optimal ranges for " + data.plantType + ".");
    }

    return new AiRecommendationResponse(
        summary,
        recommendations,
        urgency,
        String.join("; ", warnings),
        data.plantType,
        analysis);
  }

  private int determineHighestSeverity(String... statuses) {
    int max = 0;
    for (String s : statuses) {
      int sev = switch (s) {
        case "critical_high" -> SEVERITY_CRITICAL_WEIGHT;
        case "too_high", "too_low", "high" -> 2;
        case "low" -> 1;
        default -> 0;
      };
      if (sev > max) {
        max = sev;
      }
    }
    return max;
  }

  @SuppressFBWarnings("IMPROPER_UNICODE")
  private String analyzeTemperature(double temp, String plantType,
      List<String> recommendations, List<String> warnings) {
    double min = plantType != null && plantType.equalsIgnoreCase("cucumber")
        ? TEMP_CUCUMBER_MIN : TEMP_TOMATO_MIN;
    double max = plantType != null && plantType.equalsIgnoreCase("cucumber")
        ? TEMP_CUCUMBER_MAX : TEMP_TOMATO_MAX;

    if (temp < min) {
      warnings.add("Temperature too low (" + temp + "C). Ideal range: " + min + "-" + max + "C.");
      recommendations.add("Increase greenhouse temperature to " + min + "-" + max + "C for " + plantType + ".");
      return "too_low";
    } else if (temp > max) {
      warnings.add("Temperature too high (" + temp + "C). Ideal range: " + min + "-" + max + "C.");
      recommendations.add("Increase ventilation or shade to reduce temperature for " + plantType + ".");
      return "too_high";
    }
    return "optimal";
  }

  private String analyzeHumidity(double humidity, String plantType,
      List<String> recommendations, List<String> warnings) {
    if (humidity > HUMIDITY_FUNGAL_RISK) {
      warnings.add("Critical: Humidity at " + humidity + "% - high fungal disease risk for " + plantType + ".");
      recommendations.add("Increase airflow and reduce humidity below 75% to prevent fungal diseases.");
      return "critical_high";
    } else if (humidity > HUMIDITY_IDEAL_MAX) {
      warnings.add("Humidity too high (" + humidity + "%). Elevates disease risk.");
      recommendations.add("Improve ventilation to lower humidity. Consider a dehumidifier.");
      return "high";
    } else if (humidity < HUMIDITY_IDEAL_MIN) {
      warnings.add("Humidity too low (" + humidity + "%). Ideal range: 50-75%.");
      recommendations.add("Increase humidity through misting or humidifier for optimal growth.");
      return "low";
    }
    return "optimal";
  }

  private String analyzeSoilMoisture(double moisture, String plantType,
      List<String> recommendations, List<String> warnings) {
    if (moisture > SOIL_OVERWATERED) {
      warnings.add("Critical: Overwatering detected (" + moisture + "% soil moisture). Root rot risk.");
      recommendations.add("Stop watering immediately. Improve drainage and let soil dry out.");
      return "critical_high";
    } else if (moisture > SOIL_MOISTURE_IDEAL_MAX) {
      warnings.add("Soil moisture elevated (" + moisture + "%). Risk of overwatering.");
      recommendations.add("Reduce watering frequency. Check drainage.");
      return "high";
    } else if (moisture < SOIL_MOISTURE_IDEAL_MIN) {
      warnings.add("Soil too dry (" + moisture + "%). Ideal range: 40-75%.");
      recommendations.add("Increase watering frequency for " + plantType + ".");
      return "low";
    }
    return "optimal";
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private String analyzeLight(double light, String plantType,
      List<String> recommendations, List<String> warnings) {
    if (light < LIGHT_IDEAL_MIN) {
      warnings.add("Light intensity too low (" + light + " lux). Plants need more light.");
      recommendations.add("Increase supplemental lighting or move plants to brighter location.");
      return "too_low";
    } else if (light > LIGHT_IDEAL_MAX) {
      warnings.add("Light intensity very high (" + light + " lux). Risk of leaf burn.");
      recommendations.add("Use shade cloth or move light-sensitive plants away from direct light.");
      return "too_high";
    }
    return "optimal";
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private String analyzeCo2(double co2, String plantType,
      List<String> recommendations, List<String> warnings) {
    if (co2 < CO2_IDEAL_MIN) {
      warnings.add("CO2 level low (" + co2 + " ppm). May limit photosynthesis.");
      recommendations.add("Improve greenhouse ventilation or consider CO2 supplementation.");
      return "low";
    } else if (co2 > CO2_IDEAL_MAX) {
      warnings.add("CO2 level elevated (" + co2 + " ppm). Ensure adequate ventilation.");
      recommendations.add("Increase air exchange rate to maintain healthy CO2 levels.");
      return "high";
    }
    return "optimal";
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private String buildSummary(SensorDataRequest data, List<String> warnings,
      List<String> recommendations) {
    StringBuilder summary = new StringBuilder();
    summary.append("Analysis for ").append(data.plantType).append(": ");

    if (warnings.isEmpty()) {
      summary.append("All conditions are optimal.");
    } else {
      summary.append(warnings.size()).append(" area(s) need attention.");
    }

    summary.append(" Temperature ").append(data.temperature).append("C,");
    summary.append(" humidity ").append(data.humidity).append("%,");
    summary.append(" soil moisture ").append(data.soilMoisture).append("%,");
    summary.append(" light ").append(data.lightIntensity).append(" lux,");
    summary.append(" CO2 ").append(data.co2Level).append(" ppm.");

    return summary.toString();
  }
}

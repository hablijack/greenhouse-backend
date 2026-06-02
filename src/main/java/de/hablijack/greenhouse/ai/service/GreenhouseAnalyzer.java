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
      recommendations.add("Alle Sensorwerte liegen im optimalen Bereich für " + data.plantType + ".");
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
      warnings.add("Temperatur zu niedrig (" + temp + "°C). Idealbereich: " + min + "-" + max + "°C.");
      recommendations.add("Erhöhe die Gewächshaustemperatur auf " + min + "-" + max + "°C für " + plantType + ".");
      return "too_low";
    } else if (temp > max) {
      warnings.add("Temperatur zu hoch (" + temp + "°C). Idealbereich: " + min + "-" + max + "°C.");
      recommendations.add("Erhöhe die Belüftung oder Beschattung, um die Temperatur für " + plantType + " zu senken.");
      return "too_high";
    }
    return "optimal";
  }

  private String analyzeHumidity(double humidity, String plantType,
      List<String> recommendations, List<String> warnings) {
    if (humidity > HUMIDITY_FUNGAL_RISK) {
      warnings.add("Kritisch: Luftfeuchtigkeit bei " + humidity + "% - hohes Pilzrisiko für " + plantType + ".");
      recommendations.add(
          "Erhöhe die Luftzirkulation und senke die Luftfeuchtigkeit unter 75%, um Pilzkrankheiten vorzubeugen.");
      return "critical_high";
    } else if (humidity > HUMIDITY_IDEAL_MAX) {
      warnings.add("Luftfeuchtigkeit zu hoch (" + humidity + "%). Erhöhtes Krankheitsrisiko.");
      recommendations.add("Verbessere die Belüftung, um die Luftfeuchtigkeit zu senken. Erwäge einen Luftentfeuchter.");
      return "high";
    } else if (humidity < HUMIDITY_IDEAL_MIN) {
      warnings.add("Luftfeuchtigkeit zu niedrig (" + humidity + "%). Idealbereich: 50-75%.");
      recommendations.add(
          "Erhöhe die Luftfeuchtigkeit durch Besprühen oder einen Luftbefeuchter für optimales Wachstum.");
      return "low";
    }
    return "optimal";
  }

  private String analyzeSoilMoisture(double moisture, String plantType,
      List<String> recommendations, List<String> warnings) {
    if (moisture > SOIL_OVERWATERED) {
      warnings.add("Kritisch: Überwässerung erkannt (" + moisture + "% Bodenfeuchte). Wurzelfäule-Risiko.");
      recommendations.add("Stelle das Gießen sofort ein. Verbessere die Drainage und lasse die Erde austrocknen.");
      return "critical_high";
    } else if (moisture > SOIL_MOISTURE_IDEAL_MAX) {
      warnings.add("Bodenfeuchte erhöht (" + moisture + "%). Risiko der Überwässerung.");
      recommendations.add("Reduziere die Bewässerungshäufigkeit. Überprüfe die Drainage.");
      return "high";
    } else if (moisture < SOIL_MOISTURE_IDEAL_MIN) {
      warnings.add("Boden zu trocken (" + moisture + "%). Idealbereich: 40-75%.");
      recommendations.add("Erhöhe die Bewässerungshäufigkeit für " + plantType + ".");
      return "low";
    }
    return "optimal";
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private String analyzeLight(double light, String plantType,
      List<String> recommendations, List<String> warnings) {
    if (light < LIGHT_IDEAL_MIN) {
      warnings.add("Lichtintensität zu niedrig (" + light + " Lux). Pflanzen benötigen mehr Licht.");
      recommendations.add("Erhöhe die Zusatzbeleuchtung oder stelle die Pflanzen an einen helleren Ort.");
      return "too_low";
    } else if (light > LIGHT_IDEAL_MAX) {
      warnings.add("Lichtintensität sehr hoch (" + light + " Lux). Risiko von Blattverbrennungen.");
      recommendations.add("Verwende Schattiergewebe oder stelle lichtempfindliche Pflanzen aus direktem Licht.");
      return "too_high";
    }
    return "optimal";
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private String analyzeCo2(double co2, String plantType,
      List<String> recommendations, List<String> warnings) {
    if (co2 < CO2_IDEAL_MIN) {
      warnings.add("CO2-Gehalt niedrig (" + co2 + " ppm). Kann die Photosynthese einschränken.");
      recommendations.add("Verbessere die Gewächshausbelüftung oder erwäge CO2-Anreicherung.");
      return "low";
    } else if (co2 > CO2_IDEAL_MAX) {
      warnings.add("CO2-Gehalt erhöht (" + co2 + " ppm). Stelle ausreichende Belüftung sicher.");
      recommendations.add("Erhöhe den Luftaustausch, um gesunde CO2-Werte zu erhalten.");
      return "high";
    }
    return "optimal";
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private String buildSummary(SensorDataRequest data, List<String> warnings,
      List<String> recommendations) {
    StringBuilder summary = new StringBuilder();
    summary.append("Analyse für ").append(data.plantType).append(": ");

    if (warnings.isEmpty()) {
      summary.append("Alle Bedingungen sind optimal.");
    } else {
      summary.append(warnings.size()).append(" Bereich(e) benötigen Aufmerksamkeit.");
    }

    summary.append(" Temperatur ").append(data.temperature).append("°C,");
    summary.append(" Luftfeuchtigkeit ").append(data.humidity).append("%,");
    summary.append(" Bodenfeuchte ").append(data.soilMoisture).append("%,");
    summary.append(" Licht ").append(data.lightIntensity).append(" Lux,");
    summary.append(" CO2 ").append(data.co2Level).append(" ppm.");

    return summary.toString();
  }
}

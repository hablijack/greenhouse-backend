package de.hablijack.greenhouse.ai.service;

import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse.SensorAnalysis;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import de.hablijack.greenhouse.ai.service.SensorTrend.TrendDirection;
import de.hablijack.greenhouse.ai.service.TimeContext.TimeOfDay;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class GreenhouseAnalyzer {

  private static final Logger LOG = LoggerFactory.getLogger(GreenhouseAnalyzer.class);

  private static final double TEMP_NIGHT_MIN = 14.0;
  private static final double TEMP_NIGHT_LOW = 10.0;
  private static final double TEMP_NIGHT_MAX = 22.0;
  private static final double TEMP_NIGHT_HIGH = 26.0;

  private static final double TEMP_MORNING_MIN = 15.0;
  private static final double TEMP_MORNING_LOW = 10.0;
  private static final double TEMP_MORNING_MAX = 25.0;
  private static final double TEMP_MORNING_HIGH = 30.0;

  private static final double TEMP_NOON_MIN = 20.0;
  private static final double TEMP_NOON_LOW = 16.0;
  private static final double TEMP_NOON_MAX = 30.0;
  private static final double TEMP_NOON_HIGH = 33.0;

  private static final double TEMP_EVENING_MIN = 18.0;
  private static final double TEMP_EVENING_LOW = 14.0;
  private static final double TEMP_EVENING_MAX = 26.0;
  private static final double TEMP_EVENING_HIGH = 30.0;

  private static final double TEMP_CUCUMBER_OFFSET_LOW = 2.0;
  private static final double TEMP_CUCUMBER_OFFSET_HIGH = -2.0;

  private static final double HUMIDITY_LOW = 40.0;
  private static final double HUMIDITY_IDEAL_MIN = 50.0;
  private static final double HUMIDITY_IDEAL_MAX = 75.0;
  private static final double HUMIDITY_FUNGAL_RISK = 85.0;

  private static final double SOIL_MOISTURE_LOW = 30.0;
  private static final double SOIL_MOISTURE_IDEAL_MIN = 40.0;
  private static final double SOIL_MOISTURE_IDEAL_MAX = 75.0;
  private static final double SOIL_OVERWATERED = 85.0;

  private static final double LIGHT_TOO_LOW = 100.0;
  private static final double LIGHT_IDEAL_MIN = 200.0;
  private static final double LIGHT_IDEAL_MAX = 1000.0;
  private static final double LIGHT_TOO_HIGH = 1500.0;

  private static final double CO2_LOW = 350.0;
  private static final double CO2_IDEAL_MIN = 400.0;
  private static final double CO2_IDEAL_MAX = 1200.0;
  private static final double CO2_HIGH = 1500.0;

  private static final int MAX_SEVERITY = 4;
  private static final int HIGH_SEVERITY_THRESHOLD = 3;
  private static final int MEDIUM_SEVERITY_THRESHOLD = 2;
  private static final int TEMP_TREND_MARGIN = 3;
  private static final double HUMIDITY_MARGIN = 5.0;
  private static final double NIGHT_LIGHT_THRESHOLD = 50.0;
  private static final double MARGIN_100 = 100.0;
  private static final double HIGH_TEMP_COMBINED = 28.0;
  private static final double HIGH_TEMP_NIGHT = 24.0;

  public AiRecommendationResponse analyze(SensorDataRequest data) {
    return analyze(data, null);
  }

  public AiRecommendationResponse analyze(SensorDataRequest data, HistoryData history) {
    List<String> recommendations = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    int highestSeverity = 0;

    TimeContext time = history != null ? history.time : TimeContext.from(data.currentHour, data.currentMonth);

    SensorAnalysis analysis = new SensorAnalysis();
    analysis.timeOfDay = time.timeOfDayLabel();
    analysis.season = time.seasonLabel();

    if (history != null) {
      analysis.temperatureTrend = history.temperature.direction.name().toLowerCase(Locale.ROOT);
      analysis.humidityTrend = history.humidity.direction.name().toLowerCase(Locale.ROOT);
      analysis.soilMoistureTrend = history.soilMoisture.direction.name().toLowerCase(Locale.ROOT);
      analysis.lightTrend = history.light.direction.name().toLowerCase(Locale.ROOT);
      analysis.co2Trend = history.co2.direction.name().toLowerCase(Locale.ROOT);
    }

    analyzeTemperature(data.temperature, data.plantType, time, history, recommendations, warnings, analysis);
    analyzeHumidity(data.humidity, data.plantType, time, history, recommendations, warnings, analysis);
    analyzeSoilMoisture(data.soilMoisture, data.plantType, time, history, recommendations, warnings, analysis);
    analyzeLight(data.lightIntensity, data.plantType, time, history, recommendations, warnings, analysis);
    analyzeCo2(data.co2Level, data.plantType, time, history, recommendations, warnings, analysis);

    analyzeCombinedRisks(data, time, history, recommendations, warnings);

    addSeasonalTips(data.plantType, time, recommendations, warnings);

    highestSeverity = determineHighestSeverity(
        analysis.temperatureStatus,
        analysis.humidityStatus,
        analysis.soilMoistureStatus,
        analysis.lightStatus,
        analysis.co2Status);

    int boost = countTrendWorsening(analysis, history);
    highestSeverity = Math.min(highestSeverity + boost, MAX_SEVERITY);

    String urgency;
    if (highestSeverity >= HIGH_SEVERITY_THRESHOLD) {
      urgency = "high";
    } else if (highestSeverity >= MEDIUM_SEVERITY_THRESHOLD) {
      urgency = "medium";
    } else {
      urgency = "low";
    }

    String riskAssessment = String.join("; ", warnings);
    String summary = buildSummary(data, time, warnings, recommendations);

    if (recommendations.isEmpty()) {
      recommendations.add("Alle Sensorwerte liegen im optimalen Bereich für " + data.plantType + ".");
    }
    if (warnings.isEmpty() && recommendations.size() == 1) {
      String firstTip = recommendations.get(0);
      recommendations.set(0, data.plantType + ": Alle Bedingungen sind optimal. " + firstTip);
    }

    return new AiRecommendationResponse(summary, recommendations, urgency, riskAssessment, data.plantType, analysis);
  }

  private int countTrendWorsening(SensorAnalysis analysis, HistoryData history) {
    if (history == null) {
      return 0;
    }
    int boost = 0;

    if ("too_high".equals(analysis.temperatureStatus)
        && history.temperature.direction == TrendDirection.RISING_FAST) {
      boost++;
    }
    if ("too_low".equals(analysis.temperatureStatus)
        && history.temperature.direction == TrendDirection.FALLING_FAST) {
      boost++;
    }
    if ("high".equals(analysis.humidityStatus)
        && history.humidity.direction == TrendDirection.RISING_FAST) {
      boost++;
    }

    return boost;
  }

  private int determineHighestSeverity(String... statuses) {
    int max = 0;
    for (String s : statuses) {
      int sev = switch (s) {
        case "critical_high" -> MAX_SEVERITY;
        case "too_high", "too_low" -> HIGH_SEVERITY_THRESHOLD;
        case "high", "low" -> MEDIUM_SEVERITY_THRESHOLD;
        case "slightly_high", "slightly_low" -> 1;
        default -> 0;
      };
      if (sev > max) {
        max = sev;
      }
    }
    return max;
  }

  @SuppressFBWarnings("IMPROPER_UNICODE")
  private void analyzeTemperature(double temp, String plantType, TimeContext time,
      HistoryData history, List<String> recommendations, List<String> warnings,
      SensorAnalysis analysis) {

    boolean isCucumber = plantType != null && plantType.toLowerCase(Locale.ROOT).equals("cucumber");
    double min;
    double lowThreshold;
    double max;
    double highThreshold;

    switch (time.timeOfDay) {
      case NIGHT, LATE_NIGHT -> {
        min = TEMP_NIGHT_MIN;
        lowThreshold = TEMP_NIGHT_LOW;
        max = TEMP_NIGHT_MAX;
        highThreshold = TEMP_NIGHT_HIGH;
      }
      case MORNING -> {
        min = TEMP_MORNING_MIN;
        lowThreshold = TEMP_MORNING_LOW;
        max = TEMP_MORNING_MAX;
        highThreshold = TEMP_MORNING_HIGH;
      }
      case NOON -> {
        min = TEMP_NOON_MIN;
        lowThreshold = TEMP_NOON_LOW;
        max = TEMP_NOON_MAX;
        highThreshold = TEMP_NOON_HIGH;
      }
      case EVENING -> {
        min = TEMP_EVENING_MIN;
        lowThreshold = TEMP_EVENING_LOW;
        max = TEMP_EVENING_MAX;
        highThreshold = TEMP_EVENING_HIGH;
      }
      default -> {
        LOG.warn("Unknown timeOfDay: {}, using NOON defaults", time.timeOfDay);
        min = TEMP_NOON_MIN;
        lowThreshold = TEMP_NOON_LOW;
        max = TEMP_NOON_MAX;
        highThreshold = TEMP_NOON_HIGH;
      }
    }

    if (isCucumber) {
      min += TEMP_CUCUMBER_OFFSET_LOW;
      lowThreshold += TEMP_CUCUMBER_OFFSET_LOW;
      max += TEMP_CUCUMBER_OFFSET_HIGH;
      highThreshold += TEMP_CUCUMBER_OFFSET_HIGH;
    }

    if (temp < lowThreshold) {
      warnings.add("Temperatur kritisch niedrig (" + temp + "°C um " + time.timeOfDayLabel()
          + "). Minimal: " + min + "°C.");
      recommendations.add("Sofort die Heizung erhöhen. Temperatur auf mindestens " + min + "°C bringen.");
      analysis.temperatureStatus = "too_low";
    } else if (temp < min) {
      warnings.add("Temperatur etwas niedrig (" + temp + "°C um " + time.timeOfDayLabel()
          + "). Ideal: " + min + "-" + max + "°C.");
      recommendations.add("Heizung leicht erhöhen, um " + min + "-" + max + "°C zu erreichen.");
      analysis.temperatureStatus = "slightly_low";
    } else if (temp > highThreshold) {
      warnings.add("Temperatur kritisch hoch (" + temp + "°C um " + time.timeOfDayLabel()
          + "). Maximal: " + max + "°C.");
      recommendations.add("Sofort Lüftung und Beschattung maximieren! Temperatur unter " + max + "°C bringen.");
      analysis.temperatureStatus = "too_high";
    } else if (temp > max) {
      warnings.add("Temperatur etwas hoch (" + temp + "°C um " + time.timeOfDayLabel()
          + "). Ideal: " + min + "-" + max + "°C.");
      recommendations.add("Lüftung oder Beschattung erhöhen.");
      analysis.temperatureStatus = "slightly_high";
    } else {
      analysis.temperatureStatus = "optimal";
    }

    if (history != null) {
      TrendDirection trend = history.temperature.direction;
      if (trend == TrendDirection.RISING_FAST && temp > max - TEMP_TREND_MARGIN) {
        warnings.add("Temperatur steigt schnell an (" + history.temperature.rateOfChange
            + "°C/24h). Überhitzung droht.");
        recommendations.add(
            "Temperatur steigt schnell an. Frühzeitig Lüftung öffnen, bevor die Temperatur kritisch wird.");
      } else if (trend == TrendDirection.FALLING_FAST && temp < min + TEMP_TREND_MARGIN) {
        warnings.add("Temperatur fällt schnell (" + history.temperature.rateOfChange
            + "°C/24h). Frostgefahr steigt.");
        recommendations.add("Temperatur fällt schnell. Heizung und Isolation überprüfen.");
      }
    }
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void analyzeHumidity(double humidity, String plantType, TimeContext time,
      HistoryData history, List<String> recommendations, List<String> warnings,
      SensorAnalysis analysis) {

    if (humidity > HUMIDITY_FUNGAL_RISK) {
      warnings.add("Kritisch: Luftfeuchtigkeit bei " + humidity + "% - hohes Pilzrisiko für " + plantType + ".");
      recommendations.add(
          "Sofort Luftzirkulation maximieren und Luftfeuchtigkeit unter 75% senken.");
      analysis.humidityStatus = "critical_high";
    } else if (humidity > HUMIDITY_IDEAL_MAX) {
      warnings.add("Luftfeuchtigkeit erhöht (" + humidity + "%). Erhöhtes Krankheitsrisiko.");
      recommendations.add("Belüftung verbessern. Luftentfeuchter erwägen.");
      analysis.humidityStatus = "high";
    } else if (humidity < HUMIDITY_LOW) {
      warnings.add("Luftfeuchtigkeit kritisch niedrig (" + humidity + "%). Hitzestress möglich.");
      recommendations.add("Sofort Luftbefeuchtung starten oder Pflanzen besprühen.");
      analysis.humidityStatus = "too_low";
    } else if (humidity < HUMIDITY_IDEAL_MIN) {
      warnings.add("Luftfeuchtigkeit etwas niedrig (" + humidity + "%). Ideal: 50-75%.");
      recommendations.add("Luftfeuchtigkeit durch Besprühen leicht erhöhen.");
      analysis.humidityStatus = "low";
    } else {
      analysis.humidityStatus = "optimal";
    }

    if (history != null && history.humidity.direction == TrendDirection.RISING_FAST
        && humidity > HUMIDITY_IDEAL_MAX - HUMIDITY_MARGIN) {
      warnings.add("Luftfeuchtigkeit steigt schnell. Pilzgefahr nimmt zu.");
      recommendations.add("Belüftung vorbeugend erhöhen.");
    }
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void analyzeSoilMoisture(double moisture, String plantType, TimeContext time,
      HistoryData history, List<String> recommendations, List<String> warnings,
      SensorAnalysis analysis) {

    if (moisture > SOIL_OVERWATERED) {
      warnings.add("Kritisch: Überwässerung (" + moisture + "%). Wurzelfäule-Risiko.");
      recommendations.add("Gießen sofort einstellen. Drainage verbessern, Erde trocknen lassen.");
      analysis.soilMoistureStatus = "critical_high";
    } else if (moisture > SOIL_MOISTURE_IDEAL_MAX) {
      warnings.add("Bodenfeuchte erhöht (" + moisture + "%). Überwässerungsrisiko.");
      recommendations.add("Bewässerung reduzieren. Drainage prüfen.");
      analysis.soilMoistureStatus = "high";
    } else if (moisture < SOIL_MOISTURE_LOW) {
      warnings.add("Boden kritisch trocken (" + moisture + "%). Pflanzen unter Trockenstress.");
      recommendations.add("Sofort bewässern! Trockenstress gefährdet die Pflanzen.");
      analysis.soilMoistureStatus = "too_low";
    } else if (moisture < SOIL_MOISTURE_IDEAL_MIN) {
      warnings.add("Bodenfeuchte etwas niedrig (" + moisture + "%). Ideal: 40-75%.");
      recommendations.add("Bewässerung leicht erhöhen.");
      analysis.soilMoistureStatus = "low";
    } else {
      analysis.soilMoistureStatus = "optimal";
    }

    if (history != null) {
      if (history.soilMoisture.direction == TrendDirection.FALLING_FAST
          && moisture < SOIL_MOISTURE_IDEAL_MIN) {
        warnings.add("Boden trocknet schnell aus. Bewässerungsintervall verkürzen.");
        recommendations.add("Bewässerungsfrequenz erhöhen - der Boden trocknet zu schnell.");
      }
      if (history.soilMoisture.direction == TrendDirection.RISING
          && moisture > SOIL_MOISTURE_IDEAL_MAX) {
        warnings.add("Bodenfeuchte steigt kontinuierlich. Staunässe-Risiko.");
        recommendations.add("Bewässerung reduzieren - Boden wird zunehmend nasser.");
      }
    }
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void analyzeLight(double light, String plantType, TimeContext time,
      HistoryData history, List<String> recommendations, List<String> warnings,
      SensorAnalysis analysis) {

    boolean isNight = time.timeOfDay == TimeOfDay.NIGHT || time.timeOfDay == TimeOfDay.LATE_NIGHT;

    if (isNight) {
      if (light > NIGHT_LIGHT_THRESHOLD) {
        warnings.add("Licht zur Nachtzeit (" + light + " lux). Stört den Pflanzenrhythmus.");
        recommendations.add("Pflanzenlicht und Fremdlichtquellen in der Nacht vermeiden.");
        analysis.lightStatus = "high";
      } else {
        analysis.lightStatus = "optimal";
      }
      return;
    }

    if (light < LIGHT_TOO_LOW) {
      warnings.add("Licht extrem niedrig (" + light + " lux). Starke Wachstumseinschränkung.");
      recommendations.add("Zusatzbeleuchtung sofort einschalten.");
      analysis.lightStatus = "too_low";
    } else if (light < LIGHT_IDEAL_MIN) {
      warnings.add("Lichtintensität niedrig (" + light + " lux). Ideal ab 200 lux.");
      recommendations.add("Zusatzbeleuchtung prüfen oder Pflanzen umstellen.");
      analysis.lightStatus = "low";
    } else if (light > LIGHT_TOO_HIGH) {
      warnings.add("Lichtintensität sehr hoch (" + light + " lux). Blattverbrennungs-Risiko.");
      recommendations.add("Schattiergewebe verwenden oder lichtempfindliche Pflanzen schützen.");
      analysis.lightStatus = "high";
    } else {
      analysis.lightStatus = "optimal";
    }

    if (history != null && history.light.direction == TrendDirection.FALLING_FAST
        && light < LIGHT_IDEAL_MIN + MARGIN_100) {
      warnings.add("Licht nimmt schnell ab. Zusatzbeleuchtung vorbereiten.");
      recommendations.add("Zusatzbeleuchtung rechtzeitig aktivieren.");
    }
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void analyzeCo2(double co2, String plantType, TimeContext time,
      HistoryData history, List<String> recommendations, List<String> warnings,
      SensorAnalysis analysis) {

    if (co2 < CO2_LOW) {
      warnings.add("CO2 kritisch niedrig (" + co2 + " ppm). Photosynthese eingeschränkt.");
      recommendations.add("CO2-Anreicherung starten oder Lüftung vorübergehend reduzieren.");
      analysis.co2Status = "too_low";
    } else if (co2 < CO2_IDEAL_MIN) {
      warnings.add("CO2-Gehalt niedrig (" + co2 + " ppm). Kann die Photosynthese einschränken.");
      recommendations.add("CO2-Anreicherung erwägen.");
      analysis.co2Status = "low";
    } else if (co2 > CO2_HIGH) {
      warnings.add("CO2-Gehalt stark erhöht (" + co2 + " ppm). Lüftung unzureichend.");
      recommendations.add("Lüftung sofort verstärken, um CO2 zu senken.");
      analysis.co2Status = "too_high";
    } else if (co2 > CO2_IDEAL_MAX) {
      warnings.add("CO2-Gehalt erhöht (" + co2 + " ppm). Lüftung prüfen.");
      recommendations.add("Luftaustausch erhöhen.");
      analysis.co2Status = "high";
    } else {
      analysis.co2Status = "optimal";
    }

    if (history != null && history.co2.direction == TrendDirection.RISING_FAST
        && co2 > CO2_IDEAL_MAX - MARGIN_100) {
      warnings.add("CO2 steigt schnell an. Belüftungssystem prüfen.");
      recommendations.add("Lüftung vorbeugend öffnen.");
    }
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void analyzeCombinedRisks(SensorDataRequest data, TimeContext time,
      HistoryData history, List<String> recommendations, List<String> warnings) {

    boolean highTemp = data.temperature > HIGH_TEMP_COMBINED;
    boolean lowHumidity = data.humidity < HUMIDITY_IDEAL_MIN;
    boolean highHumidity = data.humidity > HUMIDITY_IDEAL_MAX;
    boolean drySoil = data.soilMoisture < SOIL_MOISTURE_IDEAL_MIN;
    boolean wetSoil = data.soilMoisture > SOIL_MOISTURE_IDEAL_MAX;
    boolean lowLight = data.lightIntensity < LIGHT_IDEAL_MIN;
    boolean highTempNight = data.temperature > HIGH_TEMP_NIGHT
        && (time.timeOfDay == TimeOfDay.NIGHT || time.timeOfDay == TimeOfDay.LATE_NIGHT);

    if (highTemp && drySoil) {
      warnings.add("Kombiniertes Risiko: Hohe Temperatur + trockener Boden = akuter Hitzestress.");
      recommendations.add("Dringend bewässern UND Lüftung/Beschattung erhöhen!");
    }

    if (highTemp && lowHumidity) {
      warnings.add("Hohes VPD-Risiko: Temperatur hoch + Luftfeuchte niedrig = starker Transpirationsstress.");
      recommendations.add("Luftfeuchtigkeit erhöhen (Besprühen) und Temperatur senken.");
    }

    if (lowHumidity && drySoil) {
      warnings.add("Trockenstress: Boden und Luft sind trocken.");
      recommendations.add("Bewässern und Luftfeuchte erhöhen.");
    }

    if (highHumidity && lowLight) {
      warnings.add("Krankheitsrisiko: Hohe Feuchte + wenig Licht = Pilzgefahr.");
      recommendations.add("Belüftung maximieren und Zusatzbeleuchtung einschalten.");
    }

    if (highTempNight) {
      warnings.add("Nächtliche Überhitzung: Pflanzen können sich nicht erholen.");
      recommendations.add("Nachts stärker lüften, um die Temperatur zu senken.");
    }

    if (highHumidity && wetSoil) {
      warnings.add("Kombiniertes Risiko: Staunässe + hohe Luftfeuchte = Wurzelfäule- und Pilzgefahr.");
      recommendations.add("Gießen sofort stoppen, Drainage verbessern, Belüftung maximieren.");
    }
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void addSeasonalTips(String plantType, TimeContext time,
      List<String> recommendations, List<String> warnings) {
    String tip = switch (time.season) {
      case WINTER -> {
        if (recommendations.stream().anyMatch(r -> r.contains("Temperatur") || r.contains("Heizung"))) {
          yield "Winter-Tipp: Achte auf ausreichende Isolierung und vermeide kalte Zugluft.";
        }
        yield "Winter-Tipp: Nutze Zusatzbeleuchtung für ausreichende Tageslichtstunden.";
      }
      case SPRING -> {
        yield "Frühling-Tipp: Achte auf plötzliche Temperaturschwankungen zwischen Tag und Nacht.";
      }
      case SUMMER -> {
        yield "Sommer-Tipp: Maximale Beschattung und Belüftung nutzen. "
            + "CO2-Anreicherung am Morgen für bessere Photosynthese bei hohem Licht.";
      }
      case AUTUMN -> {
        yield "Herbst-Tipp: Reduziere Bewässerung bei sinkenden Temperaturen.";
      }
    };
    recommendations.add(tip);
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private String buildSummary(SensorDataRequest data, TimeContext time,
      List<String> warnings, List<String> recommendations) {
    StringBuilder sb = new StringBuilder();
    sb.append(data.plantType).append(" (").append(time.timeOfDayLabel())
        .append(", ").append(time.seasonLabel()).append("): ");

    if (warnings.isEmpty()) {
      sb.append("Alle Bedingungen sind optimal.");
    } else {
      int issueCount = warnings.size();
      String severity = issueCount <= 1 ? "Bereich" : "Bereiche";
      sb.append(issueCount).append(" ").append(severity).append(" benötigen Aufmerksamkeit.");
    }

    return sb.toString();
  }
}

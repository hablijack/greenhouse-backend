package de.hablijack.greenhouse.ai.service;

import de.hablijack.greenhouse.ai.api.dto.RelayDecision.RelayAction;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.service.SensorService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SafetyValidator {

  private static final Logger LOG = LoggerFactory.getLogger(SafetyValidator.class);

  private static final int MAX_IRRIGATIONS_PER_DAY = 3;
  private static final long MAX_IRRIGATION_ON_MS = 60_000;
  private static final long IRRIGATION_COOLDOWN_MS = 4 * 3600_000;
  private static final long MAX_FAN_CONTINUOUS_MS = 4 * 3600_000;
  private static final long FAN_COOLDOWN_MS = 300_000;
  private static final double MIN_BRIGHTNESS_FOR_FAN = 2500;
  private static final int FAN_ACTIVE_START_HOUR = 8;
  private static final int FAN_ACTIVE_END_HOUR = 17;
  private static final double ABSOLUTE_MAX_TEMP = 40.0;
  private static final double ABSOLUTE_MIN_TEMP = 5.0;
  private static final double ABSOLUTE_MAX_HUMIDITY = 95.0;
  private static final double ABSOLUTE_MIN_HUMIDITY = 20.0;
  private static final double ABSOLUTE_MAX_CO2 = 2000.0;
  private static final double ABSOLUTE_MAX_SOIL = 95.0;
  private static final double ABSOLUTE_MIN_SOIL = 15.0;

  private static final double DEFAULT_TEMPERATURE = 25.0;
  private static final double DEFAULT_HUMIDITY = 60.0;
  private static final double DEFAULT_BRIGHTNESS = 500.0;
  private static final double DEFAULT_CO2 = 800.0;
  private static final int RECENT_LOG_LIMIT = 100;
  private static final long MS_PER_HOUR = 3600_000;
  private static final double HIGH_TEMP_EMERGENCY = 38.0;
  private static final int LIGHT_OFF_NIGHT_START = 6;
  private static final int LIGHT_OFF_NIGHT_END = 20;
  private static final int SOIL_SENSOR_COUNT = 6;
  private static final double MIN_BRIGHTNESS_FOR_LIGHT_ON = 100.0;

  private final SensorService sensorService;

  public SafetyValidator(SensorService sensorService) {
    this.sensorService = sensorService;
  }

  public ValidationResult validate(List<RelayAction> decisions) {
    List<RelayAction> approved = new ArrayList<>();
    List<String> blocks = new ArrayList<>();
    List<String> overrides = new ArrayList<>();

    var sensorValues = sensorService.getCurrentSensorValues();
    double temperature = sensorValues.getOrDefault("air_temp_inside", DEFAULT_TEMPERATURE);
    double humidity = sensorValues.getOrDefault("air_humidity_inside", DEFAULT_HUMIDITY);
    double brightness = sensorValues.getOrDefault("brightness", DEFAULT_BRIGHTNESS);
    double co2 = sensorValues.getOrDefault("co2", DEFAULT_CO2);
    int hour = LocalTime.now().getHour();

    for (RelayAction action : decisions) {
      String relayId = action.relayId;
      Relay relay = Relay.findByIdentifier(relayId);
      if (relay == null) {
        blocks.add(relayId + ": Relay existiert nicht");
        continue;
      }

      if (RelayLog.isLastActionManualActivated(relay)) {
        blocks.add(relayId + " (" + relay.name + "): Manuell gesteuert – Überspringen");
        continue;
      }

      if (relayId.startsWith("relay_line") && !relayId.equals("relay_line7")
          && !relayId.equals("relay_line8")) {
        String result = validateIrrigation(action, relay, sensorValues);
        if (result == null) {
          approved.add(action);
        } else {
          blocks.add(result);
        }
      } else if (relayId.equals("relay_line8")) {
        String result = validateFan(action, relay, temperature, humidity, brightness, hour);
        if (result == null) {
          approved.add(action);
        } else {
          blocks.add(result);
        }
      } else if (relayId.equals("relay_line7")) {
        String result = validateLight(action, relay, brightness, hour);
        if (result == null) {
          approved.add(action);
        } else {
          blocks.add(result);
        }
      } else {
        approved.add(action);
      }
    }

    if (temperature > ABSOLUTE_MAX_TEMP) {
      overrides.add("NOTFALL: Temperatur " + temperature + "°C > " + ABSOLUTE_MAX_TEMP
          + "°C – Ventilatoren EIN erzwungen");
      if (decisions.stream().noneMatch(d -> d.relayId.equals("relay_line8") && d.desiredState)) {
        approved.add(new RelayAction("relay_line8", true,
            "Safety-Override: Temperatur > " + ABSOLUTE_MAX_TEMP + "°C"));
      }
    }
    if (temperature < ABSOLUTE_MIN_TEMP) {
      overrides.add("NOTFALL: Temperatur " + temperature + "°C < " + ABSOLUTE_MIN_TEMP
          + "°C – Heizung erforderlich");
    }
    if (humidity > ABSOLUTE_MAX_HUMIDITY) {
      overrides.add("NOTFALL: Luftfeuchte " + humidity + "% > " + ABSOLUTE_MAX_HUMIDITY
          + "% – Ventilatoren EIN erzwungen");
    }
    if (co2 > ABSOLUTE_MAX_CO2) {
      overrides.add("NOTFALL: CO2 " + co2 + "ppm > " + ABSOLUTE_MAX_CO2 + "ppm – Lüftung erforderlich");
    }

    return new ValidationResult(approved, blocks, overrides);
  }

  private String validateIrrigation(RelayAction action, Relay relay,
      java.util.Map<String, Double> sensorValues) {
    if (!action.desiredState) {
      return null;
    }

    double soilMoisture = 0;
    for (int i = 1; i <= SOIL_SENSOR_COUNT; i++) {
      String key = "soil_humidity_line" + i;
      if (sensorValues.containsKey(key)) {
        soilMoisture = Math.max(soilMoisture, sensorValues.get(key));
      }
    }
    if (soilMoisture > ABSOLUTE_MAX_SOIL) {
      return relay.identifier + " (" + relay.name + "): Boden bereits > "
          + ABSOLUTE_MAX_SOIL + "% – blockiert";
    }

    List<RelayLog> recentLogs = RelayLog.getRecentLog(RECENT_LOG_LIMIT);
    long todayCount = recentLogs.stream()
        .filter(log -> log.relay.identifier.equals(relay.identifier))
        .filter(log -> log.value)
        .filter(log -> {
          var cal = java.util.Calendar.getInstance();
          cal.setTime(log.timestamp);
          var now = java.util.Calendar.getInstance();
          return cal.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
              && cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR);
        })
        .count();

    if (todayCount >= MAX_IRRIGATIONS_PER_DAY) {
      return relay.identifier + " (" + relay.name + "): Bereits " + todayCount
          + "x heute bewässert (max " + MAX_IRRIGATIONS_PER_DAY + ") – blockiert";
    }

    if (RelayLog.isRelayOnTooLong(relay, MAX_IRRIGATION_ON_MS)) {
      return relay.identifier + " (" + relay.name + "): Läuft bereits >60s – blockiert";
    }

    var lastLog = recentLogs.stream()
        .filter(log -> log.relay.identifier.equals(relay.identifier))
        .filter(log -> log.value)
        .findFirst();
    if (lastLog.isPresent()) {
      long elapsed = System.currentTimeMillis() - lastLog.get().timestamp.getTime();
      if (elapsed < IRRIGATION_COOLDOWN_MS) {
        return relay.identifier + " (" + relay.name + "): Cooldown von "
            + (IRRIGATION_COOLDOWN_MS / MS_PER_HOUR) + "h noch nicht abgelaufen – blockiert";
      }
    }

    return null;
  }

  private String validateFan(RelayAction action, Relay relay,
      double temperature, double humidity, double brightness, int hour) {
    if (!action.desiredState) {
      return null;
    }

    if (RelayLog.isRelayOnTooLong(relay, MAX_FAN_CONTINUOUS_MS)) {
      return relay.identifier + " (" + relay.name + "): Läuft bereits >4h – Cooldown erzwungen";
    }

    boolean hasSolarPower = brightness >= MIN_BRIGHTNESS_FOR_FAN;
    boolean withinTimeWindow = hour >= FAN_ACTIVE_START_HOUR && hour <= FAN_ACTIVE_END_HOUR;
    boolean isEmergency = temperature > HIGH_TEMP_EMERGENCY || humidity > ABSOLUTE_MAX_HUMIDITY;

    if (!isEmergency && !hasSolarPower) {
      return relay.identifier + " (" + relay.name + "): Helligkeit " + (int) brightness
          + " lux < " + (int) MIN_BRIGHTNESS_FOR_FAN + " lux (kein Solarstrom) – blockiert";
    }

    if (!isEmergency && !withinTimeWindow) {
      return relay.identifier + " (" + relay.name + "): Ausserhalb Fenster "
          + FAN_ACTIVE_START_HOUR + "-" + FAN_ACTIVE_END_HOUR + " Uhr – blockiert";
    }

    return null;
  }

  private String validateLight(RelayAction action, Relay relay, double brightness, int hour) {
    if (action.desiredState && (hour < LIGHT_OFF_NIGHT_START || hour > LIGHT_OFF_NIGHT_END)) {
      return relay.identifier + " (" + relay.name + "): Licht zur Nachtzeit ("
          + hour + " Uhr) – blockiert";
    }
    if (!action.desiredState && brightness < MIN_BRIGHTNESS_FOR_LIGHT_ON
        && hour >= LIGHT_OFF_NIGHT_START && hour <= LIGHT_OFF_NIGHT_END) {
      return relay.identifier + " (" + relay.name + "): Kaum Tageslicht ("
          + (int) brightness + " lux) tagsüber – Licht EIN erzwungen";
    }
    return null;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public record ValidationResult(
      List<RelayAction> approved,
      List<String> blocked,
      List<String> overrides) {

    public boolean hasIssues() {
      return !blocked.isEmpty() || !overrides.isEmpty();
    }

    public String formatTelegramMessage() {
      return formatTelegramMessage(null);
    }

    public String formatTelegramMessage(List<RelayPulseService.PulsePlan> pulsePlans) {
      StringBuilder sb = new StringBuilder();
      sb.append("🤖 *AI-Steuerung (Shadow Mode)*\n");
      sb.append("━━━━━━━━━━━━━━━━━\n");

      if (!approved.isEmpty()) {
        sb.append("\n*✅ Genehmigt:*\n");
        for (RelayAction a : approved) {
          String pulseInfo = "";
          if (pulsePlans != null) {
            pulseInfo = pulsePlans.stream()
                .filter(p -> p.action().relayId.equals(a.relayId) && p.action().desiredState)
                .findFirst()
                .map(p -> " (Puls: " + p.durationLabel() + ")")
                .orElse("");
          }
          sb.append(String.format("• %s → %s%s%n  └ %s%n",
              a.relayId, a.desiredState ? "EIN" : "AUS", pulseInfo, a.reason));
        }
      }

      if (!blocked.isEmpty()) {
        sb.append("\n*⛔ Blockiert:*\n");
        for (String b : blocked) {
          sb.append("  • ").append(b).append("\n");
        }
      }

      if (!overrides.isEmpty()) {
        sb.append("\n*🚨 Safety-Override:*\n");
        for (String o : overrides) {
          sb.append("  • ").append(o).append("\n");
        }
      }

      sb.append("\n⚠️ *Aktuelle Steuerung:* Regelwerk (nicht AI)\n");
      sb.append("⏱ ").append(java.time.LocalDateTime.now()
          .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

      return sb.toString();
    }
  }
}

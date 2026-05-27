package de.hablijack.greenhouse.schedule;

import static de.hablijack.greenhouse.schedule.WaterControlScheduler.QUARKUS_CONDITION_TRIGGER;
import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;
import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import de.hablijack.greenhouse.webclient.TelegramClient;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class FanControlScheduler {
  static final Logger LOGGER = Logger.getLogger(FanControlScheduler.class.getName());
  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  @RestClient
  TelegramClient telegramClient;
  @Inject
  SatelliteService satelliteService;
  @ConfigProperty(name = "telegram.bot.token")
  String botToken;
  @ConfigProperty(name = "telegram.bot.chatid")
  String chatId;

  @ConfigProperty(name = "fan.maximum.temp", defaultValue = "35")
  int maximumTemp;
  @ConfigProperty(name = "fan.maximum.humidity", defaultValue = "95")
  int maximumHumidity;
  @ConfigProperty(name = "fan.minimum.sunshine.lux", defaultValue = "2500")
  int minimumSunshineLux;
  @ConfigProperty(name = "fan.hysteresis.temp.off", defaultValue = "32")
  int hysteresisTempOff;
  @ConfigProperty(name = "fan.hysteresis.humidity.off", defaultValue = "90")
  int hysteresisHumidityOff;
  @ConfigProperty(name = "fan.max.on.duration.ms", defaultValue = "7200000")
  long maxFanOnDurationMs;
  @ConfigProperty(name = "fan.cooldown.after.max.duration.ms", defaultValue = "900000")
  long fanCooldownAfterMaxDurationMs;
  @ConfigProperty(name = "fan.cron.activation.range", defaultValue = "* * 9-16 ? * * *")
  String cronActivationRange;

  volatile long lastForcedOffTimestamp = 0;

  @Scheduled(every = "10s", concurrentExecution = SKIP)
  void switchFansConditionally() {
    Relay fan = Relay.findByIdentifier("relay_line8");
    if (fan == null || !fan.satellite.online) {
      return;
    }

    if (RelayLog.isLastActionManualActivated(fan)) {
      LOGGER.log(Level.INFO, "Fan relay {0} skipped: last action was manual", fan.identifier);
      return;
    }

    Sensor humiditySensor = Sensor.findByIdentifier("air_humidity_inside");
    Measurement currentHumidity = humiditySensor.findCurrentMeasurement();

    Sensor tempInsideSensor = Sensor.findByIdentifier("air_temp_inside");
    Measurement currentTemp = tempInsideSensor.findCurrentMeasurement();

    Sensor brightnessSensor = Sensor.findByIdentifier("brightness");
    Measurement currentLux = brightnessSensor.findCurrentMeasurement();

    boolean triggerTimeFlag = isWithinTriggerTime();
    boolean environmentOk = triggerTimeFlag && currentLux.value >= minimumSunshineLux;

    boolean shouldBeOn = false;

    // Safety: force OFF if fan has been running too long
    if (fan.value && RelayLog.isRelayOnTooLong(fan, maxFanOnDurationMs)) {
      LOGGER.log(Level.WARNING, "Fan relay {0} forced OFF: exceeded max ON duration", fan.identifier);
      shouldBeOn = false;
      lastForcedOffTimestamp = System.currentTimeMillis();
    } else if (environmentOk) {
      // Only consider normal logic if within allowed time and brightness
      if (fan.value) {
        // Hysteresis: use lower thresholds to decide when to turn OFF
        shouldBeOn = currentTemp.value >= hysteresisTempOff
            || currentHumidity.value >= hysteresisHumidityOff;
      } else if (!isInCooldown()) {
        // Upper thresholds to decide when to turn ON (only if not in cooldown)
        shouldBeOn = currentTemp.value >= maximumTemp
            || currentHumidity.value >= maximumHumidity;
      }
    }

    if (shouldBeOn != fan.value) {
      switchRelay(fan, shouldBeOn);
    }
  }

  private boolean isInCooldown() {
    if (lastForcedOffTimestamp == 0) {
      return false;
    }
    return System.currentTimeMillis() - lastForcedOffTimestamp < fanCooldownAfterMaxDurationMs;
  }

  private void switchRelay(Relay fan, boolean value) {
    Map<String, Boolean> relayState = new HashMap<>();
    relayState.put(fan.identifier, value);
    try {
      satelliteClient = satelliteService.createSatelliteClient(fan.satellite.ip);
      satelliteClient.updateRelayState(relayState);
      persistFanRelaySwitch(fan.identifier, value);
    } catch (Exception error) {
      LOGGER.log(Level.WARNING, "Error on FanControlScheduler - could not switch relay: {0}", error.getMessage());
      try {
        telegramClient.sendMessage(botToken, chatId,
            "Fehler beim Schalten der Ventilatoren! \r\n\r\n"
                + "Konnte das Relay: " + fan.name + " nicht auf: "
                + value + " schalten.\r\n\r\n"
                + error.getMessage());
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Telegram notification failed: {0}", e.getMessage());
      }
    }
  }

  @Transactional(REQUIRES_NEW)
  void persistFanRelaySwitch(String relayIdentifier, boolean value) {
    Relay fan = Relay.findByIdentifier(relayIdentifier);
    if (fan == null) {
      return;
    }
    fan.value = value;
    new RelayLog(fan, QUARKUS_CONDITION_TRIGGER, new Date(), value).persist();
  }

  private boolean isWithinTriggerTime() {
    CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
    CronParser parser = new CronParser(cronDefinition);
    Cron unixCron = parser.parse(cronActivationRange);
    ExecutionTime executionTime = ExecutionTime.forCron(unixCron);
    return executionTime.isMatch(ZonedDateTime.now());
  }
}

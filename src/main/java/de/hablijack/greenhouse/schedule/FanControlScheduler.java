package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

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

  static final int MAXIMUM_HUMIDIY = 95; // more is bad for our tomatos
  static final int MAXIMUM_TEMP = 35; // more is bad for every plant growing
  static final int MINIMUM_SUNSHINE_LUX = 4000; // if we have no sunshine, we cant ventilate - battery!!!
  static final String CRON_ACTIVATION_RANGE = "* * 9-16 ? * * *";

  @Scheduled(every = "10s", concurrentExecution = SKIP)
  @Transactional
  void switchFansConditionally() {
    Relay fan = Relay.findByIdentifier("relay_line8");
    if (fan == null || !fan.satellite.online) {
      return;
    }

    Sensor humiditySensor = Sensor.findByIdentifier("air_humidity_inside");
    Measurement currentHumidity = humiditySensor.findCurrentMeasurement();

    Sensor tempInsideSensor = Sensor.findByIdentifier("air_temp_inside");
    Measurement currentTemp = tempInsideSensor.findCurrentMeasurement();

    Sensor brightnessSensor = Sensor.findByIdentifier("brightness");
    Measurement currentLux = brightnessSensor.findCurrentMeasurement();

    boolean triggerTimeFlag = isWithinTriggerTime();

    boolean newState = triggerTimeFlag
        && currentLux.value >= MINIMUM_SUNSHINE_LUX
        && (currentHumidity.value >= MAXIMUM_HUMIDIY || currentTemp.value >= MAXIMUM_TEMP);
    if (fan.satellite.online && newState != fan.value) {
      switchRelay(fan, newState);
    }
  }

  private void switchRelay(Relay fan, boolean value) {
    fan.value = value;
    Map<String, Boolean> relayState = new HashMap<>();
    relayState.put(fan.identifier, value);
    try {
      satelliteClient = satelliteService.createSatelliteClient(fan.satellite.ip);
      satelliteClient.updateRelayState(relayState);
      new RelayLog(fan, "CONDITION-INTELLIGENCE", new Date(), value).persist();
    } catch (Exception error) {
      LOGGER.warning("Error on FanControlScheduler - could not switch relay: " + error.getMessage());
      telegramClient.sendMessage(botToken, chatId, "Fehler beim Schalten der Ventilatoren: \r\n\r\n"
          + error.getMessage());
    }
  }

  private boolean isWithinTriggerTime() {
    CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
    CronParser parser = new CronParser(cronDefinition);
    Cron unixCron = parser.parse(CRON_ACTIVATION_RANGE);
    ExecutionTime executionTime = ExecutionTime.forCron(unixCron);
    return executionTime.isMatch(ZonedDateTime.now());
  }
}

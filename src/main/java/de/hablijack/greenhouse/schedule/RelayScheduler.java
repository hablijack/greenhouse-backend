package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import de.hablijack.greenhouse.webclient.TelegramClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class RelayScheduler {

  public static final String QUARKUS_TIME_TRIGGER = "TIME-TRIGGER";
  public static final String QUARKUS_CONDITION_TRIGGER = "CONDITION-TRIGGER";
  private static final Logger LOGGER = Logger.getLogger(RelayScheduler.class.getName());

  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  @RestClient
  TelegramClient telegramClient;
  @ConfigProperty(name = "telegram.bot.token")
  String botToken;
  @ConfigProperty(name = "telegram.bot.chatid")
  String chatId;

  @SuppressFBWarnings("CRLF_INJECTION_LOGS")
  @Scheduled(every = "1m", concurrentExecution = SKIP)
  @Transactional
  void switchRelaysConditionally() {
    Boolean newState = null;
    String trigger = null;
    for (PanacheEntityBase entity : Relay.listAll()) {
      Relay relay = (Relay) entity;
      LOGGER.warning("=========================================");
      LOGGER.warning(String.valueOf(relay.satellite.id));
      LOGGER.warning("=========================================");
      Satellite satellite = Satellite.findById(relay.satellite.id);
      LOGGER.warning(satellite.ip);
      LOGGER.warning("=========================================");
      if (relay.timeTrigger.active) {
        if (isWithinTriggerTime(relay)) {
          trigger = QUARKUS_TIME_TRIGGER;
          newState = true;
        } else {
          trigger = QUARKUS_TIME_TRIGGER;
          newState = false;
        }
      }
      if (relay.conditionTrigger.active) {
        if (isConditionTriggered(relay)) {
          trigger = QUARKUS_CONDITION_TRIGGER;
          newState = true;
        } else {
          trigger = QUARKUS_CONDITION_TRIGGER;
          newState = false;
        }
      }

      if (newState != null && newState != relay.value) {
        Map<String, Boolean> relayState = new HashMap<>();
        relay.value = newState;
        relayState.put(relay.identifier, relay.value);
        LOGGER.warning("=========================================");
        LOGGER.warning(String.valueOf(relay.satellite.ip));
        try {
          satelliteClient = RestClientBuilder.newBuilder().baseUrl(
              new URL("http://" + String.valueOf(relay.satellite.ip))
          ).build(SatelliteClient.class);
          satelliteClient.updateRelayState(relayState);
          relay.persist();
          new RelayLog(relay, trigger, new Date(), newState).persist();
        } catch (Exception error) {
          LOGGER.warning(error.getMessage());
          telegramClient.sendMessage(botToken, chatId,
              "Fehler beim Einschalten des Relays! \r\n\r\n"
                  + "Konnte das Relay: " + relay.name + " nicht auf: "
                  + relay.value + " schalten.\r\n\r\n"
                  + error.getMessage());
        }
      }
      newState = null;
      trigger = null;
    }
  }

  private boolean isWithinTriggerTime(Relay relay) {
    CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
    CronParser parser = new CronParser(cronDefinition);
    Cron unixCron = parser.parse(relay.timeTrigger.cronString);
    ExecutionTime executionTime = ExecutionTime.forCron(unixCron);
    return executionTime.isMatch(ZonedDateTime.now());
  }

  private boolean isConditionTriggered(Relay relay) {
    Sensor triggerSensor = relay.conditionTrigger.triggerSensor;
    return triggerSensor.findCurrentMeasurement().value >= triggerSensor.maxAlarmValue;
  }
}

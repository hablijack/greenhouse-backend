package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;
import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import de.hablijack.greenhouse.webclient.TelegramClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class WaterControlScheduler {

  public static final String QUARKUS_TIME_TRIGGER = "TIME-TRIGGER";
  public static final String QUARKUS_CONDITION_TRIGGER = "CONDITION-TRIGGER";
  private static final Logger LOGGER = Logger.getLogger(WaterControlScheduler.class.getName());
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

  @SuppressFBWarnings(value = {"REC_CATCH_EXCEPTION"})
  @Scheduled(every = "5s", concurrentExecution = SKIP)
  @Transactional(REQUIRES_NEW)
  void switchRelaysConditionally() {
    Boolean newState = null;
    String trigger = null;
    List<Relay> waterRelays = Relay.listAllWaterRelays();
    if (waterRelays == null || waterRelays.isEmpty()) {
      return;
    }
    for (Relay relay : waterRelays) {
      if (!relay.satellite.online || RelayLog.isLastActionManualActivated(relay)) {
        return;
      }
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
        try {
          // PREPARE REQUEST
          Map<String, Boolean> relayState = new HashMap<>();
          relayState.put(relay.identifier, newState);
          satelliteClient = satelliteService.createSatelliteClient(relay.satellite.ip);
          // FIRE UP SATELLITE REQUEST
          satelliteClient.updateRelayState(relayState);
          // UPDATE DATABASE WITH NEW STATES
          relay.value = newState;
          new RelayLog(relay, trigger, new Date(), newState).persist();
        } catch (Exception error) {
          LOGGER.warning("Error on RelayScheduler - could not switch relay: " + error.getMessage());
          try {
            telegramClient.sendMessage(botToken, chatId,
                "Fehler beim Schalten des Relays! \r\n\r\n"
                    + "Konnte das Relay: " + relay.name + " nicht auf: "
                    + relay.value + " schalten.\r\n\r\n"
                    + error.getMessage());
          } catch (Exception e) {
            LOGGER.warning("Fehler beim versenden der Telegramm Nachricht: " + e.getMessage());
          }
        }
      }
      newState = null;
      trigger = null;
    }
  }

  private boolean isWithinTriggerTime(Relay relay) {
    CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
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

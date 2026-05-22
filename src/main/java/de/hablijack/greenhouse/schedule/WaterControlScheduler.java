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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class WaterControlScheduler {

  public static final String QUARKUS_TIME_TRIGGER = "TIME-TRIGGER";
  public static final String QUARKUS_CONDITION_TRIGGER = "CONDITION-TRIGGER";
  private static final long MAX_RELAY_ON_DURATION_MS = 60_000; // 1 min safety limit
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
  void switchRelaysConditionally() {
    Boolean newState = null;
    String trigger = null;
    List<Relay> waterRelays = Relay.listAllWaterRelays();
    if (waterRelays == null || waterRelays.isEmpty()) {
      return;
    }
    for (Relay relay : waterRelays) {
      if (!relay.satellite.online) {
        LOGGER.log(Level.FINE, "Relay {0} skipped: satellite offline", relay.identifier);
        continue;
      }
      if (RelayLog.isLastActionManualActivated(relay)) {
        LOGGER.log(Level.INFO, "Relay {0} skipped: last action was manual", relay.identifier);
        continue;
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

      // Safety: force OFF if relay has been ON beyond the safety limit
      if (relay.value && RelayLog.isRelayOnTooLong(relay, MAX_RELAY_ON_DURATION_MS)) {
        LOGGER.log(Level.WARNING, "Relay {0} forced OFF: exceeded max ON duration", relay.identifier);
        trigger = QUARKUS_CONDITION_TRIGGER;
        newState = false;
      }

      if (newState != null && newState != relay.value) {
        LOGGER.log(Level.INFO, "Relay {0}: switching {1} via {2}",
            new Object[] {relay.identifier, newState, trigger});
        try {
          Map<String, Boolean> relayState = new HashMap<>();
          relayState.put(relay.identifier, newState);
          satelliteClient = satelliteService.createSatelliteClient(relay.satellite.ip);
          satelliteClient.updateRelayState(relayState);
          persistRelaySwitch(relay.identifier, trigger, newState);
        } catch (Exception error) {
          LOGGER.log(Level.SEVERE, "Relay {0}: switch to {1} failed: {2}",
              new Object[] {relay.identifier, newState, error.getMessage()});
          try {
            telegramClient.sendMessage(botToken, chatId,
                "Fehler beim Schalten des Relays! \r\n\r\n"
                    + "Konnte das Relay: " + relay.name + " nicht auf: "
                    + newState + " schalten.\r\n\r\n"
                    + error.getMessage());
          } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Telegram notification failed: {0}", e.getMessage());
          }
        }
      }
      newState = null;
      trigger = null;
    }
  }

  @Transactional(REQUIRES_NEW)
  void persistRelaySwitch(String relayIdentifier, String trigger, boolean newState) {
    Relay relay = Relay.findByIdentifier(relayIdentifier);
    if (relay == null) {
      return;
    }
    relay.value = newState;
    new RelayLog(relay, trigger, new Date(), newState).persist();
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

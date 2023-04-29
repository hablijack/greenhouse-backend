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
import de.hablijack.greenhouse.entity.Sensor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Date;

@ApplicationScoped
public class RelayScheduler {

  public static final String QUARKUS_TIME_TRIGGER = "TIME-TRIGGER";
  public static final String QUARKUS_CONDITION_TRIGGER = "CONDITION-TRIGGER";

  @SuppressFBWarnings("CRLF_INJECTION_LOGS")
  @Scheduled(every = "1m", concurrentExecution = SKIP)
  @Transactional
  void switchRelaysConditionally() {
    Boolean newState = null;
    String trigger = null;
    for (PanacheEntityBase entity : Relay.getAllRelaysForScheduler()) {
      Relay relay = (Relay) entity;

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
        relay.value = newState;
        relay.persist();
        new RelayLog(relay, trigger, new Date(), newState).persist();
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

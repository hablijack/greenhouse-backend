package de.hablijack.greenhouse.entity;

import static de.hablijack.greenhouse.schedule.RelayScheduler.QUARKUS_CONDITION_TRIGGER;
import static de.hablijack.greenhouse.schedule.RelayScheduler.QUARKUS_TIME_TRIGGER;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "relay_log", schema = "greenhouse")
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class RelayLog extends PanacheEntity {
  private static final int ONE_MONTH_PAST_IN_DAYS = -31;
  @Column(name = "initiator", nullable = false)
  public String initiator;
  @Column(name = "timestamp", nullable = false)
  public Date timestamp;
  @Column(name = "value", nullable = false)
  public boolean value;
  @ManyToOne(fetch = FetchType.LAZY)
  @JsonManagedReference
  public Relay relay;

  public RelayLog() {
  }

  public RelayLog(Relay relay, String initiator, Date timestamp, boolean value) {
    this.relay = relay;
    this.value = value;
    this.initiator = initiator;
    this.timestamp = timestamp;
  }

  public static boolean isLastActionManualActivated(Relay relay) {
    RelayLog lastAction = (RelayLog) RelayLog.find("relay=?1 ORDER BY timestamp DESC", relay).range(0, 1).list().get(0);
    return !lastAction.initiator.equals(QUARKUS_TIME_TRIGGER)
        && !lastAction.initiator.equals(QUARKUS_CONDITION_TRIGGER) && lastAction.value;
  }

  public static void cleanupOldEntries() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, ONE_MONTH_PAST_IN_DAYS);
    delete("timestamp<=?1", cal.getTime());
  }

  public static List<RelayLog> getRecentLog(int maxEntries) {
    return RelayLog.find("ORDER BY timestamp DESC").range(0, maxEntries).list();
  }

  public void persistIfInitForThisRelay() {
    if (find("relay = ?1", relay).count() == 0) {
      this.persist();
    }
  }
}

package de.hablijack.greenhouse.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "relay_log", schema = "greenhouse")
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class RelayLog extends PanacheEntity {
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

  public static List<RelayLog> getRecentLog(int maxEntries) {
    return RelayLog.find("ORDER BY timestamp DESC").range(0, maxEntries).list();
  }
}

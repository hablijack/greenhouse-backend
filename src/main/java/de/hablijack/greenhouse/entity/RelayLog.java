package de.hablijack.greenhouse.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "relay_log", schema = "greenhouse")
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
    return RelayLog.find("ORDER BY timestamp").range(0, maxEntries).list();
  }
}

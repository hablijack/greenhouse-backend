package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "time_trigger", schema = "greenhouse")
public class TimeTrigger extends PanacheEntity {
  @OneToOne(fetch = FetchType.LAZY)
  public Relay relay;

  @Column(name = "cron_string", nullable = false)
  public String cronString;

  @Column(name = "active", nullable = false)
  public boolean active;

  public TimeTrigger() {
  }

  public TimeTrigger(String cronString, boolean active) {
    this.cronString = cronString;
    this.active = active;
  }

  public TimeTrigger persistIfNotExist() {
    if (id == null || findById(id).count() == 0) {
      this.persist();
      return this;
    } else {
      return findById(id);
    }
  }
}

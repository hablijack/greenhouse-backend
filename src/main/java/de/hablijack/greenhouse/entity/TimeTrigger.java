package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

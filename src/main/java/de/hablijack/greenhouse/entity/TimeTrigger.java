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

  @Column(name = "start_cron_string", nullable = false)
  public String startCronString;

  @Column(name = "end_cron_string", nullable = false)
  public String endCronString;

  @Column(name = "active", nullable = false)
  public boolean active;

  public TimeTrigger() {
  }

  public TimeTrigger(String startCronString, String endCronString, boolean active) {
    this.startCronString = startCronString;
    this.endCronString = endCronString;
    this.active = active;
  }

  public TimeTrigger persistIfNotExist() {
    if (findById(id).count() == 0) {
      this.persist();
      return this;
    } else {
      return findById(id);
    }
  }
}

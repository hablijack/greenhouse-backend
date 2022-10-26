package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "condition_trigger", schema = "greenhouse")
public class ConditionTrigger extends PanacheEntity {
  @OneToOne(fetch = FetchType.LAZY)
  public Relay relay;

  @OneToOne(fetch = FetchType.LAZY)
  public Sensor triggerSensor;
  @Column(name = "active", nullable = false)
  public boolean active;

  public ConditionTrigger() {
  }

  public ConditionTrigger(Sensor triggerSensor, boolean active) {
    this.triggerSensor = triggerSensor;
    this.active = active;
  }

  public ConditionTrigger persistIfNotExist() {
    if (id == null || findById(id).count() == 0) {
      this.persist();
      return this;
    } else {
      return findById(id);
    }
  }
}

package de.hablijack.greenhouse.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "condition_trigger", schema = "greenhouse")
public class ConditionTrigger extends PanacheEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JsonBackReference
  public Relay relay;

  @OneToOne(fetch = FetchType.LAZY)
  public Sensor triggerSensor;
  @Column(name = "active", nullable = false)
  public boolean active;

  public ConditionTrigger() {
  }


  public ConditionTrigger(Sensor triggerSensor, boolean active, Relay relay) {
    this.relay = relay;
    this.triggerSensor = triggerSensor;
    this.active = active;
  }

  public ConditionTrigger persistIfNotExist() {
    if (relay == null || find("relay = ?1", relay).count() == 0) {
      this.persist();
      return this;
    } else {
      return (ConditionTrigger) find("relay = ?1", relay).list().get(0);
    }
  }
}

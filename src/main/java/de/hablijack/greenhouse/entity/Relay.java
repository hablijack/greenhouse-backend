package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "relay", schema = "greenhouse")
public class Relay extends PanacheEntity {

  @Column(name = "identifier", nullable = false, unique = true)
  public String identifier;
  @Column(name = "name", nullable = false)
  public String name;
  @Column(name = "target")
  public String target;
  @Column(name = "value", nullable = false)
  public boolean value;
  @Column(name = "description", nullable = false)
  public String description;
  @Column(name = "icon", nullable = false)
  public String icon;
  @Column(name = "color", nullable = false)
  public String color;

  @OneToOne(fetch = FetchType.LAZY)
  public ConditionTrigger conditionTrigger;

  @OneToOne(fetch = FetchType.LAZY)
  public TimeTrigger timeTrigger;

  @ManyToOne(fetch = FetchType.LAZY)
  public Satelite satelite;

  public Relay() {
  }

  public Relay(String identifier, String name, String target, boolean value, String description, String icon,
      String color, ConditionTrigger conditionTrigger, TimeTrigger timeTrigger, Satelite satelite) {
    this.identifier = identifier;
    this.name = name;
    this.value = value;
    this.description = description;
    this.icon = icon;
    this.color = color;
    this.conditionTrigger = conditionTrigger;
    this.timeTrigger = timeTrigger;
    this.satelite = satelite;
    this.target = target;
  }

  public static Relay findByIdentifier(String id) {
    return (Relay) find("identifier = ?1", id).list().get(0);
  }

  public Relay persistIfNotExist() {
    if (find("identifier = ?1", identifier).count() == 0) {
      this.persist();
      return this;
    } else {
      return (Relay) find("identifier = ?1", identifier).list().get(0);
    }
  }
}

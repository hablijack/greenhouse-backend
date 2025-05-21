package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;

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
  @Column(name = "sortkey")
  public int sortkey;

  @OneToOne(fetch = FetchType.LAZY)
  public ConditionTrigger conditionTrigger;

  @OneToOne(fetch = FetchType.LAZY)
  public TimeTrigger timeTrigger;

  @ManyToOne(fetch = FetchType.LAZY)
  public Satellite satellite;

  public Relay() {
  }

  public Relay(String identifier, String name, String target, boolean value, String description, String icon,
               String color, Satellite satellite, int sortkey) {
    this.identifier = identifier;
    this.name = name;
    this.value = value;
    this.description = description;
    this.icon = icon;
    this.color = color;
    this.satellite = satellite;
    this.target = target;
    this.sortkey = sortkey;
  }

  public static Relay findByIdentifier(String id) {
    List<Relay> relays = list("identifier = ?1", id);
    if (relays.isEmpty()) {
      return null;
    } else {
      return relays.getFirst();
    }
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

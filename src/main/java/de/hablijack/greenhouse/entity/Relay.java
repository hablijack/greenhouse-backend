package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "relay", schema = "greenhouse")
public class Relay extends PanacheEntity {

  @Column(name = "identifier", nullable = false, unique = true)
  public String identifier;
  @Column(name = "name", nullable = false)
  public String name;
  @Column(name = "value", nullable = false)
  public boolean value;
  @Column(name = "description", nullable = false)
  public String description;
  @Column(name = "icon", nullable = false)
  public String icon;
  @Column(name = "color", nullable = false)
  public String color;

  public Relay() {
  }

  public Relay(String identifier, String name, boolean value, String description, String icon, String color) {
    this.identifier = identifier;
    this.name = name;
    this.value = value;
    this.description = description;
    this.icon = icon;
    this.color = color;
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

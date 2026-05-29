package de.hablijack.greenhouse.ai.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "plant", schema = "greenhouse")
public class Plant extends PanacheEntity {

  @Column(name = "name", nullable = false, unique = true)
  public String name;

  @Column(name = "description", columnDefinition = "TEXT")
  public String description;

  public Plant() {
  }

  public Plant(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public static Plant findByName(String name) {
    return find("name = ?1", name).firstResult();
  }
}

package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "sensor", schema = "greenhouse")
public class Sensor extends PanacheEntity {

  @Column(name = "name", nullable = false)
  public String name;

  @Column(name = "unit", nullable = false)
  public String unit;

  @Column(name = "decimals", nullable = false)
  public Integer decimals;

  @Column(name = "description", nullable = false)
  public String description;

  @Column(name = "icon", nullable = false)
  public String icon;

  @Column(name = "min_alarm_value", nullable = false)
  public Double minAlarmValue;

  @Column(name = "max_alarm_value", nullable = false)
  public Double maxAlarmValue;

  public Sensor() {
  }

  public Sensor(String name, String unit, Integer decimals, String description, String icon, Double minAlarmValue,
                Double maxAlarmValue) {
    this.name = name;
    this.unit = unit;
    this.decimals = decimals;
    this.description = description;
    this.icon = icon;
    this.minAlarmValue = minAlarmValue;
    this.maxAlarmValue = maxAlarmValue;
  }

  public Sensor persistIfNotExist() {
    if (find("name = ?1 AND unit = ?2", name, unit).count() == 0) {
      this.persist();
      return this;
    } else {
      return (Sensor) find("name = ?1 AND unit = ?2", name, unit).list().get(0);
    }
  }
}

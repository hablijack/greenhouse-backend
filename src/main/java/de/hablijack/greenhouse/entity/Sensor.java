package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "sensor", schema = "greenhouse")
public class Sensor extends PanacheEntity {

  @Column(name = "identifier", nullable = false, unique = true)
  public String identifier;
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

  public Sensor(String identifier, String name, String unit, Integer decimals, String description, String icon,
                Double minAlarmValue,
                Double maxAlarmValue) {
    this.identifier = identifier;
    this.name = name;
    this.unit = unit;
    this.decimals = decimals;
    this.description = description;
    this.icon = icon;
    this.minAlarmValue = minAlarmValue;
    this.maxAlarmValue = maxAlarmValue;
  }

  public Measurement findCurrentMeasurement() {
    return Measurement.find("sensor = ?1 ORDER BY timestamp", this).firstResult();
  }

  public static Sensor findByIdentifier(String id) {
    return Sensor.find("identifier = ?1", id).firstResult();
  }

  public Sensor persistIfNotExist() {
    if (find("identifier = ?1", identifier).count() == 0) {
      this.persist();
      return this;
    } else {
      return (Sensor) find("identifier = ?1", identifier).list().get(0);
    }
  }

  public List<Measurement> findMeasurementsWithinTimeRange(String timeRange) {
    return Measurement.find("sensor = ?1 ORDER BY timestamp", this).list();
  }
}

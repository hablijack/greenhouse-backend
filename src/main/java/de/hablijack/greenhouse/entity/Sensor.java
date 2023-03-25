package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "sensor", schema = "greenhouse")
public class Sensor extends PanacheEntity {

  private static final int DAYS_A_WEEK = 7;
  private static final int DAYS_A_MONTH = 7;
  private static final int ONE_DAY = 1;
  private static final int DAY_TO_MS_FACTOR = 86400000;

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

  public static Sensor findByIdentifier(String id) {
    return Sensor.find("identifier = ?1", id).firstResult();
  }

  public Measurement findCurrentMeasurement() {
    return Measurement.find("sensor = ?1 ORDER BY timestamp", this).firstResult();
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
    long days = 0;
    if (timeRange.equals("week")) {
      days = DAYS_A_WEEK;
    } else if (timeRange.equals("month")) {
      days = DAYS_A_MONTH;
    } else {
      days = ONE_DAY;
    }
    Date ago = new Date(System.currentTimeMillis() - (days * DAY_TO_MS_FACTOR));
    return Measurement.find("sensor = ?1 AND timestamp >= ?2 ORDER BY timestamp", this, ago).list();
  }
}

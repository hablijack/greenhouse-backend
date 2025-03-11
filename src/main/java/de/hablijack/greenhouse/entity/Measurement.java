package de.hablijack.greenhouse.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "measurement", schema = "greenhouse")
public class Measurement extends PanacheEntity {

  private static final int ONE_MONTH_PAST_IN_DAYS = -31;

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  public Sensor sensor;

  @Column(name = "value", nullable = false)
  public Double value;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  public Date timestamp;

  public Measurement() {
  }

  public Measurement(Sensor sensor, Double value, Date timestamp) {
    this.sensor = sensor;
    this.value = value;
    this.timestamp = timestamp;
  }

  public static void cleanupOldEntries() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, ONE_MONTH_PAST_IN_DAYS);
    delete("timestamp<=?1", cal.getTime());
  }

  public void persistIfInitForThisSensor() {
    if (find("sensor = ?1", sensor).count() == 0) {
      this.persist();
    }
  }
}

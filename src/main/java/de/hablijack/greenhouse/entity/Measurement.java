package de.hablijack.greenhouse.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "measurement", schema = "greenhouse")
public class Measurement extends PanacheEntity {

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
}

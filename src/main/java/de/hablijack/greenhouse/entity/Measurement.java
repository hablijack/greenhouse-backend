package de.hablijack.greenhouse.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

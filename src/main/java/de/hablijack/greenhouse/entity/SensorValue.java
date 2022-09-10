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
@Table(name = "sensor_value", schema = "greenhouse")
public class SensorValue extends PanacheEntity {

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  public Sensor sensor;

  @Column(name = "value", nullable = false)
  public Double value;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  public Date timestamp;

  public SensorValue() {
  }
}

package eu.hablijack.persistence.sensor;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import java.time.Instant;

@Measurement(name = "air_temp_inside")
public class AirTempInside {

  @Column
  private Double value;

  @Column(timestamp = true)
  private Instant time;

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }
}
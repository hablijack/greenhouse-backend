package de.hablijack.greenhouse.api.pojo;

public class TableSize {

  private Long measurementSizeByte;

  private Long relayLogSizeByte;

  public TableSize(Long measurementSizeByte, Long relayLogSizeByte) {
    this.measurementSizeByte = measurementSizeByte;
    this.relayLogSizeByte = relayLogSizeByte;
  }

  public TableSize() {
  }

  public Long getMeasurementSizeByte() {
    return measurementSizeByte;
  }

  public void setMeasurementSizeByte(Long measurementSizeByte) {
    this.measurementSizeByte = measurementSizeByte;
  }

  public Long getRelayLogSizeByte() {
    return relayLogSizeByte;
  }

  public void setRelayLogSizeByte(Long relayLogSizeByte) {
    this.relayLogSizeByte = relayLogSizeByte;
  }
}

package de.hablijack.greenhouse.api.pojo;

import java.math.BigInteger;

public class TableSize {

  private BigInteger measurementSizeByte;

  private BigInteger relayLogSizeByte;

  public TableSize(BigInteger measurementSizeByte, BigInteger relayLogSizeByte) {
    this.measurementSizeByte = measurementSizeByte;
    this.relayLogSizeByte = relayLogSizeByte;
  }

  public TableSize() {
  }

  public BigInteger getMeasurementSizeByte() {
    return measurementSizeByte;
  }

  public void setMeasurementSizeByte(BigInteger measurementSizeByte) {
    this.measurementSizeByte = measurementSizeByte;
  }

  public BigInteger getRelayLogSizeByte() {
    return relayLogSizeByte;
  }

  public void setRelayLogSizeByte(BigInteger relayLogSizeByte) {
    this.relayLogSizeByte = relayLogSizeByte;
  }
}

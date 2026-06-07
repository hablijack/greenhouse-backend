package de.hablijack.greenhouse.ai.service;

public class HistoryData {

  public final SensorTrend temperature;
  public final SensorTrend humidity;
  public final SensorTrend soilMoisture;
  public final SensorTrend light;
  public final SensorTrend co2;
  public final TimeContext time;

  public HistoryData(SensorTrend temperature, SensorTrend humidity,
      SensorTrend soilMoisture, SensorTrend light, SensorTrend co2,
      TimeContext time) {
    this.temperature = temperature;
    this.humidity = humidity;
    this.soilMoisture = soilMoisture;
    this.light = light;
    this.co2 = co2;
    this.time = time;
  }
}

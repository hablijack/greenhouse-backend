package de.hablijack.greenhouse.ai.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public class SensorDataRequest {

  @NotBlank
  public String plantType;

  @NotNull
  public Double temperature;

  @NotNull
  public Double humidity;

  @NotNull
  public Double soilMoisture;

  @NotNull
  public Double lightIntensity;

  @NotNull
  public Double co2Level;

  public Integer currentHour;

  public Integer currentMonth;

  public SensorDataRequest() {
  }

  public SensorDataRequest(String plantType, Double temperature, Double humidity,
      Double soilMoisture, Double lightIntensity, Double co2Level) {
    this.plantType = plantType;
    this.temperature = temperature;
    this.humidity = humidity;
    this.soilMoisture = soilMoisture;
    this.lightIntensity = lightIntensity;
    this.co2Level = co2Level;
  }

  public SensorDataRequest(String plantType, Double temperature, Double humidity,
      Double soilMoisture, Double lightIntensity, Double co2Level,
      Integer currentHour, Integer currentMonth) {
    this.plantType = plantType;
    this.temperature = temperature;
    this.humidity = humidity;
    this.soilMoisture = soilMoisture;
    this.lightIntensity = lightIntensity;
    this.co2Level = co2Level;
    this.currentHour = currentHour;
    this.currentMonth = currentMonth;
  }
}

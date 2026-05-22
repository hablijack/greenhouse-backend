package de.hablijack.greenhouse.api.pojo;

public class PlantingTrack {

  private int lineNumber;
  private String plantType;
  private RelayInfo relay;
  private SensorInfo sensor;

  public PlantingTrack() {
  }

  public PlantingTrack(int lineNumber, String plantType, RelayInfo relay, SensorInfo sensor) {
    this.lineNumber = lineNumber;
    this.plantType = plantType;
    this.relay = relay;
    this.sensor = sensor;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public String getPlantType() {
    return plantType;
  }

  public void setPlantType(String plantType) {
    this.plantType = plantType;
  }

  public RelayInfo getRelay() {
    return relay;
  }

  public void setRelay(RelayInfo relay) {
    this.relay = relay;
  }

  public SensorInfo getSensor() {
    return sensor;
  }

  public void setSensor(SensorInfo sensor) {
    this.sensor = sensor;
  }

  public static class RelayInfo {
    private final long id;
    private final String identifier;
    private final boolean value;

    public RelayInfo() {
      this.id = 0;
      this.identifier = "";
      this.value = false;
    }

    public RelayInfo(long id, String identifier, boolean value) {
      this.id = id;
      this.identifier = identifier;
      this.value = value;
    }

    public long getId() {
      return id;
    }

    public String getIdentifier() {
      return identifier;
    }

    public boolean isValue() {
      return value;
    }
  }

  public static class SensorInfo {
    private final String identifier;
    private final Double currentValue;
    private final Double minAlarm;
    private final Double maxAlarm;
    private final String unit;

    public SensorInfo() {
      this.identifier = "";
      this.currentValue = null;
      this.minAlarm = null;
      this.maxAlarm = null;
      this.unit = "";
    }

    public SensorInfo(String identifier, Double currentValue, Double minAlarm, Double maxAlarm, String unit) {
      this.identifier = identifier;
      this.currentValue = currentValue;
      this.minAlarm = minAlarm;
      this.maxAlarm = maxAlarm;
      this.unit = unit;
    }

    public String getIdentifier() {
      return identifier;
    }

    public Double getCurrentValue() {
      return currentValue;
    }

    public Double getMinAlarm() {
      return minAlarm;
    }

    public Double getMaxAlarm() {
      return maxAlarm;
    }

    public String getUnit() {
      return unit;
    }
  }
}

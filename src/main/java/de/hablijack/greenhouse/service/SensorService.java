package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Sensor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SensorService {
  @Transactional
  public Map<String, Double> getCurrentSensorValues() {
    Map<String, Double> currentValues = new HashMap<>();
    List<Sensor> sensors = Sensor.listAll();
    for (Sensor sensor : sensors) {
      Measurement measurement = sensor.findCurrentMeasurement();
      if (measurement != null) {
        currentValues.put(sensor.identifier, measurement.value);
      }
    }
    return currentValues;
  }
}

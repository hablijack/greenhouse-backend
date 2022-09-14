package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.Sensor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
public class SensorService {
  @Transactional
  public Map<String, Double> getCurrentSensorValues() {
    Map<String, Double> currentValues = new HashMap<>();
    List<Sensor> sensors = Sensor.listAll();
    for (Sensor sensor : sensors) {
      currentValues.put(sensor.identifier, sensor.findCurrentMeasurement().value);
    }
    return currentValues;
  }
}

package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.Sensor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SensorService {
  @Transactional
  @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
  public Map<String, Double> getCurrentSensorValues() {
    Map<String, Double> currentValues = new HashMap<>();
    List<Sensor> sensors = Sensor.listAll();
    for (Sensor sensor : sensors) {
      currentValues.put(sensor.identifier, sensor.findCurrentMeasurement().value);
    }
    return currentValues;
  }
}

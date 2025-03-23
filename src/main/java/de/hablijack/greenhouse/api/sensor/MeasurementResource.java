package de.hablijack.greenhouse.api.sensor;

import de.hablijack.greenhouse.service.SensorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/api/rest")
public class MeasurementResource {

  @Inject
  SensorService sensorService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/sensors/measurements/live")

  public Map<String, Double> live() {
    return sensorService.getCurrentSensorValues();
  }
}

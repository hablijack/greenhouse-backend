package de.hablijack.greenhouse.api.sensor;

import de.hablijack.greenhouse.service.SensorService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/backend")
public class MeasurementResource {

  @Inject
  SensorService sensorService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/sensors/measurements/live")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public Map<String, Double> live() {
    return sensorService.getCurrentSensorValues();
  }
}

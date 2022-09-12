package de.hablijack.greenhouse.api.sensor;

import de.hablijack.greenhouse.service.SensorService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class MeasurementResource {

  @Inject
  SensorService sensorService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/sensors/measurements/live")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public String live() {
    sensorService.getCurrentValues();
    return "Hello from RESTEasy Reactive";
  }
}

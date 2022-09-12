package de.hablijack.greenhouse.api.sensor;

import de.hablijack.greenhouse.service.SensorService;
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
  public String live() {
    sensorService.getCurrentValues();
    return "Hello from RESTEasy Reactive";
  }
}

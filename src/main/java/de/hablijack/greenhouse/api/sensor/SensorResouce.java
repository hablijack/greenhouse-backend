package de.hablijack.greenhouse.api.sensor;

import de.hablijack.greenhouse.entity.Sensor;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class SensorResouce {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/sensors")
  public List<Sensor> getAllSensors() {
    return Sensor.listAll();
  }
}

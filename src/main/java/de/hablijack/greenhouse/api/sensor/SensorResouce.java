package de.hablijack.greenhouse.api.sensor;

import de.hablijack.greenhouse.entity.Sensor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<Sensor> getAllSensors() {
    return Sensor.listAll();
  }
}

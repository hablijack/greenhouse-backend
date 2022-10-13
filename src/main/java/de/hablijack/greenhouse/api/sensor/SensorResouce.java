package de.hablijack.greenhouse.api.sensor;

import de.hablijack.greenhouse.entity.Sensor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/sensor/{identifier}")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public Sensor getOneSensor(@PathParam("identifier") Long identifier) {
    return Sensor.findById(identifier);
  }

  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/sensor/{identifier}")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public Sensor updateOneSensor(@PathParam("identifier") Long identifier, Sensor sensor) {
    sensor.persist();
    return sensor;
  }
}

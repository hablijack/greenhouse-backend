package de.hablijack.greenhouse.api.sensor;

import de.hablijack.greenhouse.entity.Sensor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/backend")
public class SensorResouce {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/sensors")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<Sensor> getAllSensors() {
    return Sensor.list("order by identifier");
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
  @Transactional
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public Sensor updateOneSensor(@PathParam("identifier") Long identifier, Sensor newSensor) {
    Sensor oldSensor = Sensor.findById(identifier);
    oldSensor.identifier = newSensor.identifier;
    oldSensor.icon = newSensor.icon;
    oldSensor.unit = newSensor.unit;
    oldSensor.decimals = newSensor.decimals;
    oldSensor.minAlarmValue = newSensor.minAlarmValue;
    oldSensor.maxAlarmValue = newSensor.maxAlarmValue;
    oldSensor.description = newSensor.description;
    oldSensor.name = newSensor.name;
    oldSensor.persist();
    return oldSensor;
  }
}

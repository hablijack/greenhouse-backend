package de.hablijack.greenhouse.webclient;

import de.hablijack.greenhouse.webclient.pojo.SleepTime;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface SatelliteClient {

  @GET
  @Path("/health")
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Yes it is a REST endpoint o.O ?!")
  JsonObject healthcheck();

  @GET
  @Path("/capture")
  @Produces(MediaType.TEXT_PLAIN)
  @SuppressFBWarnings(value = "", justification = "Yes it is a REST endpoint o.O ?!")
  String takePicture();

  @GET
  @Path("/saved-photo")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @SuppressFBWarnings(value = "", justification = "Yes it is a REST endpoint o.O ?!")
  File savePicture();

  @GET
  @Path("/sensors/measurements")
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Yes it is a REST endpoint o.O ?!")
  JsonObject getMeasurements();

  @GET
  @Path("/relays/state")
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Yes it is a REST endpoint o.O ?!")
  JsonObject getRelayState();

  @POST
  @Path("/relays/set")
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Yes it is a REST endpoint o.O ?!")
  JsonObject updateRelayState(Map<String, Boolean> relaysState);

  @POST
  @Path("/system/deepsleep")
  @Produces(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Yes it is a REST endpoint o.O ?!")
  JsonObject deepSleep(SleepTime sleepTime);
}

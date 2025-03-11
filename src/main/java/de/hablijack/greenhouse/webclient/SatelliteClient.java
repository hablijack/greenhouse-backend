package de.hablijack.greenhouse.webclient;

import de.hablijack.greenhouse.webclient.pojo.SleepTime;
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
  JsonObject healthcheck();

  @GET
  @Path("/capture")
  @Produces(MediaType.TEXT_PLAIN)
  String takePicture();

  @GET
  @Path("/saved-photo")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  File savePicture();

  @GET
  @Path("/sensors/measurements")
  @Produces(MediaType.APPLICATION_JSON)
  JsonObject getMeasurements();

  @GET
  @Path("/relays/state")
  @Produces(MediaType.APPLICATION_JSON)
  JsonObject getRelayState();

  @POST
  @Path("/relays/set")
  @Produces(MediaType.APPLICATION_JSON)
  JsonObject updateRelayState(Map<String, Boolean> relaysState);

  @POST
  @Path("/system/deepsleep")
  @Produces(MediaType.APPLICATION_JSON)
  JsonObject deepSleep(SleepTime sleepTime);
}

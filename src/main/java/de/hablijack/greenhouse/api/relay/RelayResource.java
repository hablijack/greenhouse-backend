package de.hablijack.greenhouse.api.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.api.pojo.RelayLogEvent;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import jakarta.transaction.Transactional;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logmanager.Level;

@Path("/api/rest")
public class RelayResource {
  private static final int NUMBER_OF_LOG_ENTRIES = 30;
  private static final Logger LOGGER = Logger.getLogger(RelayResource.class.getName());
  private final ObjectMapper objectMapper;
  @RestClient
  SatelliteClient satelliteClient;

  public RelayResource() {
    this.objectMapper = new ObjectMapper();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/relays")
  public List<Relay> getAllRelays() {
    return Relay.list("order by sortkey");
  }


  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/relay/{identifier}/switch")
  @Transactional
  public boolean toggleRelay(@PathParam("identifier") String identifier, RelayLogEvent event) throws IOException {
    // FIND CORRESPONDING RELAY
    Relay relay = Relay.findByIdentifier(identifier);
    if (relay == null) {
      return false;
    }
    // TRIGGER REST ENDPOINT ON SATELLITE
    satelliteClient = RestClientBuilder.newBuilder().baseUrl(
        URI.create("http://" + relay.satellite.ip).toURL()
    ).build(SatelliteClient.class);
    Map<String, Boolean> relayState = new HashMap<>();
    relayState.put(relay.identifier, event.getNewValue());
    satelliteClient.updateRelayState(relayState);
    // REFRESH RELAY IN DB
    relay.value = event.getNewValue();
    relay.persist();
    // RELAY LOG REFRESH IN DB AND WEBSOCKET
    List<RelayLog> logs = new ArrayList<>();
    RelayLog log = new RelayLog(relay, event.getInitiator(), new Date(), event.getNewValue());
    log.persist();
    logs.add(log);
    sendToSocket(logs);
    return true;
  }

  private void sendToSocket(List<RelayLog> relayLogs) {
    try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(
        Client.class,
        new URI("ws://localhost:8080/api/socket/relays/system")
    )) {

      session.getAsyncRemote().sendObject(objectMapper.writeValueAsString(relayLogs), result -> {
        if (result.getException() != null) {
          LOGGER.log(Level.ERROR, "Unable to send socket message! " + result.getException());
        }
      });
    } catch (DeploymentException | IOException | URISyntaxException e) {
      LOGGER.log(Level.ERROR, "Unable to send socket message! " + e.getMessage());
    }
  }

  @PUT
  @Path("/relays/{identifier}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public Relay updateRelay(@PathParam("identifier") String identifier, Relay newRelayData) {
    Relay oldRelay = Relay.findByIdentifier(identifier);
    oldRelay.color = newRelayData.color;
    oldRelay.conditionTrigger = newRelayData.conditionTrigger;
    oldRelay.description = newRelayData.description;
    oldRelay.icon = newRelayData.icon;
    oldRelay.identifier = newRelayData.identifier;
    oldRelay.name = newRelayData.name;
    oldRelay.target = newRelayData.target;
    oldRelay.satellite = newRelayData.satellite;
    oldRelay.timeTrigger = newRelayData.timeTrigger;
    oldRelay.value = newRelayData.value;
    oldRelay.persist();
    return oldRelay;
  }

  @GET
  @Path("/relays/{identifier}")
  @Produces(MediaType.APPLICATION_JSON)
  public Relay getRelay(@PathParam("identifier") String identifier) {
    return Relay.findByIdentifier(identifier);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/relays/log")

  public List<RelayLog> getRecentLog() {
    return RelayLog.getRecentLog(NUMBER_OF_LOG_ENTRIES);
  }


  @ClientEndpoint
  public static class Client {

  }
}

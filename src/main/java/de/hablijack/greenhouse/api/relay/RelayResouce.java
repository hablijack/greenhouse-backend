package de.hablijack.greenhouse.api.relay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.api.pojo.RelayLogEvent;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.entity.Satelite;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/backend")
public class RelayResouce {
  private static final int NUMBER_OF_LOG_ENTRIES = 10;
  private final Session session;
  private final ObjectMapper objectMapper;

  public RelayResouce() throws URISyntaxException, DeploymentException, IOException {
    objectMapper = new ObjectMapper();
    this.session = ContainerProvider.getWebSocketContainer().connectToServer(
        Client.class,
        new URI("ws://localhost:8080/backend/relays/socket/system")
    );
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/relays")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<Relay> getAllRelays() {
    return Relay.listAll();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/relay/{identifier}/switch")
  @Transactional
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public boolean toggleRelay(@PathParam("identifier") String identifier, RelayLogEvent event)
      throws JsonProcessingException {
    List<RelayLog> newRelay = new ArrayList<>();
    Relay relay = Relay.findByIdentifier(identifier);
    relay.value = event.getNewValue();
    relay.persist();
    RelayLog log = new RelayLog(relay, event.getInitiator(), new Date(), event.getNewValue());
    log.persist();
    newRelay.add(log);
    session.getAsyncRemote().sendText(objectMapper.writeValueAsString(newRelay));
    return true;
  }

  @PUT
  @Path("/relays/{identifier}")
  @Consumes(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
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
    oldRelay.satelite = newRelayData.satelite;
    oldRelay.timeTrigger = newRelayData.timeTrigger;
    oldRelay.value = newRelayData.value;
    oldRelay.persist();
    return oldRelay;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/relays/log")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<RelayLog> getRecentLog() {
    return RelayLog.getRecentLog(NUMBER_OF_LOG_ENTRIES);
  }


  @ClientEndpoint
  public static class Client {

  }
}

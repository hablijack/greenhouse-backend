package de.hablijack.greenhouse.api.relay;

import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class RelayResouce {

  private static final int NUMBER_OF_LOG_ENTRIES = 10;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/relays")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<Relay> getAllRelays() {
    return Relay.listAll();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/relays/log")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<RelayLog> getRecentLog() {
    return RelayLog.getRecentLog(NUMBER_OF_LOG_ENTRIES);
  }
}

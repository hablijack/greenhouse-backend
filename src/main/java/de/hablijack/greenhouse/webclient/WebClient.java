package de.hablijack.greenhouse.webclient;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface WebClient {
  @GET
  @Path("/{endpoint}")
  @Consumes(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  Uni<Map<String, String>> getByEndpoint(@PathParam("endpoint") String endpoint);
}

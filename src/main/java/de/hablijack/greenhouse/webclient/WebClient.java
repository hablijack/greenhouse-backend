package de.hablijack.greenhouse.webclient;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/")
public interface WebClient {
  @GET
  @Path("/{endpoint}")
  @Consumes(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  Uni<Map<String, String>> getByEndpoint(@PathParam("endpoint") String endpoint);
}

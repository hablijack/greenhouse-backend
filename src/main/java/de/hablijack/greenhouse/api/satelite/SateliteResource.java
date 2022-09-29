package de.hablijack.greenhouse.api.satelite;

import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.Satelite;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class SateliteResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/satelites")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<Relay> getAllSatelites() {
    return Satelite.listAll();
  }

}

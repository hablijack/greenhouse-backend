package de.hablijack.greenhouse.api.satelite;

import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.Satelite;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/satelite/{identifier}")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public Satelite findSateliteByIdentifier(@PathParam("identifier") String identifier) {
    Satelite satelite = Satelite.findByIdentifier(identifier);
    return satelite;
  }

  @POST
  @Path("/satelites")
  @Consumes(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  @Transactional
  public Satelite createSatelite(Satelite satelite) {
    satelite.persist();
    return satelite;
  }

  @PUT
  @Path("/satelite/{identifier}")
  @Consumes(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  @Transactional
  public Satelite createSatelite(@PathParam("identifier") String identifier, Satelite newSateliteData) {
    Satelite oldSatelite = Satelite.findByIdentifier(identifier);
    oldSatelite.ip = newSateliteData.ip;
    oldSatelite.online = newSateliteData.online;
    oldSatelite.description = newSateliteData.description;
    oldSatelite.name = newSateliteData.name;
    oldSatelite.imageUrl = newSateliteData.imageUrl;
    oldSatelite.persist();
    return oldSatelite;
  }
}

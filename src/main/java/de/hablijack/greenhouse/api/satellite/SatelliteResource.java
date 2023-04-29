package de.hablijack.greenhouse.api.satellite;

import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.Satellite;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/backend")
public class SatelliteResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/satellites")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<Relay> getAllSatellites() {
    return Satellite.listAll();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/satellite/{identifier}")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public Satellite findSatelliteByIdentifier(@PathParam("identifier") String identifier) {
    Satellite satellite = Satellite.findByIdentifier(identifier);
    return satellite;
  }

  @POST
  @Path("/satellites")
  @Consumes(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  @Transactional
  public Satellite createSatellite(Satellite satellite) {
    satellite.persist();
    return satellite;
  }

  @PUT
  @Path("/satellite/{identifier}")
  @Consumes(MediaType.APPLICATION_JSON)
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  @Transactional
  public Satellite createSatellite(@PathParam("identifier") String identifier, Satellite newSatelliteData) {
    Satellite oldSatellite = Satellite.findByIdentifier(identifier);
    oldSatellite.ip = newSatelliteData.ip;
    oldSatellite.online = newSatelliteData.online;
    oldSatellite.description = newSatelliteData.description;
    oldSatellite.name = newSatelliteData.name;
    oldSatellite.imageUrl = newSatelliteData.imageUrl;
    oldSatellite.persist();
    return oldSatellite;
  }
}

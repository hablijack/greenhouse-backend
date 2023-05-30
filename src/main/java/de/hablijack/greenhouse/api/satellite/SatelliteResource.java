package de.hablijack.greenhouse.api.satellite;

import de.hablijack.greenhouse.entity.CameraPicture;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/backend")
public class SatelliteResource {
  private static final Logger LOGGER = Logger.getLogger(SatelliteResource.class.getName());

  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  SatelliteService satelliteService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/satellites")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<Relay> getAllSatellites() {
    return Satellite.listAll();
  }

  @GET
  @Produces("image/jpg")
  @Path("/satellites/greenhouse-cam/picture.jpg")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  @Transactional
  public Response getCurrentPicture() {
    if (CameraPicture.findAll().list().size() > 0) {
      CameraPicture picture = (CameraPicture) CameraPicture.findAll().list().get(0);
      Response.ResponseBuilder response = Response.ok(picture.imageByte);
      response.header("Content-Disposition", "inline; filename=\"picture.jpg\"");
      return response.build();
    } else {
      Response.ResponseBuilder response = Response.serverError();
      return response.build();
    }
  }

  @GET
  @Produces("text/plain")
  @Path("/satellites/greenhouse-cam/snapshot")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  @Transactional
  public Response takeSnapshot() throws InterruptedException {
    boolean success = satelliteService.takeCameraSnapshot();
    Response.ResponseBuilder response = Response.ok();
    if (!success) {
      response = Response.serverError();
      response.entity("Could not take snapshot from camera!");
    } else {
      TimeUnit.SECONDS.sleep(SatelliteService.CAMERA_SNAPSHOT_WAIT_TIME);
      success = satelliteService.savePictureToDatabase();
      if (!success) {
        response = Response.serverError();
        response.entity("Could not persist snapshot in database!");
      }
    }
    return response.build();
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

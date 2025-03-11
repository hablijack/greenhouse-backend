package de.hablijack.greenhouse.api.satellite;

import de.hablijack.greenhouse.entity.CameraPicture;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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
  
  public List<Relay> getAllSatellites() {
    return Satellite.listAll();
  }

  @GET
  @Produces("image/jpg")
  @Path("/satellites/greenhouse-cam/picture.jpg")
  
  @Transactional
  public Response getCurrentPicture() {
    if (!CameraPicture.findAll().list().isEmpty()) {
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
  
  @Transactional
  public Response takeSnapshot() throws InterruptedException, IOException, URISyntaxException {
    satelliteService.takeCameraSnapshot();
    Response.ResponseBuilder response = Response.ok();
    TimeUnit.SECONDS.sleep(SatelliteService.CAMERA_SNAPSHOT_WAIT_TIME);
    satelliteService.savePictureToDatabase();
    return response.build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/satellite/{identifier}")
  
  public Satellite findSatelliteByIdentifier(@PathParam("identifier") String identifier) {
    return Satellite.findByIdentifier(identifier);
  }

  @POST
  @Path("/satellites")
  @Consumes(MediaType.APPLICATION_JSON)
  
  @Transactional
  public Satellite createSatellite(Satellite satellite) {
    satellite.persist();
    return satellite;
  }

  @PUT
  @Path("/satellite/{identifier}")
  @Consumes(MediaType.APPLICATION_JSON)
  
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

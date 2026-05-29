package de.hablijack.greenhouse.ai.api;

import de.hablijack.greenhouse.ai.api.dto.ErrorResponse;
import de.hablijack.greenhouse.ai.api.dto.PlantRequest;
import de.hablijack.greenhouse.ai.api.dto.PlantResponse;
import de.hablijack.greenhouse.ai.service.PlantService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/rest")
public class PlantResource {

  private static final Logger LOG = LoggerFactory.getLogger(PlantResource.class);

  @Inject
  PlantService plantService;

  @GET
  @Path("/plants")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PlantResponse> getAllPlants() {
    LOG.info("GET /plants");
    return plantService.getAllPlants();
  }

  @GET
  @Path("/plant/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPlant(@PathParam("id") Long id) {
    LOG.info("GET /plant/{}", id);
    try {
      PlantResponse plant = plantService.getPlant(id);
      return Response.ok(plant).build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("not_found", e.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
          .build();
    }
  }

  @POST
  @Path("/plants")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createPlant(@Valid PlantRequest request) {
    LOG.info("POST /plants name: {}", request.name);
    PlantResponse plant = plantService.createPlant(request);
    return Response.ok(plant).build();
  }

  @DELETE
  @Path("/plant/{id}")
  public Response deletePlant(@PathParam("id") Long id) {
    LOG.info("DELETE /plant/{}", id);
    try {
      plantService.deletePlant(id);
      return Response.noContent().build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("not_found", e.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
          .build();
    }
  }
}

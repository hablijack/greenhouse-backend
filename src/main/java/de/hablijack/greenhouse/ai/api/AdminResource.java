package de.hablijack.greenhouse.ai.api;

import de.hablijack.greenhouse.ai.api.dto.ErrorResponse;
import de.hablijack.greenhouse.ai.lifecycle.RagDataInitializer;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/rest/admin")
public class AdminResource {

  private static final Logger LOG = LoggerFactory.getLogger(AdminResource.class);

  @Inject
  RagDataInitializer ragDataInitializer;

  @POST
  @Path("/reimport-vectors")
  @Produces(MediaType.APPLICATION_JSON)
  public Response reimportVectors() {
    LOG.info("POST /admin/reimport-vectors - starting RAG vector reimport");
    try {
      ragDataInitializer.reimportAll();
      LOG.info("RAG vector reimport completed successfully");
      return Response.ok(Map.of("status", "success",
          "message", "RAG vectors reimported successfully")).build();
    } catch (Exception e) {
      LOG.error("Failed to reimport RAG vectors", e);
      return Response.serverError()
          .entity(new ErrorResponse("reimport_error",
              "Failed to reimport RAG vectors: " + e.getMessage(),
              Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
          .build();
    }
  }
}

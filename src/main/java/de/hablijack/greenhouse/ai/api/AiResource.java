package de.hablijack.greenhouse.ai.api;

import de.hablijack.greenhouse.ai.api.dto.AiRecommendationResponse;
import de.hablijack.greenhouse.ai.api.dto.AskAiRequest;
import de.hablijack.greenhouse.ai.api.dto.BatchSensorDataResponse;
import de.hablijack.greenhouse.ai.api.dto.ErrorResponse;
import de.hablijack.greenhouse.ai.api.dto.SensorDataRequest;
import de.hablijack.greenhouse.ai.llm.LlmException;
import de.hablijack.greenhouse.ai.service.AiService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/rest")
public class AiResource {

  private static final Logger LOG = LoggerFactory.getLogger(AiResource.class);

  @Inject
  AiService aiService;

  @POST
  @Path("/sensor-data")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response analyzeSensorData(@Valid SensorDataRequest request) {
    LOG.info("POST /sensor-data for plant: {}", request.plantType);
    try {
      AiRecommendationResponse response = aiService.analyzeSensorData(request);
      return Response.ok(response).build();
    } catch (LlmException e) {
      LOG.warn("LLM unavailable, using local analysis: {}", e.getMessage());
      AiRecommendationResponse fallback = aiService.analyzeWithLocalOnly(request);
      return Response.ok(fallback).build();
    } catch (Exception e) {
      LOG.error("Error analyzing sensor data", e);
      return Response.serverError()
          .entity(new ErrorResponse("analysis_error",
          "Failed to analyze sensor data: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
          .build();
    }
  }

  @POST
  @Path("/sensor-data/batch")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response analyzeSensorDataBatch(List<SensorDataRequest> requests) {
    LOG.info("POST /sensor-data/batch for {} plants", requests.size());
    try {
      Map<String, AiRecommendationResponse> results = aiService.analyzeBatch(requests);
      return Response.ok(new BatchSensorDataResponse(results)).build();
    } catch (Exception e) {
      LOG.error("Error analyzing batch sensor data", e);
      return Response.serverError()
          .entity(new ErrorResponse("analysis_error",
          "Failed to analyze sensor data: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
          .build();
    }
  }

  @POST
  @Path("/ask-ai")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response askAi(@Valid AskAiRequest request) {
    LOG.info("POST /ask-ai question: {}", request.question);
    try {
      String answer = aiService.askQuestion(request);
      return Response.ok(java.util.Map.of("answer", answer, "question", request.question)).build();
    } catch (LlmException e) {
      LOG.error("LLM unavailable for question", e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new ErrorResponse("llm_unavailable",
              "AI service is currently unavailable: " + e.getMessage(),
              Response.Status.SERVICE_UNAVAILABLE.getStatusCode()))
          .build();
    } catch (Exception e) {
      LOG.error("Error processing AI question", e);
      return Response.serverError()
          .entity(new ErrorResponse("ai_error",
          "Failed to process question: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
          .build();
    }
  }
}

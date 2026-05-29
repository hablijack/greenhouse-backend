package de.hablijack.greenhouse.ai.api;

import de.hablijack.greenhouse.ai.api.dto.ErrorResponse;
import de.hablijack.greenhouse.ai.llm.LlmException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class AiExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOG = LoggerFactory.getLogger(AiExceptionMapper.class);

  @Override
  public Response toResponse(Exception exception) {
    LOG.error("Unhandled exception", exception);

    if (exception instanceof LlmException) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(new ErrorResponse("llm_error", exception.getMessage(),
              Response.Status.SERVICE_UNAVAILABLE.getStatusCode()))
          .build();
    }

    if (exception instanceof ConstraintViolationException cve) {
      String message = cve.getConstraintViolations().stream()
          .map(ConstraintViolation::getMessage)
          .collect(Collectors.joining(", "));
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(new ErrorResponse("validation_error", message, Response.Status.BAD_REQUEST.getStatusCode()))
          .build();
    }

    if (exception instanceof jakarta.ws.rs.NotFoundException) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new ErrorResponse("not_found", exception.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
          .build();
    }

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(new ErrorResponse("internal_error",
        "An unexpected error occurred", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
        .build();
  }
}

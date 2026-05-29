package de.hablijack.greenhouse.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class ErrorResponse {

  @JsonProperty("error")
  public String error;

  @JsonProperty("message")
  public String message;

  @JsonProperty("status")
  public int status;

  public ErrorResponse() {
  }

  public ErrorResponse(String error, String message, int status) {
    this.error = error;
    this.message = message;
    this.status = status;
  }
}

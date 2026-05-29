package de.hablijack.greenhouse.ai.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public class AskAiRequest {

  @NotBlank
  public String question;

  public String plantType;

  public AskAiRequest() {
  }

  public AskAiRequest(String question, String plantType) {
    this.question = question;
    this.plantType = plantType;
  }
}

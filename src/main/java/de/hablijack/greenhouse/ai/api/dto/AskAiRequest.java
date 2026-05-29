package de.hablijack.greenhouse.ai.api.dto;

import jakarta.validation.constraints.NotBlank;

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

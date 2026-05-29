package de.hablijack.greenhouse.ai.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;

@RegisterForReflection
public class PlantRequest {

  @NotBlank
  public String name;

  public String description;

  public PlantRequest() {
  }

  public PlantRequest(String name, String description) {
    this.name = name;
    this.description = description;
  }
}

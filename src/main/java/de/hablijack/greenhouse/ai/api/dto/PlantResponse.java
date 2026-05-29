package de.hablijack.greenhouse.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class PlantResponse {

  @JsonProperty("id")
  public Long id;

  @JsonProperty("name")
  public String name;

  @JsonProperty("description")
  public String description;

  public PlantResponse() {
  }

  public PlantResponse(Long id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }
}

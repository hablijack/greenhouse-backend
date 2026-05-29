package de.hablijack.greenhouse.ai.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EmbeddingRequest {

  @JsonProperty("model")
  public String model;

  @JsonProperty("input")
  public List<String> input;

  public EmbeddingRequest() {
  }

  public EmbeddingRequest(String model, List<String> input) {
    this.model = model;
    this.input = input;
  }

  public static EmbeddingRequest single(String model, String text) {
    return new EmbeddingRequest(model, List.of(text));
  }
}

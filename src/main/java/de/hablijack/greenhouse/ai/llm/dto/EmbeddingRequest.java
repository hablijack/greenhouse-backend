package de.hablijack.greenhouse.ai.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
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

package de.hablijack.greenhouse.ai.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public class EmbeddingResponse {

  @JsonProperty("object")
  public String object;

  @JsonProperty("data")
  public List<EmbeddingData> data;

  @JsonProperty("model")
  public String model;

  @JsonProperty("usage")
  public Usage usage;

  @RegisterForReflection
  public static class EmbeddingData {
    @JsonProperty("object")
    public String object;

    @JsonProperty("index")
    public Integer index;

    @JsonProperty("embedding")
    public List<Double> embedding;
  }

  @RegisterForReflection
  public static class Usage {
    @JsonProperty("prompt_tokens")
    public Integer promptTokens;

    @JsonProperty("total_tokens")
    public Integer totalTokens;
  }

  public float[] getEmbedding() {
    if (data != null && !data.isEmpty() && data.get(0).embedding != null) {
      List<Double> emb = data.get(0).embedding;
      float[] result = new float[emb.size()];
      for (int i = 0; i < emb.size(); i++) {
        result[i] = emb.get(i).floatValue();
      }
      return result;
    }
    return new float[0];
  }
}

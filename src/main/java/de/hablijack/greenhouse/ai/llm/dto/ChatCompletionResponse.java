package de.hablijack.greenhouse.ai.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public class ChatCompletionResponse {

  @JsonProperty("id")
  public String id;

  @JsonProperty("object")
  public String object;

  @JsonProperty("created")
  public Long created;

  @JsonProperty("model")
  public String model;

  @JsonProperty("choices")
  public List<Choice> choices;

  @JsonProperty("usage")
  public Usage usage;

  @RegisterForReflection
  public static class Choice {
    @JsonProperty("index")
    public Integer index;

    @JsonProperty("message")
    public ChatMessage message;

    @JsonProperty("finish_reason")
    public String finishReason;
  }

  @RegisterForReflection
  public static class Usage {
    @JsonProperty("prompt_tokens")
    public Integer promptTokens;

    @JsonProperty("completion_tokens")
    public Integer completionTokens;

    @JsonProperty("total_tokens")
    public Integer totalTokens;
  }

  public String getContent() {
    if (choices != null && !choices.isEmpty() && choices.get(0).message != null) {
      return choices.get(0).message.content;
    }
    return null;
  }
}

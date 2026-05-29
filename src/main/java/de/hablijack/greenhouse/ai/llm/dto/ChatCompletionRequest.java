package de.hablijack.greenhouse.ai.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ChatCompletionRequest {

  @JsonProperty("model")
  public String model;

  @JsonProperty("messages")
  public List<ChatMessage> messages;

  @JsonProperty("temperature")
  public Double temperature;

  @JsonProperty("max_tokens")
  public Integer maxTokens;

  @JsonProperty("stream")
  public Boolean stream;

  @JsonProperty("response_format")
  public ResponseFormat responseFormat;

  public ChatCompletionRequest() {
  }

  public ChatCompletionRequest(String model, List<ChatMessage> messages) {
    this.model = model;
    this.messages = messages;
  }

  public static class ResponseFormat {
    @JsonProperty("type")
    public String type;

    public ResponseFormat() {
    }

    public ResponseFormat(String type) {
      this.type = type;
    }

    public static ResponseFormat json() {
      return new ResponseFormat("json_object");
    }
  }
}

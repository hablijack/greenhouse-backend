package de.hablijack.greenhouse.ai.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatMessage {

  @JsonProperty("role")
  public String role;

  @JsonProperty("content")
  public String content;

  public ChatMessage() {
  }

  public ChatMessage(String role, String content) {
    this.role = role;
    this.content = content;
  }

  public static ChatMessage system(String content) {
    return new ChatMessage("system", content);
  }

  public static ChatMessage user(String content) {
    return new ChatMessage("user", content);
  }

  public static ChatMessage assistant(String content) {
    return new ChatMessage("assistant", content);
  }
}

package de.hablijack.greenhouse.ai.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
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

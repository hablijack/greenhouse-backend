package de.hablijack.greenhouse.ai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.ai.config.AiConfig;
import de.hablijack.greenhouse.ai.llm.dto.ChatCompletionRequest;
import de.hablijack.greenhouse.ai.llm.dto.ChatCompletionResponse;
import de.hablijack.greenhouse.ai.llm.dto.ChatMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LlmService {

  private static final Logger LOG = LoggerFactory.getLogger(LlmService.class);

  private static final double DEFAULT_TEMPERATURE = 0.3;
  private static final int DEFAULT_MAX_TOKENS = 4096;
  private static final int JSON_CODE_FENCE_PREFIX_LENGTH = 7;
  private static final int CODE_FENCE_PREFIX_LENGTH = 3;
  private static final long MILLIS_PER_SECOND = 1000L;
  private static final int LOG_TRUNCATE_LENGTH = 500;
  private static final int LOG_JSON_TRUNCATE_LENGTH = 1000;

  private final LlmClient llmClient;
  private final AiConfig config;
  private final ObjectMapper objectMapper;

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public LlmService(LlmClient llmClient, AiConfig config, ObjectMapper objectMapper) {
    this.llmClient = llmClient;
    this.config = config;
    this.objectMapper = objectMapper;
  }

  public String chat(String systemPrompt, String userPrompt) {
    return chat(systemPrompt, userPrompt, false);
  }

  public String chat(String systemPrompt, String userPrompt, boolean expectJson) {
    LOG.info("LLM call: expectJson={}, systemPromptLen={}, userPromptLen={}",
        expectJson, systemPrompt.length(), userPrompt.length());
    LOG.debug("LLM system prompt: {}", truncate(systemPrompt, LOG_TRUNCATE_LENGTH));
    LOG.debug("LLM user prompt: {}", truncate(userPrompt, LOG_TRUNCATE_LENGTH));

    ChatCompletionRequest request = new ChatCompletionRequest();
    request.messages = List.of(
        ChatMessage.system(systemPrompt),
        ChatMessage.user(userPrompt));
    request.temperature = DEFAULT_TEMPERATURE;
    request.maxTokens = DEFAULT_MAX_TOKENS;

    if (expectJson) {
      request.responseFormat = ChatCompletionRequest.ResponseFormat.json();
    }

    return executeWithRetry(request, 0);
  }

  public <T> T chatAsJson(String systemPrompt, String userPrompt, Class<T> responseType) {
    String json = chat(systemPrompt, userPrompt, true);
    try {
      String cleaned = json;
      if (json.startsWith("```json")) {
        cleaned = json.substring(JSON_CODE_FENCE_PREFIX_LENGTH, json.lastIndexOf("```")).trim();
      } else if (json.startsWith("```")) {
        cleaned = json.substring(CODE_FENCE_PREFIX_LENGTH, json.lastIndexOf("```")).trim();
      }
      cleaned = sanitizeJson(cleaned);
      LOG.debug("Raw LLM response ({} chars), cleaned JSON: {}",
          json.length(), truncate(cleaned, LOG_JSON_TRUNCATE_LENGTH));
      return objectMapper.readValue(cleaned, responseType);
    } catch (Exception e) {
      LOG.error("Failed to parse LLM response as {}: {}", responseType.getSimpleName(), json, e);
      throw new LlmException("Failed to parse LLM response: " + e.getMessage(), e);
    }
  }

  public float[] generateEmbedding(String text) {
    LOG.info("Generating embedding for text ({} chars)", text.length());
    return executeEmbeddingWithRetry(text, 0);
  }

  private static String truncate(String s, int maxLen) {
    if (s == null) {
      return null;
    }
    if (s.length() <= maxLen) {
      return s;
    }
    return s.substring(0, maxLen) + "...";
  }

  private String sanitizeJson(String json) {
    StringBuilder sb = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);
      if (c == '\\' && inQuotes) {
        sb.append(c);
        if (i + 1 < json.length()) {
          i++;
          sb.append(json.charAt(i));
        }
      } else if (c == '\"') {
        inQuotes = !inQuotes;
        sb.append(c);
      } else if (c == '\n' && inQuotes) {
        sb.append('\\');
        sb.append('n');
      } else if (c == '\r' && inQuotes) {
        sb.append('\\');
        sb.append('n');
      } else if (c == '\t' && inQuotes) {
        sb.append('\\');
        sb.append('t');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private String executeWithRetry(ChatCompletionRequest request, int attempt) {
    try {
      ChatCompletionResponse response = llmClient.chat(request);
      String content = response.getContent();
      if (content == null || content.isBlank()) {
        throw new LlmException("LLM returned empty response");
      }
      LOG.info("LLM response received ({} chars, attempt {})", content.length(), attempt + 1);
      LOG.debug("LLM response content: {}", truncate(content, LOG_JSON_TRUNCATE_LENGTH));
      return content;
    } catch (Exception e) {
      if (attempt < config.llm().maxRetries()) {
        long backoff = (long) Math.pow(2, attempt) * MILLIS_PER_SECOND;
        LOG.warn("LLM call failed (attempt {}), retrying in {}ms: {}",
            attempt + 1, backoff, e.getMessage());
        try {
          Thread.sleep(backoff);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new LlmException("Retry interrupted", ie);
        }
        return executeWithRetry(request, attempt + 1);
      }
      throw new LlmException("LLM call failed after " + config.llm().maxRetries()
          + " retries: " + e.getMessage(), e);
    }
  }

  private float[] executeEmbeddingWithRetry(String text, int attempt) {
    try {
      return llmClient.embed(text);
    } catch (Exception e) {
      if (attempt < config.llm().maxRetries()) {
        long backoff = (long) Math.pow(2, attempt) * MILLIS_PER_SECOND;
        LOG.warn("Embedding call failed (attempt {}), retrying in {}ms: {}",
            attempt + 1, backoff, e.getMessage());
        try {
          Thread.sleep(backoff);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new LlmException("Retry interrupted", ie);
        }
        return executeEmbeddingWithRetry(text, attempt + 1);
      }
      throw new LlmException("Embedding failed after " + config.llm().maxRetries()
          + " retries: " + e.getMessage(), e);
    }
  }
}

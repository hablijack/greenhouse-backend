package de.hablijack.greenhouse.ai.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.ai.config.AiConfig;
import de.hablijack.greenhouse.ai.llm.dto.ChatCompletionRequest;
import de.hablijack.greenhouse.ai.llm.dto.ChatCompletionResponse;
import de.hablijack.greenhouse.ai.llm.dto.EmbeddingRequest;
import de.hablijack.greenhouse.ai.llm.dto.EmbeddingResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LlmClient {

  private static final Logger LOG = LoggerFactory.getLogger(LlmClient.class);

  private final HttpClient httpClient;
  private final AiConfig config;
  private final ObjectMapper objectMapper;

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public LlmClient(AiConfig config, ObjectMapper objectMapper) {
    this.config = config;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(config.llm().timeout()))
        .build();
  }

  public ChatCompletionResponse chat(ChatCompletionRequest request) {
    request.model = config.llm().chatModel();
    request.stream = false;
    try {
      String json = objectMapper.writeValueAsString(request);
      LOG.debug("Sending chat request to LLM: model={}, messages={}",
          request.model, request.messages.size());

      HttpRequest httpRequest = HttpRequest.newBuilder()
          .uri(URI.create(config.llm().baseUrl() + "/v1/chat/completions"))
          .header("Content-Type", "application/json")
          .timeout(Duration.ofMillis(config.llm().timeout()))
          .POST(HttpRequest.BodyPublishers.ofString(json))
          .build();

      HttpResponse<String> response = httpClient.send(httpRequest,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != HttpURLConnection.HTTP_OK) {
        throw new LlmException("LLM returned status " + response.statusCode()
            + ": " + response.body());
      }

      return objectMapper.readValue(response.body(), ChatCompletionResponse.class);
    } catch (LlmException e) {
      throw e;
    } catch (Exception e) {
      throw new LlmException("Failed to communicate with LLM: " + e.getMessage(), e);
    }
  }

  public float[] embed(String text) {
    EmbeddingRequest request = EmbeddingRequest.single(config.llm().embeddingModel(), text);
    try {
      String json = objectMapper.writeValueAsString(request);
      LOG.debug("Sending embedding request for text length={}", text.length());

      HttpRequest httpRequest = HttpRequest.newBuilder()
          .uri(URI.create(config.llm().baseUrl() + "/v1/embeddings"))
          .header("Content-Type", "application/json")
          .timeout(Duration.ofMillis(config.llm().timeout()))
          .POST(HttpRequest.BodyPublishers.ofString(json))
          .build();

      HttpResponse<String> response = httpClient.send(httpRequest,
          HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != HttpURLConnection.HTTP_OK) {
        throw new LlmException("Embedding endpoint returned status "
            + response.statusCode() + ": " + response.body());
      }

      EmbeddingResponse embeddingResponse = objectMapper.readValue(
          response.body(), EmbeddingResponse.class);
      return embeddingResponse.getEmbedding();
    } catch (LlmException e) {
      throw e;
    } catch (Exception e) {
      throw new LlmException("Failed to generate embedding: " + e.getMessage(), e);
    }
  }
}

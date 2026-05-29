package de.hablijack.greenhouse.ai.rag.service;

import de.hablijack.greenhouse.ai.config.AiConfig;
import de.hablijack.greenhouse.ai.llm.LlmService;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EmbeddingService {

  private static final Logger LOG = LoggerFactory.getLogger(EmbeddingService.class);

  private static final int MAX_INPUT_LENGTH = 8192;

  private final LlmService llmService;
  private final AiConfig config;

  public EmbeddingService(LlmService llmService, AiConfig config) {
    this.llmService = llmService;
    this.config = config;
  }

  public float[] generateEmbedding(String text) {
    String cleaned = text.trim().replaceAll("\\s+", " ");
    int maxLength = Math.min(cleaned.length(), MAX_INPUT_LENGTH);
    cleaned = cleaned.substring(0, maxLength);
    LOG.debug("Generating embedding for text length={}", cleaned.length());
    return llmService.generateEmbedding(cleaned);
  }

  public float[] generateEmbeddingForDocument(String plantType, String title,
      String content, String category) {
    String text = String.format("Plant: %s. Title: %s. Category: %s. Content: %s",
        plantType,
        title != null ? title : "",
        category != null ? category : "",
        content);
    return generateEmbedding(text);
  }
}

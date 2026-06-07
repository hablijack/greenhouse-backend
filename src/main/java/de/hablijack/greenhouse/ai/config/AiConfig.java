package de.hablijack.greenhouse.ai.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "ai")
public interface AiConfig {

  LlmConfig llm();

  RagConfig rag();

  ShadowConfig shadow();

  interface ShadowConfig {
    @WithDefault("false")
    boolean enabled();
  }

  interface LlmConfig {
    @WithName("base-url")
    String baseUrl();

    @WithDefault("60000")
    int timeout();

    @WithDefault("3")
    @WithName("max-retries")
    int maxRetries();

    @WithDefault("gemma-4-e2b-it-q4_k_m.gguf")
    @WithName("chat-model")
    String chatModel();

    @WithDefault("bge-small-en-v1.5")
    @WithName("embedding-model")
    String embeddingModel();

    @WithDefault("384")
    @WithName("embedding-dimension")
    int embeddingDimension();

    @WithName("embedding-base-url")
    String embeddingBaseUrl();
  }

  interface RagConfig {
    @WithDefault("true")
    boolean enabled();

    @WithDefault("5")
    @WithName("max-documents")
    int maxDocuments();

    @WithDefault("0.5")
    @WithName("similarity-threshold")
    double similarityThreshold();
  }
}

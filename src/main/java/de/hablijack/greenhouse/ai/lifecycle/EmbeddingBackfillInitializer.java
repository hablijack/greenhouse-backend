package de.hablijack.greenhouse.ai.lifecycle;

import de.hablijack.greenhouse.ai.rag.entity.PlantKnowledgeDocument;
import de.hablijack.greenhouse.ai.rag.service.EmbeddingService;
import de.hablijack.greenhouse.ai.rag.service.VectorSearchService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
public class EmbeddingBackfillInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(EmbeddingBackfillInitializer.class);

  private final EmbeddingService embeddingService;
  private final VectorSearchService vectorSearchService;

  @Inject
  EmbeddingBackfillInitializer self;

  public EmbeddingBackfillInitializer(EmbeddingService embeddingService,
      VectorSearchService vectorSearchService) {
    this.embeddingService = embeddingService;
    this.vectorSearchService = vectorSearchService;
  }

  @PostConstruct
  void init() {
    Thread.startVirtualThread(() -> self.backfillMissingEmbeddings());
  }

  @Transactional
  void backfillMissingEmbeddings() {
    try {
      long missingCount = PlantKnowledgeDocument.count("embedding IS NULL");
      if (missingCount == 0) {
        LOG.info("All documents have embeddings, no backfill needed");
        return;
      }

      LOG.info("Found {} documents without embeddings, starting backfill", missingCount);

      List<PlantKnowledgeDocument> docs = PlantKnowledgeDocument.list("embedding IS NULL");
      int successCount = 0;
      for (PlantKnowledgeDocument doc : docs) {
        try {
          float[] embedding = embeddingService.generateEmbeddingForDocument(
              doc.plantType, doc.title, doc.content, doc.category);
          vectorSearchService.updateEmbedding(doc.id, embedding);
          successCount++;
          if (successCount % 10 == 0) {
            LOG.info("Backfilled {}/{} embeddings", successCount, missingCount);
          }
        } catch (Exception e) {
          LOG.warn("Failed to generate embedding for document {} ({}): {}",
              doc.id, doc.title, e.getMessage());
        }
      }

      LOG.info("Embedding backfill complete: {}/{} documents processed", successCount, missingCount);
    } catch (Exception e) {
      LOG.error("Failed to backfill embeddings: {}", e.getMessage(), e);
    }
  }
}

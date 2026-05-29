package de.hablijack.greenhouse.ai.rag.service;

import de.hablijack.greenhouse.ai.rag.entity.PlantKnowledgeDocument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DocumentIngestionService {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentIngestionService.class);

  private final EmbeddingService embeddingService;
  private final VectorSearchService vectorSearchService;

  public DocumentIngestionService(EmbeddingService embeddingService,
      VectorSearchService vectorSearchService) {
    this.embeddingService = embeddingService;
    this.vectorSearchService = vectorSearchService;
  }

  @Transactional
  public PlantKnowledgeDocument ingestDocument(String plantType, String title,
      String content, String category) {
    PlantKnowledgeDocument doc = new PlantKnowledgeDocument(plantType, title, content, category);
    doc.persist();

    try {
      float[] embedding = embeddingService.generateEmbeddingForDocument(
          plantType, title, content, category);
      vectorSearchService.updateEmbedding(doc.id, embedding);
      LOG.info("Ingested document '{}' for plant type '{}' with embedding (id={})",
          title, plantType, doc.id);
    } catch (Exception e) {
      LOG.warn("Failed to generate embedding for document '{}': {}. "
          + "Document saved without embedding.", title, e.getMessage());
    }

    return doc;
  }

  @Transactional
  public void ingestBatch(List<DocumentInput> documents) {
    for (DocumentInput input : documents) {
      try {
        ingestDocument(input.plantType, input.title, input.content, input.category);
      } catch (Exception e) {
        LOG.error("Failed to ingest document '{}': {}", input.title, e.getMessage());
      }
    }
  }

  public static class DocumentInput {
    public final String plantType;
    public final String title;
    public final String content;
    public final String category;

    public DocumentInput(String plantType, String title, String content, String category) {
      this.plantType = plantType;
      this.title = title;
      this.content = content;
      this.category = category;
    }
  }
}

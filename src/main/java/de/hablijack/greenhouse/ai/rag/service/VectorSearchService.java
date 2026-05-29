package de.hablijack.greenhouse.ai.rag.service;

import de.hablijack.greenhouse.ai.config.AiConfig;
import de.hablijack.greenhouse.ai.rag.entity.PlantKnowledgeDocument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VectorSearchService {

  private static final Logger LOG = LoggerFactory.getLogger(VectorSearchService.class);

  private static final int PARAM_INDEX_MAX_RESULTS = 3;

  @PersistenceContext
  EntityManager entityManager;

  private final EmbeddingService embeddingService;
  private final AiConfig config;

  public VectorSearchService(EmbeddingService embeddingService, AiConfig config) {
    this.embeddingService = embeddingService;
    this.config = config;
  }

  VectorSearchService() {
    this.embeddingService = null;
    this.config = null;
  }

  public List<PlantKnowledgeDocument> findSimilarDocuments(String query, int maxResults) {
    float[] queryEmbedding = embeddingService.generateEmbedding(query);
    return findByVectorSimilarity(queryEmbedding, maxResults);
  }

  public List<PlantKnowledgeDocument> findSimilarDocuments(String query, String plantType,
      int maxResults) {
    float[] queryEmbedding = embeddingService.generateEmbedding(query);
    return findByVectorSimilarity(queryEmbedding, plantType, maxResults);
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public List<PlantKnowledgeDocument> findByVectorSimilarity(float[] queryVector,
      int maxResults) {
    String vectorLiteral = arrayToPgVector(queryVector);
    String sql = "SELECT id, plant_type, title, content, category, created_at, updated_at "
        + "FROM greenhouse.plant_knowledge_document "
        + "ORDER BY embedding <=> ?1::vector "
        + "LIMIT ?2";

    var query = entityManager.createNativeQuery(sql, PlantKnowledgeDocument.class);
    query.setParameter(1, vectorLiteral);
    query.setParameter(2, maxResults);
    return query.getResultList();
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public List<PlantKnowledgeDocument> findByVectorSimilarity(float[] queryVector,
      String plantType,
      int maxResults) {
    String vectorLiteral = arrayToPgVector(queryVector);
    String sql = "SELECT id, plant_type, title, content, category, created_at, updated_at "
        + "FROM greenhouse.plant_knowledge_document "
        + "WHERE plant_type = ?1 "
        + "ORDER BY embedding <=> ?2::vector "
        + "LIMIT ?3";

    var query = entityManager.createNativeQuery(sql, PlantKnowledgeDocument.class);
    query.setParameter(1, plantType);
    query.setParameter(2, vectorLiteral);
    query.setParameter(PARAM_INDEX_MAX_RESULTS, maxResults);
    return query.getResultList();
  }

  @Transactional
  public void updateEmbedding(Long documentId, float[] embedding) {
    String vectorLiteral = arrayToPgVector(embedding);
    String sql = "UPDATE greenhouse.plant_knowledge_document "
        + "SET embedding = ?1::vector, updated_at = NOW() "
        + "WHERE id = ?2";

    entityManager.createNativeQuery(sql)
        .setParameter(1, vectorLiteral)
        .setParameter(2, documentId)
        .executeUpdate();
  }

  private String arrayToPgVector(float[] array) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(array[i]);
    }
    sb.append("]");
    return sb.toString();
  }
}

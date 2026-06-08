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
  private static final int COL_CONTENT = 3;
  private static final int COL_CATEGORY = 4;
  private static final int COL_CREATED_AT = 5;
  private static final int COL_UPDATED_AT = 6;

  @PersistenceContext
  EntityManager entityManager;

  private final EmbeddingService embeddingService;
  private final AiConfig config;

  public VectorSearchService(EmbeddingService embeddingService, AiConfig config) {
    this.embeddingService = embeddingService;
    this.config = config;
  }

  @Transactional
  public List<PlantKnowledgeDocument> findSimilarDocuments(String query, int maxResults) {
    try {
      float[] queryEmbedding = embeddingService.generateEmbedding(query);
      return findByVectorSimilarity(queryEmbedding, maxResults);
    } catch (Exception e) {
      LOG.error("Vector search failed for query: {}", query, e);
      return List.of();
    }
  }

  @Transactional
  public List<PlantKnowledgeDocument> findSimilarDocuments(String query, String plantType,
      int maxResults) {
    try {
      float[] queryEmbedding = embeddingService.generateEmbedding(query);
      return findByVectorSimilarity(queryEmbedding, plantType, maxResults);
    } catch (Exception e) {
      LOG.error("Vector search failed for query (plantType={}): {}", plantType, query, e);
      return List.of();
    }
  }

  @SuppressWarnings("unchecked")
  public List<PlantKnowledgeDocument> findByVectorSimilarity(float[] queryVector,
      int maxResults) {
    String vectorLiteral = arrayToPgVector(queryVector);
    String sql = "SELECT id, plant_type, title, content, category, created_at, updated_at "
        + "FROM greenhouse.plant_knowledge_document "
        + "ORDER BY embedding <=> CAST(?1 AS vector) "
        + "LIMIT ?2";

    var query = entityManager.createNativeQuery(sql);
    query.setParameter(1, vectorLiteral);
    query.setParameter(2, maxResults);
    return mapToDocuments(query.getResultList());
  }

  @SuppressWarnings("unchecked")
  public List<PlantKnowledgeDocument> findByVectorSimilarity(float[] queryVector,
      String plantType,
      int maxResults) {
    String vectorLiteral = arrayToPgVector(queryVector);
    String sql = "SELECT id, plant_type, title, content, category, created_at, updated_at "
        + "FROM greenhouse.plant_knowledge_document "
        + "WHERE plant_type = ?1 "
        + "ORDER BY embedding <=> CAST(?2 AS vector) "
        + "LIMIT ?3";

    var query = entityManager.createNativeQuery(sql);
    query.setParameter(1, plantType);
    query.setParameter(2, vectorLiteral);
    query.setParameter(PARAM_INDEX_MAX_RESULTS, maxResults);
    return mapToDocuments(query.getResultList());
  }

  private List<PlantKnowledgeDocument> mapToDocuments(List<Object[]> rows) {
    return rows.stream().map(row -> {
      PlantKnowledgeDocument doc = new PlantKnowledgeDocument();
      doc.id = ((Number) row[0]).longValue();
      doc.plantType = (String) row[1];
      doc.title = (String) row[2];
      doc.content = (String) row[COL_CONTENT];
      doc.category = (String) row[COL_CATEGORY];
      doc.createdAt = ((java.sql.Timestamp) row[COL_CREATED_AT]).toInstant();
      doc.updatedAt = ((java.sql.Timestamp) row[COL_UPDATED_AT]).toInstant();
      doc.embedding = null;
      return doc;
    }).toList();
  }

  @Transactional
  public void updateEmbedding(Long documentId, float[] embedding) {
    String vectorLiteral = arrayToPgVector(embedding);
    String sql = "UPDATE greenhouse.plant_knowledge_document "
        + "SET embedding = CAST(?1 AS vector), updated_at = NOW() "
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

package de.hablijack.greenhouse.ai.rag.service;

import de.hablijack.greenhouse.ai.config.AiConfig;
import de.hablijack.greenhouse.ai.rag.entity.PlantKnowledgeDocument;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PromptEnrichmentService {

  private static final Logger LOG = LoggerFactory.getLogger(PromptEnrichmentService.class);

  private final VectorSearchService vectorSearchService;
  private final AiConfig config;

  public PromptEnrichmentService(VectorSearchService vectorSearchService, AiConfig config) {
    this.vectorSearchService = vectorSearchService;
    this.config = config;
  }

  public String enrichPrompt(String userQuery, String plantType) {
    if (!config.rag().enabled()) {
      LOG.debug("RAG is disabled, returning unenriched prompt");
      return userQuery;
    }

    List<PlantKnowledgeDocument> relevantDocs;
    if (plantType != null && !plantType.isBlank()) {
      relevantDocs = vectorSearchService.findSimilarDocuments(
          userQuery, plantType, config.rag().maxDocuments());
    } else {
      relevantDocs = vectorSearchService.findSimilarDocuments(
          userQuery, config.rag().maxDocuments());
    }

    if (relevantDocs.isEmpty()) {
      LOG.debug("No relevant documents found for query");
      return userQuery;
    }

    String knowledgeContext = relevantDocs.stream()
        .map(this::formatDocument)
        .collect(Collectors.joining("\n---\n"));

    LOG.debug("Enriched prompt with {} relevant documents", relevantDocs.size());
    return String.format(
        "Relevant plant knowledge:%n%s%n%nUser query: %s",
        knowledgeContext, userQuery);
  }

  public String buildSystemPrompt(String plantType) {
    StringBuilder sb = new StringBuilder();
    sb.append("You are an expert greenhouse assistant with deep knowledge of plant care. Always respond in German language.\n");
    if (plantType != null && !plantType.isBlank()) {
      sb.append("Plant type: ").append(plantType).append("\n");
    }
    sb.append("""
    Provide:
        - risk assessment
    - actionable recommendations
    - short explanation
    - urgency level (low, medium, or high)

    Output valid JSON with the following structure:
        {
      "summary": "brief analysis summary",
          "recommendations": ["recommendation1", "recommendation2"],
          "urgency": "low|medium|high",
          "riskAssessment": "detailed risk assessment"
    }

    Consider these factors:
        - Temperature ranges optimal for the plant
    - Humidity requirements and disease risk
    - Soil moisture and overwatering warning
    - Light intensity requirements
    - CO2 levels for healthy growth
        """);
    return sb.toString();
  }

  private String formatDocument(PlantKnowledgeDocument doc) {
    return String.format("[%s] %s%n%s",
        doc.category != null ? doc.category : "General",
        doc.title != null ? doc.title : "Untitled",
        doc.content);
  }
}

package de.hablijack.greenhouse.ai.rag.service;

import de.hablijack.greenhouse.ai.config.AiConfig;
import de.hablijack.greenhouse.ai.rag.entity.PlantKnowledgeDocument;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
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

  public String enrichBatchPrompt(String batchPrompt, List<String> plantTypes) {
    if (!config.rag().enabled()) {
      LOG.debug("RAG is disabled, returning unenriched batch prompt");
      return batchPrompt;
    }

    List<PlantKnowledgeDocument> allDocs = new ArrayList<>();
    for (String plantType : plantTypes) {
      List<PlantKnowledgeDocument> docs = vectorSearchService.findSimilarDocuments(
          plantType, plantType, config.rag().maxDocuments());
      allDocs.addAll(docs);
    }

    if (allDocs.isEmpty()) {
      LOG.debug("No relevant documents found for batch query");
      return batchPrompt;
    }

    String knowledgeContext = allDocs.stream()
        .distinct()
        .map(this::formatDocument)
        .collect(Collectors.joining("\n---\n"));

    LOG.debug("Enriched batch prompt with {} unique documents for {} plant types",
        allDocs.size(), plantTypes.size());
    return String.format(
        "Relevant plant knowledge:%n%s%n%nUser query: %s",
        knowledgeContext, batchPrompt);
  }

  public String buildSystemPrompt(String plantType) {
    StringBuilder sb = new StringBuilder();
    sb.append("You are an expert greenhouse assistant with deep knowledge of plant care. ");
    sb.append("Always respond in German language.\n");
    if (plantType != null && !plantType.isBlank()) {
      sb.append("Plant type: ").append(plantType).append("\n");
    }
    sb.append("""
        Provide:
            - urgency level (low, medium, or high)

        Output valid JSON with the following structure:
            {
          "summary": "single sentence with assessment and actionable tip in German",
              "urgency": "low|medium|high"
        }

        Do NOT list individual sensor values. Do NOT output recommendations separately.
        """);

    return sb.toString();
  }

  public String buildBatchSystemPrompt(List<String> plantTypes) {
    StringBuilder sb = new StringBuilder();
    sb.append("You are an expert greenhouse assistant with deep knowledge of plant care. ");
    sb.append("Always respond in German language.\n");
    sb.append("Plant types to analyze: ").append(String.join(", ", plantTypes)).append("\n");
    sb.append("""
        Analyze each plant separately and provide a summary and urgency for each.

        Output valid JSON where each key is a plant name and each value has the following structure:
            {
              "summary": "single sentence with assessment and actionable tip in German",
              "urgency": "low|medium|high"
            }

        Example:
            {
              "Tomaten": {
                "summary": "Temperatur leicht erhöht, Belüftung prüfen.",
                "urgency": "medium"
              },
              "Salat": {
                "summary": "Alle Werte im optimalen Bereich.",
                "urgency": "low"
              }
            }

        Do NOT list individual sensor values. Do NOT output recommendations separately.
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

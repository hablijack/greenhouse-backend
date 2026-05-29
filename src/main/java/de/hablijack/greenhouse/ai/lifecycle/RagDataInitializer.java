package de.hablijack.greenhouse.ai.lifecycle;

import de.hablijack.greenhouse.ai.rag.entity.PlantKnowledgeDocument;
import de.hablijack.greenhouse.ai.rag.service.DocumentIngestionService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
public class RagDataInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(RagDataInitializer.class);

  private final DocumentIngestionService documentIngestionService;

  public RagDataInitializer(DocumentIngestionService documentIngestionService) {
    this.documentIngestionService = documentIngestionService;
  }

  RagDataInitializer() {
    this.documentIngestionService = null;
  }

  @PostConstruct
  @Transactional
  public void init() {
    if (PlantKnowledgeDocument.count() > 0) {
      LOG.info("RAG documents already exist, skipping initialization");
      return;
    }

    LOG.info("Initializing RAG knowledge base with plant care documents");

    List<DocumentIngestionService.DocumentInput> documents = new ArrayList<>();
    documents.addAll(initTomatoDocuments());
    documents.addAll(initCucumberDocuments());
    documents.addAll(initGeneralDocuments());

    documentIngestionService.ingestBatch(documents);
    LOG.info("RAG knowledge base initialized with {} documents", documents.size());
  }

  private List<DocumentIngestionService.DocumentInput> initTomatoDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("tomato",
        "Ideal temperature ranges for tomatoes",
        "Tomatoes (Solanum lycopersicum) thrive in daytime temperatures between 18-30°C. "
        + "Night temperatures should stay above 15°C. Below 10°C causes cold stress and "
        + "prevents fruit set. Above 32°C reduces pollination and causes flower drop. "
        + "Optimal fruit development occurs at 21-24°C.",
        "temperature"),

        new DocumentIngestionService.DocumentInput("tomato",
        "Tomato watering schedule and requirements",
        "Tomatoes need consistent moisture: 2-3 cm of water per week. "
        + "Water deeply 2-3 times per week rather than daily shallow watering. "
        + "Soil should be moist but not waterlogged. Reduce watering when fruits ripen. "
        + "Drip irrigation is preferred to avoid wetting foliage. "
        + "Overwatering causes root rot and blossom-end rot. "
        + "Underwatering causes blossom drop and cracked fruits.",
        "watering"),

        new DocumentIngestionService.DocumentInput("tomato",
        "Tomato disease prevention",
        "Common tomato diseases: early blight (Alternaria), late blight (Phytophthora), "
        + "powdery mildew, fusarium wilt, and bacterial spot. "
        + "Prevention: ensure good air circulation, avoid overhead watering, "
        + "rotate crops every 3 years, use disease-resistant varieties. "
        + "Remove lower leaves touching soil. Space plants 60-90cm apart. "
        + "Apply mulch to prevent soil splash. Copper fungicide for prevention.",
        "disease"),

        new DocumentIngestionService.DocumentInput("tomato",
        "Tomato nutrient requirements",
        "Tomatoes are heavy feeders. Nitrogen (N): important for vegetative growth, "
        + "reduce when flowering begins. Phosphorus (P): essential for flowers and fruits. "
        + "Potassium (K): crucial for fruit development and disease resistance. "
        + "Calcium: prevents blossom-end rot. Magnesium: prevents leaf yellowing. "
        + "Apply balanced 10-10-10 fertilizer at planting, side-dress monthly. "
        + "Epsom salt (magnesium sulfate) every 2 weeks during fruiting.",
        "nutrients"),

        new DocumentIngestionService.DocumentInput("tomato",
        "Tomato pruning and support",
        "Prune indeterminate tomatoes to 1-2 main stems for best yields. "
        + "Remove suckers (side shoots) when 5-10cm long. "
        + "Use stakes, cages, or trellises for support. "
        + "Remove lower leaves to improve airflow. "
        + "Pinch growing tips 4 weeks before first frost to redirect energy to fruits. "
        + "Regular pruning reduces disease risk and improves fruit size.",
        "pruning"),

        new DocumentIngestionService.DocumentInput("tomato",
        "Tomato humidity requirements",
        "Tomatoes prefer 50-75% relative humidity. "
        + "High humidity (>85%) promotes fungal diseases like late blight and powdery mildew. "
        + "Low humidity (<40%) reduces pollination and causes blossom drop. "
        + "In high humidity greenhouses, increase ventilation and use fans. "
        + "Avoid overcrowding plants. Space vines for good air circulation. "
        + "Heat and vent in the morning to reduce leaf wetness duration.",
        "humidity")
    );
  }

  private List<DocumentIngestionService.DocumentInput> initCucumberDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("cucumber",
        "Ideal temperature ranges for cucumbers",
        "Cucumbers (Cucumis sativus) thrive in warm conditions: 20-28°C daytime, "
        + "16-22°C nighttime. Growth stops below 15°C. Frost kills plants. "
        + "Above 35°C causes bitter fruits and flower abortion. "
        + "Soil temperature should be at least 18°C for germination. "
        + "Optimal fruit quality at 21-26°C. Use row covers for cold protection.",
        "temperature"),

        new DocumentIngestionService.DocumentInput("cucumber",
        "Cucumber watering and moisture",
        "Cucumbers need consistent moisture: 2.5-4 cm of water per week. "
        + "Water deeply 2-3 times weekly. Soil must stay evenly moist. "
        + "Inconsistent watering causes bitter fruits. "
        + "Use drip irrigation or soaker hoses to keep foliage dry. "
        + "Mulch heavily to retain moisture and regulate soil temperature. "
        + "Overwatering leads to root rot and fungal diseases. "
        + "Reduce watering slightly as fruits mature.",
        "watering"),

        new DocumentIngestionService.DocumentInput("cucumber",
        "Cucumber disease prevention",
        "Common cucumber diseases: powdery mildew, downy mildew, "
        + "angular leaf spot, bacterial wilt (spread by cucumber beetles), "
        + "and anthracnose. Prevention: plant resistant varieties, "
        + "ensure good airflow, rotate crops yearly, "
        + "use drip irrigation, remove infected leaves promptly. "
        + "Apply sulfur or neem oil for powdery mildew. "
        + "Control cucumber beetles to prevent bacterial wilt.",
        "disease"),

        new DocumentIngestionService.DocumentInput("cucumber",
        "Cucumber nutrient requirements",
        "Cucumbers require balanced nutrition. Nitrogen: supports vine growth. "
        + "Phosphorus: important for flowers and fruit set. "
        + "Potassium: improves fruit quality and disease resistance. "
        + "Apply 5-10-10 fertilizer before planting. "
        + "Side-dress with compost tea or balanced fertilizer every 2-3 weeks. "
        + "Magnesium deficiency causes leaf yellowing between veins. "
        + "Avoid excess nitrogen which reduces fruit production.",
        "nutrients"),

        new DocumentIngestionService.DocumentInput("cucumber",
        "Cucumber pruning and trellising",
        "Cucumbers benefit from trellising: saves space, improves airflow, "
        + "keeps fruits clean, reduces disease. Use A-frame or vertical trellises. "
        + "Train main vine upward, remove lower lateral branches. "
        + "Prune suckers and excess foliage to improve light penetration. "
        + "Harvest regularly to encourage continued production. "
        + "Space plants 30-60cm apart on trellises.",
        "pruning"),

        new DocumentIngestionService.DocumentInput("cucumber",
        "Cucumber humidity and ventilation",
        "Cucumbers prefer 60-75% relative humidity. "
        + "High humidity (>85%) promotes downy mildew and powdery mildew. "
        + "Ensure good greenhouse ventilation with side and roof vents. "
        + "Use horizontal airflow fans to reduce humidity pockets. "
        + "Avoid overhead watering. Water in morning so foliage dries by night. "
        + "In humid conditions, space plants wider and prune for airflow.",
        "humidity")
    );
  }

  private List<DocumentIngestionService.DocumentInput> initGeneralDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general",
        "General greenhouse environmental control",
        "Optimal greenhouse conditions: temperature 18-28°C, humidity 50-75%, "
        + "CO2 400-1200 ppm, light 200-1000 µmol/m²/s. "
        + "Use automated ventilation when temperature exceeds 26°C. "
        + "CO2 enrichment to 800-1000 ppm boosts photosynthesis by 30-40%. "
        + "Supplemental lighting when natural light below 200 µmol/m²/s. "
        + "Monitor daily for pest outbreaks and environmental stress.",
        "general"),

        new DocumentIngestionService.DocumentInput("general",
        "Overwatering prevention and root health",
        "Overwatering is the most common cause of greenhouse plant problems. "
        + "Symptoms: yellowing lower leaves, wilting despite wet soil, "
        + "algae on soil surface, fungal gnats, root rot odor. "
        + "Prevention: use well-draining soil mix, pots with drainage holes, "
        + "water based on soil moisture not schedule, "
        + "allow soil to dry slightly between waterings. "
        + "Most plants prefer soil moisture between 40-75%.",
        "watering"),

        new DocumentIngestionService.DocumentInput("general",
        "Fungal disease prevention in greenhouses",
        "Fungal diseases thrive in high humidity (>85%), poor air circulation, "
        + "and leaf wetness. Prevention strategies: "
        + "1) Maintain humidity below 80%, 2) Use horizontal airflow fans, "
        + "3) Space plants for air circulation, 4) Water at soil level, "
        + "5) Remove infected plant material immediately, "
        + "6) Sterilize tools between plants, "
        + "7) Apply preventative fungicides (copper, sulfur, neem oil), "
        + "8) Use disease-resistant varieties.",
        "disease")
    );
  }
}

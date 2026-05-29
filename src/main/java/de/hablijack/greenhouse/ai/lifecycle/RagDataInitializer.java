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

  @PostConstruct
  public void init() {
    LOG.info("RAG initialization scheduled in background thread");
    Thread.startVirtualThread(this::initInternal);
  }

  private void initInternal() {
    try {
      if (PlantKnowledgeDocument.count() > 0) {
        LOG.info("RAG documents already exist, skipping initialization");
        return;
      }

      LOG.info("Initializing advanced greenhouse RAG knowledge base");

      List<DocumentIngestionService.DocumentInput> documents = new ArrayList<>();

      documents.addAll(initTomatoDocuments());
      documents.addAll(initCucumberDocuments());
      documents.addAll(initClimateDocuments());
      documents.addAll(initSensorInterpretationDocuments());
      documents.addAll(initVpdDocuments());
      documents.addAll(initAutomationDocuments());
      documents.addAll(initDiseaseDocuments());
      documents.addAll(initPestDocuments());
      documents.addAll(initHydroponicDocuments());
      documents.addAll(initLightingDocuments());
      documents.addAll(initDiagnosticsDocuments());
      documents.addAll(initEmergencyDocuments());
      documents.addAll(initPollinationDocuments());
      documents.addAll(initYieldOptimizationDocuments());
      documents.addAll(initWaterQualityDocuments());
      documents.addAll(initSensorFaultDocuments());

      documentIngestionService.ingestBatch(documents);

      LOG.info("Advanced greenhouse RAG initialized with {} documents", documents.size());
    } catch (Exception e) {
      LOG.error("Failed to initialize RAG knowledge base (application will continue): {}",
          e.getMessage(), e);
    }
  }

  // ===========================================================================
  // TOMATO
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initTomatoDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato vegetative growth climate",
            "During vegetative growth tomatoes prefer 22-28°C daytime temperatures "
                + "and 18-22°C nighttime temperatures. Humidity should remain between "
                + "60-75%. Excess humidity increases fungal disease risk while low humidity "
                + "increases transpiration stress and calcium deficiency risk.",
            "tomato_climate"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato flowering climate management",
            "During flowering tomatoes require 21-27°C daytime and 16-20°C nighttime "
                + "temperatures. Humidity above 80% reduces pollen viability and causes "
                + "poor fruit set. Maintain strong airflow and moderate humidity "
                + "to maximize pollination success.",
            "tomato_flowering"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato fruiting nutrient management",
            "During fruiting tomatoes require elevated potassium and calcium levels. "
                + "Reduce excessive nitrogen during heavy fruit production. "
                + "Calcium deficiencies during fruit expansion cause blossom-end rot. "
                + "Maintain stable irrigation to support nutrient transport.",
            "tomato_nutrients"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato irrigation strategy",
            "Tomatoes require deep irrigation with moderate dry-back cycles. "
                + "Frequent shallow watering weakens root development. "
                + "Allow moderate substrate drying between irrigation cycles "
                + "to improve oxygen availability in the root zone.",
            "tomato_irrigation"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato heat stress response",
            "Temperatures above 32°C cause tomato pollen sterility, flower abortion, "
                + "reduced fruit set, and elevated transpiration stress. "
                + "Immediately increase ventilation and cooling during heat stress events.",
            "tomato_heat"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato pruning and airflow",
            "Prune indeterminate tomatoes regularly to maintain airflow and light penetration. "
                + "Remove lower leaves touching the soil to reduce fungal disease risk. "
                + "Proper pruning improves fruit quality and reduces humidity accumulation.",
            "tomato_pruning"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato pollination strategy",
            "Tomatoes are self-pollinating but require vibration for efficient pollen release. "
                + "High humidity causes pollen clumping and poor pollination. "
                + "Use airflow, bumblebees, or mechanical vibration to improve fruit set.",
            "tomato_pollination"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato blossom-end rot diagnosis",
            "Blossom-end rot is primarily caused by calcium transport disruption rather "
                + "than lack of calcium in soil. High VPD, irregular irrigation, root stress, "
                + "and excessive salinity increase blossom-end rot risk.",
            "tomato_diagnostics")

    );
  }

  // ===========================================================================
  // CUCUMBER
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initCucumberDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "cucumber",
            "Cucumber climate management",
            "Cucumbers thrive between 22-30°C daytime temperatures and "
                + "18-22°C nighttime temperatures. Temperatures below 15°C slow growth "
                + "while temperatures above 35°C increase bitter fruit formation.",
            "cucumber_climate"),

        new DocumentIngestionService.DocumentInput(
            "cucumber",
            "Cucumber irrigation requirements",
            "Cucumbers require consistently moist substrates but should not remain waterlogged. "
                + "Irregular irrigation causes bitter fruits and poor fruit development. "
                + "Drip irrigation is preferred to maintain stable root-zone moisture.",
            "cucumber_irrigation"),

        new DocumentIngestionService.DocumentInput(
            "cucumber",
            "Cucumber humidity and disease",
            "Humidity above 85% increases powdery mildew and downy mildew risk in cucumbers. "
                + "Ensure continuous airflow and proper plant spacing to reduce disease pressure.",
            "cucumber_humidity"),

        new DocumentIngestionService.DocumentInput(
            "cucumber",
            "Cucumber trellising and airflow",
            "Vertical trellising improves airflow, reduces disease pressure, "
                + "improves fruit quality, and simplifies harvesting operations.",
            "cucumber_pruning")

    );
  }

  // ===========================================================================
  // CLIMATE
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initClimateDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Greenhouse climate balancing",
            "Optimal greenhouse climate management balances temperature, humidity, "
                + "CO2, airflow, and light simultaneously. Stable conditions reduce "
                + "plant stress and improve growth consistency.",
            "climate"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Greenhouse ventilation strategy",
            "When outside temperature is significantly lower than greenhouse temperature, "
                + "aggressive ventilation efficiently removes heat and humidity. "
                + "Roof ventilation removes accumulated hot air most effectively.",
            "ventilation"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Humidity management strategy",
            "Maintain greenhouse humidity between 55-75% for most crops. "
                + "Humidity above 85% dramatically increases fungal disease risk "
                + "while humidity below 40% increases transpiration stress.",
            "humidity"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "CO2 enrichment strategy",
            "CO2 concentrations between 800-1200 ppm improve photosynthesis "
                + "under high light conditions. CO2 above 1500 ppm provides "
                + "limited additional benefit and may indicate insufficient ventilation.",
            "co2")

    );
  }

  // ===========================================================================
  // SENSOR INTERPRETATION
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initSensorInterpretationDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "High greenhouse temperature interpretation",
            "Greenhouse temperatures above 30°C increase transpiration demand "
                + "and stress sensitive crops. Tomatoes experience pollination problems "
                + "while cucumbers may produce bitter fruits.",
            "sensor_interpretation"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Dry soil moisture interpretation",
            "Multiple dry soil moisture sensors during high greenhouse temperatures "
                + "indicate elevated drought stress risk and possible calcium transport problems.",
            "soil_moisture"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Combined stress interpretation",
            "High temperature combined with dry substrate conditions significantly "
                + "increases plant stress, nutrient transport disruption, and fruit quality problems.",
            "combined_stress"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Outside climate cooling opportunity",
            "If outside temperature is at least 5°C lower than inside greenhouse temperature, "
                + "ventilation can rapidly reduce heat stress and humidity accumulation.",
            "cooling")

    );
  }

  // ===========================================================================
  // VPD
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initVpdDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "VPD greenhouse management",
            "Vapor Pressure Deficit controls transpiration and nutrient transport. "
                + "Low VPD increases fungal disease risk while high VPD increases "
                + "water stress and calcium deficiency risk.",
            "vpd"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "High VPD conditions",
            "High temperature combined with moderate or low humidity increases VPD "
                + "and causes excessive transpiration demand. "
                + "Plants may wilt despite adequate irrigation.",
            "vpd_high"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Low VPD conditions",
            "Low VPD caused by excessive humidity reduces transpiration "
                + "and increases fungal disease pressure and edema risk.",
            "vpd_low")

    );
  }

  // ===========================================================================
  // AUTOMATION
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initAutomationDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Ventilation automation rules",
            "If greenhouse temperature exceeds 30°C and outside air is cooler, "
                + "increase ventilation immediately. "
                + "Prioritize heat stress prevention over CO2 retention.",
            "automation"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Irrigation automation rules",
            "If substrate moisture sensors indicate dry conditions during high temperature periods, "
                + "increase irrigation frequency while monitoring root-zone oxygen levels.",
            "automation_irrigation"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Humidity automation strategy",
            "Reduce humidity aggressively during nighttime and early morning periods "
                + "to prevent fungal disease outbreaks and condensation.",
            "automation_humidity"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Greenhouse AI reasoning strategy",
            "Always prioritize prevention of irreversible plant stress. "
                + "High temperature damage occurs faster than moderate CO2 reduction.",
            "reasoning")

    );
  }

  // ===========================================================================
  // DISEASE
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initDiseaseDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Fungal disease prevention",
            "Fungal diseases thrive under high humidity, leaf wetness, and poor airflow. "
                + "Maintain airflow, avoid overhead irrigation, and reduce nighttime humidity.",
            "disease"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Root rot conditions",
            "Overwatering combined with low oxygen conditions promotes root rot pathogens. "
                + "Plants may wilt despite wet substrate conditions.",
            "root_rot"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato late blight prevention",
            "Late blight spreads rapidly under cool humid conditions. "
                + "Maintain airflow and avoid prolonged leaf wetness periods.",
            "tomato_blight")

    );
  }

  // ===========================================================================
  // PESTS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initPestDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Spider mite management",
            "Spider mites thrive under hot dry greenhouse conditions. "
                + "Increase humidity temporarily and introduce predatory mites if necessary.",
            "pests"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Whitefly greenhouse management",
            "Whiteflies reproduce rapidly in warm protected environments. "
                + "Monitor undersides of leaves and use sticky traps for early detection.",
            "whitefly"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Fungus gnat prevention",
            "Excessively wet substrates promote fungus gnat reproduction. "
                + "Allow moderate substrate drying between irrigation cycles.",
            "fungus_gnat")

    );
  }

  // ===========================================================================
  // HYDROPONICS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initHydroponicDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Hydroponic pH management",
            "Most greenhouse crops prefer nutrient solution pH between 5.5 and 6.5. "
                + "Incorrect pH reduces nutrient availability and causes deficiency symptoms.",
            "hydroponics"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Hydroponic EC management",
            "Excessively high EC increases salinity stress and root damage. "
                + "Low EC reduces nutrient availability and plant vigor.",
            "ec")

    );
  }

  // ===========================================================================
  // LIGHTING
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initLightingDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Greenhouse lighting management",
            "Low light conditions reduce photosynthesis and fruit quality. "
                + "During cloudy periods reduce irrigation frequency to prevent overwatering.",
            "lighting"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Supplemental LED strategy",
            "Supplemental LED lighting improves winter production and "
                + "maintains consistent plant growth during low solar radiation periods.",
            "led"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Daily light integral management",
            "Daily Light Integral strongly influences crop yield and fruit quality. "
                + "Tomatoes require high cumulative light levels for maximum productivity.",
            "dli")

    );
  }

  // ===========================================================================
  // DIAGNOSTICS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initDiagnosticsDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Leaf yellowing diagnostics",
            "Uniform leaf yellowing may indicate nitrogen deficiency or root stress. "
                + "Yellowing combined with wet substrate suggests root disease.",
            "diagnostics"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Wilting diagnostics",
            "Wilting during hot conditions may indicate excessive transpiration demand. "
                + "Wilting despite wet soil suggests root damage or oxygen deficiency.",
            "wilting"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Leaf curl diagnostics",
            "Leaf curl under high temperatures often indicates excessive VPD and water stress.",
            "leaf_curl")

    );
  }

  // ===========================================================================
  // EMERGENCY
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initEmergencyDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Emergency heat response",
            "When greenhouse temperatures exceed 35°C immediately maximize ventilation, "
                + "deploy shading systems, and ensure adequate irrigation availability.",
            "emergency"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Emergency drought response",
            "Dry substrate conditions during heat stress require immediate deep irrigation "
                + "to restore transpiration and nutrient transport.",
            "drought"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Emergency humidity response",
            "Condensation and excessive humidity require aggressive airflow and ventilation "
                + "to prevent rapid fungal disease outbreaks.",
            "humidity_emergency")

    );
  }

  // ===========================================================================
  // POLLINATION
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initPollinationDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Greenhouse pollination management",
            "Pollination success depends on moderate humidity, stable temperatures, "
                + "and adequate airflow. Excess heat and humidity reduce pollen viability.",
            "pollination")

    );
  }

  // ===========================================================================
  // YIELD
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initYieldOptimizationDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Yield optimization strategy",
            "Stable environmental conditions produce higher quality fruits "
                + "than aggressive growth strategies with fluctuating climate conditions.",
            "yield"),

        new DocumentIngestionService.DocumentInput(
            "tomato",
            "Tomato flavor optimization",
            "Moderate EC increase during fruit ripening improves sugar concentration "
                + "and flavor intensity in tomatoes.",
            "tomato_quality")

    );
  }

  // ===========================================================================
  // WATER QUALITY
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initWaterQualityDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Irrigation water quality",
            "Poor irrigation water quality can cause nutrient lockout and salinity buildup. "
                + "Monitor pH, bicarbonates, and sodium concentrations regularly.",
            "water_quality")

    );
  }

  // ===========================================================================
  // SENSOR FAULTS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initSensorFaultDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Soil temperature sensor anomaly",
            "Soil temperatures above 45°C are unlikely in normal greenhouse conditions "
                + "and may indicate sensor calibration or hardware failure.",
            "sensor_fault"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Sensor validation strategy",
            "Always compare sensor readings against environmental context. "
                + "Extreme values inconsistent with surrounding conditions may indicate sensor malfunction.",
            "sensor_validation")

    );
  }
}

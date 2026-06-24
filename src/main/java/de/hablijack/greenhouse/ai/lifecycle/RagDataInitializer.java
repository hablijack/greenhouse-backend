package de.hablijack.greenhouse.ai.lifecycle;

import de.hablijack.greenhouse.ai.rag.entity.PlantKnowledgeDocument;
import de.hablijack.greenhouse.ai.rag.service.DocumentIngestionService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
@SuppressWarnings("checkstyle:LineLength")
public class RagDataInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(RagDataInitializer.class);

  private final DocumentIngestionService documentIngestionService;

  @Inject
  RagDataInitializer self;

  public RagDataInitializer(DocumentIngestionService documentIngestionService) {
    this.documentIngestionService = documentIngestionService;
  }

  @PostConstruct
  void init() {
    LOG.info("RAG initialization scheduled in background thread");
    Thread.startVirtualThread(() -> self.initInternal());
  }

  @Transactional
  public void reimportAll() {
    LOG.info("Reimporting all RAG documents - deleting existing documents");
    PlantKnowledgeDocument.deleteAll();
    LOG.info("Existing documents deleted, re-ingesting all documents");
    ingestAllDocuments();
  }

  @Transactional
  void initInternal() {
    try {
      if (PlantKnowledgeDocument.count() > 0) {
        LOG.info("RAG documents already exist, skipping initialization");
        return;
      }
      LOG.info("Initializing advanced greenhouse RAG knowledge base");
      ingestAllDocuments();
    } catch (Exception e) {
      LOG.error("Failed to initialize RAG knowledge base (application will continue): {}",
          e.getMessage(), e);
    }
  }

  private void ingestAllDocuments() {
    try {
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
      documents.addAll(initRelayControlDocuments());
      documents.addAll(initOperationalSafetyDocuments());
      documents.addAll(initSafetyLimitDocuments());
      documents.addAll(initThresholdTablesDocuments());
      documents.addAll(initLettuceDocuments());
      documents.addAll(initRadishDocuments());
      documents.addAll(initMelonDocuments());
      documents.addAll(initGrapeDocuments());
      documents.addAll(initGrowthStageDocuments());
      documents.addAll(initTrendInterpretationDocuments());

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
        new DocumentIngestionService.DocumentInput("tomato", "Tomato vegetative growth climate",
            "Daytime 22-28°C, nighttime 18-22°C. Humidity 60-75%. Above 75% fungal risk, below 60% calcium deficiency risk.",
            "tomato_climate"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato flowering climate management",
            "Daytime 21-27°C, nighttime 16-20°C. Humidity below 80% for pollen viability. Maintain strong airflow for pollination.",
            "tomato_flowering"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato fruiting nutrient management",
            "Increase K and Ca, reduce N during heavy fruiting. Ca deficiency causes blossom-end rot. Maintain stable irrigation.",
            "tomato_nutrients"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato irrigation strategy",
            "Deep irrigation with dry-back cycles. Avoid shallow frequent watering. Allow moderate drying between cycles for root oxygenation.",
            "tomato_irrigation"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato heat stress response",
            "Above 32°C: pollen sterility, flower abortion, reduced fruit set. Immediately increase ventilation and cooling.",
            "tomato_heat"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato pruning and airflow",
            "Prune indeterminate tomatoes regularly. Remove lower leaves touching soil. Improves airflow and reduces fungal risk.",
            "tomato_pruning"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato pollination strategy",
            "Self-pollinating, needs vibration for pollen release. Humidity >80% causes pollen clumping. Use airflow or mechanical vibration.",
            "tomato_pollination"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato blossom-end rot diagnosis",
            "Caused by Ca transport disruption, not soil Ca. High VPD, irregular irrigation, root stress, and high salinity increase risk.",
            "tomato_diagnostics"));
  }

  // ===========================================================================
  // CUCUMBER
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initCucumberDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("cucumber", "Cucumber climate management",
            "Daytime 22-30°C, nighttime 18-22°C. Below 15°C growth slows. Above 35°C causes bitter fruit.",
            "cucumber_climate"),
        new DocumentIngestionService.DocumentInput("cucumber", "Cucumber irrigation requirements",
            "Consistently moist, never waterlogged. Irregular irrigation causes bitter fruit and poor development. Drip irrigation preferred.",
            "cucumber_irrigation"),
        new DocumentIngestionService.DocumentInput("cucumber", "Cucumber humidity and disease",
            "Humidity below 85%. Above 85% increases powdery and downy mildew risk. Ensure continuous airflow.",
            "cucumber_humidity"),
        new DocumentIngestionService.DocumentInput("cucumber", "Cucumber trellising and airflow",
            "Vertical trellising improves airflow, reduces disease, improves fruit quality, and simplifies harvesting.",
            "cucumber_pruning"));
  }

  // ===========================================================================
  // CLIMATE
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initClimateDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Greenhouse climate balancing",
            "Balance temperature, humidity, CO2, airflow, and light simultaneously. Stable conditions reduce plant stress.",
            "climate"),
        new DocumentIngestionService.DocumentInput("general", "Greenhouse ventilation strategy",
            "When outside is cooler than inside, aggressive ventilation removes heat and humidity efficiently. Roof ventilation works best.",
            "ventilation"),
        new DocumentIngestionService.DocumentInput("general", "Humidity management strategy",
            "Target 55-75% for most crops. Above 85% fungal risk, below 40% transpiration stress.",
            "humidity"),
        new DocumentIngestionService.DocumentInput("general", "CO2 enrichment strategy",
            "Target 800-1200 ppm for photosynthesis. Above 1500 ppm limited benefit, may indicate insufficient ventilation.",
            "co2"));
  }

  // ===========================================================================
  // SENSOR INTERPRETATION
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initSensorInterpretationDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "High greenhouse temperature interpretation",
            "Above 30°C increases transpiration stress. Tomato pollination fails, cucumbers produce bitter fruit.",
            "sensor_interpretation"),
        new DocumentIngestionService.DocumentInput("general", "Dry soil moisture interpretation",
            "Multiple dry sensors + high temperature = elevated drought stress and Ca transport risk.",
            "soil_moisture"),
        new DocumentIngestionService.DocumentInput("general", "Combined stress interpretation",
            "High temperature + dry substrate = severe plant stress, nutrient disruption, and fruit quality problems.",
            "combined_stress"),
        new DocumentIngestionService.DocumentInput("general", "Outside climate cooling opportunity",
            "If outside is 5°C below inside, ventilation rapidly reduces heat stress and humidity.",
            "cooling"),
        new DocumentIngestionService.DocumentInput("general", "Time-of-day temperature interpretation",
            "Morning (5-9am): 12-15°C acceptable. Noon (10am-4pm): 12°C is dangerously cold. Night: above 24°C prevents plant rest.",
            "time_of_day_sensor"),
        new DocumentIngestionService.DocumentInput("general", "Seasonal greenhouse management",
            "Winter: heat+insulate+supplemental light. Spring: watch temperature swings. Summer: cool+shade+ventilate+morning CO2. Autumn: reduce irrigation, prepare heating.",
            "seasonal_management"),
        new DocumentIngestionService.DocumentInput("general", "Day-night temperature differential",
            "4-8°C cooler night promotes strong stems and fruit set. Tomato night 16-20°C, cucumber 18-22°C. Below 10°C damages sensitive crops.",
            "dif_temperature"));
  }

  // ===========================================================================
  // VPD
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initVpdDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "VPD greenhouse management",
            "VPD controls transpiration and nutrient transport. Low VPD = fungal risk. High VPD = water stress and Ca deficiency.",
            "vpd"),
        new DocumentIngestionService.DocumentInput("general", "High VPD conditions",
            "High temp + low/medium humidity = high VPD, excessive transpiration demand. Plants wilt despite adequate irrigation.",
            "vpd_high"),
        new DocumentIngestionService.DocumentInput("general", "Low VPD conditions",
            "Low VPD from excessive humidity reduces transpiration, increases fungal disease and edema risk.",
            "vpd_low"));
  }

  // ===========================================================================
  // AUTOMATION
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initAutomationDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Ventilation automation rules",
            "If temperature >30°C and outside is cooler, ventilate immediately. Prioritize heat stress prevention over CO2 retention.",
            "automation"),
        new DocumentIngestionService.DocumentInput("general", "Irrigation automation rules",
            "If dry substrate during high temperature, increase irrigation frequency but monitor root-zone oxygen.",
            "automation_irrigation"),
        new DocumentIngestionService.DocumentInput("general", "Humidity automation strategy",
            "Reduce humidity aggressively at night and early morning to prevent fungal outbreaks and condensation.",
            "automation_humidity"),
        new DocumentIngestionService.DocumentInput("general", "Greenhouse AI reasoning strategy",
            "Prevent irreversible plant stress first. High temperature damage occurs faster than moderate CO2 reduction impacts.",
            "reasoning"));
  }

  // ===========================================================================
  // DISEASE
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initDiseaseDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Fungal disease prevention",
            "High humidity, leaf wetness, poor airflow enable fungal diseases. Maintain airflow, avoid overhead irrigation, reduce night humidity.",
            "disease"),
        new DocumentIngestionService.DocumentInput("general", "Root rot conditions",
            "Overwatering + low oxygen = root rot pathogens. Plants wilt despite wet substrate.",
            "root_rot"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato late blight prevention",
            "Late blight spreads rapidly in cool humid conditions. Maintain airflow, avoid prolonged leaf wetness.",
            "tomato_blight"));
  }

  // ===========================================================================
  // PESTS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initPestDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Spider mite management",
            "Thrives in hot dry conditions. Increase humidity temporarily, introduce predatory mites.",
            "pests"),
        new DocumentIngestionService.DocumentInput("general", "Whitefly greenhouse management",
            "Reproduces rapidly in warm environments. Monitor leaf undersides, use sticky traps for early detection.",
            "whitefly"),
        new DocumentIngestionService.DocumentInput("general", "Fungus gnat prevention",
            "Wet substrates promote fungus gnats. Allow moderate drying between irrigation cycles.",
            "fungus_gnat"));
  }

  // ===========================================================================
  // HYDROPONICS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initHydroponicDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Hydroponic pH management",
            "Target pH 5.5-6.5. Incorrect pH reduces nutrient availability and causes deficiency symptoms.",
            "hydroponics"),
        new DocumentIngestionService.DocumentInput("general", "Hydroponic EC management",
            "High EC = salinity stress and root damage. Low EC = reduced nutrient availability.",
            "ec"));
  }

  // ===========================================================================
  // LIGHTING
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initLightingDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Greenhouse lighting management",
            "Low light reduces photosynthesis. During cloudy periods reduce irrigation to prevent overwatering.",
            "lighting"),
        new DocumentIngestionService.DocumentInput("general", "Supplemental LED strategy",
            "LED lighting improves winter production and maintains growth during low solar radiation.",
            "led"),
        new DocumentIngestionService.DocumentInput("general", "Daily light integral management",
            "DLI strongly influences yield. Tomatoes need high cumulative light for maximum productivity.",
            "dli"));
  }

  // ===========================================================================
  // DIAGNOSTICS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initDiagnosticsDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Leaf yellowing diagnostics",
            "Uniform yellowing = N deficiency or root stress. Yellowing + wet substrate = root disease.",
            "diagnostics"),
        new DocumentIngestionService.DocumentInput("general", "Wilting diagnostics",
            "Wilting in heat = excessive transpiration. Wilting despite wet soil = root damage or O2 deficiency.",
            "wilting"),
        new DocumentIngestionService.DocumentInput("general", "Leaf curl diagnostics",
            "Leaf curl under high temperature = excessive VPD and water stress.",
            "leaf_curl"));
  }

  // ===========================================================================
  // EMERGENCY
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initEmergencyDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Emergency heat response",
            "Above 35°C: immediately max ventilation, deploy shading, ensure irrigation.",
            "emergency"),
        new DocumentIngestionService.DocumentInput("general", "Emergency drought response",
            "Dry substrate + heat stress = immediate deep irrigation to restore transpiration and nutrient transport.",
            "drought"),
        new DocumentIngestionService.DocumentInput("general", "Emergency humidity response",
            "Condensation + excessive humidity = aggressive airflow and ventilation to prevent rapid fungal outbreaks.",
            "humidity_emergency"));
  }

  // ===========================================================================
  // POLLINATION
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initPollinationDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Greenhouse pollination management",
            "Moderate humidity, stable temperatures, adequate airflow needed. Excess heat and humidity reduce pollen viability.",
            "pollination"));
  }

  // ===========================================================================
  // YIELD
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initYieldOptimizationDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Yield optimization strategy",
            "Stable conditions produce higher quality than aggressive growth with fluctuating climate.",
            "yield"),
        new DocumentIngestionService.DocumentInput("tomato", "Tomato flavor optimization",
            "Moderate EC increase during ripening improves sugar concentration and flavor intensity.",
            "tomato_quality"));
  }

  // ===========================================================================
  // WATER QUALITY
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initWaterQualityDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Irrigation water quality",
            "Poor water quality causes nutrient lockout and salinity buildup. Monitor pH, bicarbonates, and sodium.",
            "water_quality"));
  }

  // ===========================================================================
  // SENSOR FAULTS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initSensorFaultDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Soil temperature sensor anomaly",
            "Soil temperature >45°C unlikely in normal conditions, indicates sensor fault.",
            "sensor_fault"),
        new DocumentIngestionService.DocumentInput("general", "Sensor validation strategy",
            "Compare sensor readings against environmental context. Extreme values inconsistent with conditions may be malfunction.",
            "sensor_validation"));
  }

  // ===========================================================================
  // RELAY CONTROL LOGIC
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initRelayControlDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Relay overview and mapping",
            "8 controllable relays on main ESP32, 1 on wine satellite. Line1-6 = irrigation. Line7 = LED lights. Line8 = fans. relay_wine_pump = grape irrigation. Default state = OFF.",
            "relay_overview"),
        new DocumentIngestionService.DocumentInput("general", "Irrigation relay control rules",
            "Irrigate when soil moisture <35%. Stop at >80% to prevent waterlogging. Duration 15-30min. Avoid 11am-3pm peak heat. Morning (6-9am) preferred. Winter reduce frequency 40%.",
            "relay_irrigation"),
        new DocumentIngestionService.DocumentInput("general", "Fan relay control rules",
            "Activate when temperature >28°C, humidity >80%, or CO2 >1200ppm. Run continuously during heat waves >32°C. Minimize night operation below 20°C. Winter: intermittent 10min on/30min off.",
            "relay_fans"),
        new DocumentIngestionService.DocumentInput("general", "Light relay control rules",
            "Activate when brightness <200 lux during daytime (6am-8pm). NEVER on at night (9pm-5am). Winter: 12-14h total. Summer: cloudy days only. Target DLI: 15-25 mol/m2/day tomatoes, 10-15 lettuce.",
            "relay_lighting"),
        new DocumentIngestionService.DocumentInput("general", "Relay priority and conflict resolution",
            "Priority: 1. Emergency safety limits. 2. Fan during humidity >85% or temp >35°C. 3. Fan+light compatible. 4. Irrigation+fan compatible. 5. >35°C prioritize cooling over CO2/irrigation. 6. Power constraints: fans before lights.",
            "relay_priority"),
        new DocumentIngestionService.DocumentInput("general", "Relay automation decision framework",
            "Consider: 1. Sensor severity. 2. Time of day. 3. Season. 4. Trend direction (proactive before threshold breach). 5. Combined risks (high temp+low moisture = urgent). 6. Rate of change.",
            "relay_decision_framework"));
  }

  // ===========================================================================
  // OPERATIONAL SAFETY LIMITS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initOperationalSafetyDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Irrigation duty cycle limits - sensor failure protection",
            "CRITICAL: Max 3 cycles/day per line. Max ON duration: 60s per cycle. Min cooldown: 4h. Total daily budget: 180s. If sensor reads <30% but 3 cycles reached, do NOT irrigate—sensor likely faulty.",
            "operational_irrigation"),
        new DocumentIngestionService.DocumentInput("general", "Fan duty cycle limits - power management",
            "CRITICAL: Max continuous ON: 4h. Min cooldown after max: 5min. Min ON time: 3min. Min OFF time: 3min. Hysteresis ON: >=29°C or >=90% humidity. Hysteresis OFF: <=27°C AND <=85%.",
            "operational_fan"),
        new DocumentIngestionService.DocumentInput("general", "Fan power availability - solar dependency",
            "CRITICAL: Fans need brightness >=2500 lux AND time 8am-5pm. Otherwise fans OFF unless emergency: temp >38°C or humidity >95% allows max 10min fan without solar.",
            "operational_fan_power"),
        new DocumentIngestionService.DocumentInput("general", "Sensor failure detection and handling",
            "CRITICAL: Sensor likely faulty if: no change >1% in 24h, physically impossible reading, wild disagreement with peers, sudden >20% jump in 5min. If faulty: do NOT act on its readings. Fall back to other sensors or last reliable value. A stuck-dry sensor causes endless irrigation—track daily cycle count.",
            "operational_sensor_failure"),
        new DocumentIngestionService.DocumentInput("general", "Manual override detection",
            "If a human manually toggled a relay (check RelayLog.isLastActionManualActivated), do NOT override. Skip automated control until manually turned off. Exception: hard safety limits still apply.",
            "operational_manual_override"),
        new DocumentIngestionService.DocumentInput("general", "Hysteresis to prevent rapid relay cycling",
            "Irrigation ON <35%, OFF >55%. Fan temp ON >=29°C, OFF <=27°C. Fan humidity ON >=90%, OFF <=85%. Light ON <200 lux, OFF >400 lux. Min between changes: 3min fans, 4h irrigation.",
            "operational_hysteresis"));
  }

  // ===========================================================================
  // SAFETY LIMITS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initSafetyLimitDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Temperature safety limits - never exceed",
            "Min safe: 5°C (frost damage). Max safe: 40°C (severe heat damage). Emergency heat at <8°C. Emergency cooling at >38°C. Hard limits—not advisory.",
            "safety_temperature"),
        new DocumentIngestionService.DocumentInput("general", "Humidity safety limits - never exceed",
            "Min safe: 20% (desiccation). Max safe: 95% (condensation, mold). Activate humidification below 30%, dehumidification above 90%. Hard limits for all crops.",
            "safety_humidity"),
        new DocumentIngestionService.DocumentInput("general", "CO2 safety limits - never exceed",
            "Min safe: 200 ppm. Max plant safe: 2000 ppm (stomatal closure). Max human safe: 5000 ppm. Ventilate above 1500 ppm. Enrich to 800-1200 ppm during daylight only.",
            "safety_co2"),
        new DocumentIngestionService.DocumentInput("general", "Soil moisture safety limits - never exceed",
            "Min safe: 15% (root death). Max safe: 95% (root asphyxiation). Emergency irrigation below 20%. Stop irrigation above 85%.",
            "safety_soil_moisture"),
        new DocumentIngestionService.DocumentInput("general", "Emergency response protocol",
            "If >38°C: fans ON, lights OFF, check irrigation. If <8°C: heaters ON, close vents. If humidity >90%: fans ON, reduce irrigation. If <30%: reduce ventilation. If CO2 >2000ppm: fans ON immediately. If soil moisture <18%: irrigate immediately. Emergency overrides all scheduled operations.",
            "emergency_protocol"),
        new DocumentIngestionService.DocumentInput("general", "Sensor drift and plausibility checks",
            "Air and soil temp within 5-10°C. Night brightness <50 lux. Battery <3.0V = maintenance needed. All soil sensors identical within 1% = communication fault. Sudden >20% swing in 5min = likely sensor fault.",
            "sensor_plausibility"));
  }

  // ===========================================================================
  // THRESHOLD TABLES
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initThresholdTablesDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Complete sensor threshold reference for all crops",
            "TEMP day optimal: 20-28°C (most), 22-30°C (cucumber/melon). TEMP night optimal: 15-20°C (most), 18-22°C (cucumber/melon). TEMP cold: <12°C day, <8°C night. TEMP hot: >32°C day, >25°C night. HUMIDITY optimal 55-75%, fungal >85%, dry <40%. SOIL optimal 40-75%, dry <30%, waterlogged >85%. LIGHT min 200 lux, too bright >1500 lux. CO2 optimal 800-1200, low <400, high >1500.",
            "threshold_reference"),
        new DocumentIngestionService.DocumentInput("general", "Time-of-day temperature thresholds",
            "Night (21-4): heat if <10°C, fans off unless humidity >90%, lights OFF. Morning (5-9): heat if <12°C, min ventilation, lights ON if dark. Noon (10-16): fans ON if >28°C, shade if >30°C, lights OFF. Evening (17-20): fans reduce gradually, lights ON if dark.",
            "threshold_time_of_day"),
        new DocumentIngestionService.DocumentInput("general", "Seasonal threshold adjustments for relay control",
            "Winter: heat at <15°C day/<12°C night, lights 6am-8pm, fans limited, irrigation -40%, humidity 60-70%. Spring: heat at <12°C night, fans at >26°C day, lights supplemental, irrigation normal. Summer: fans at >25°C aggressive, lights off except cloudy, CO2 pre-dawn. Autumn: heat at <10°C night, fans reduced, lights increasing, irrigation -20%.",
            "threshold_seasonal"));
  }

  // ===========================================================================
  // LETTUCE
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initLettuceDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("lettuce", "Lettuce climate requirements",
            "Daytime 15-22°C, nighttime 10-15°C. Above 25°C causes bolting and bitter flavor. Tolerates -2°C light frost. Humidity 60-75%, low humidity causes tipburn.",
            "lettuce_climate"),
        new DocumentIngestionService.DocumentInput("lettuce", "Lettuce irrigation requirements",
            "Shallow roots, frequent light irrigation. Soil moisture 60-80%. Cannot tolerate dry-back like tomatoes. Above 22°C: irrigate twice daily. Reduce in cool weather.",
            "lettuce_irrigation"),
        new DocumentIngestionService.DocumentInput("lettuce", "Lettuce light requirements",
            "10-14h/day at 150-300 µmol/m2/s. DLI target 10-15 mol/m2/day. Excessive light >1000 lux + heat = bolting. Winter supplemental lighting beneficial.",
            "lettuce_light"),
        new DocumentIngestionService.DocumentInput("lettuce", "Lettuce relay control rules for Line3",
            "Irrigation ON <55% soil moisture (higher threshold than tomatoes), OFF >85%. Morning 6-8am preferred. Fans at >24°C (heat sensitive). Supplemental light at 200-400 lux on dark winter days.",
            "lettuce_relay"));
  }

  // ===========================================================================
  // RADISH
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initRadishDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("radish", "Radish climate and irrigation",
            "Optimal 15-20°C, >25°C causes woody/spicy roots. Night 8-15°C. Soil moisture 50-70% consistently. Irrigate daily in warm, every 2-3 days in cool. 6-8h direct light daily.",
            "radish_care"),
        new DocumentIngestionService.DocumentInput("radish", "Radish relay control rules for Line5",
            "Irrigation ON <45% soil moisture, OFF >75%. Fans at >24°C to prevent woody roots. Primary seasons: spring and autumn.",
            "radish_relay"));
  }

  // ===========================================================================
  // MELON
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initMelonDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("melon", "Melon climate and management",
            "Daytime 24-32°C, nighttime 18-24°C. Growth slows <18°C, fruit fails <20°C average. Above 35°C flower abortion. Humidity 60-75% growth, 50-60% ripening. Deep less-frequent irrigation. Soil moisture 40-65%. Reduce irrigation during ripening for flavor.",
            "melon_climate"),
        new DocumentIngestionService.DocumentInput("melon", "Melon relay control rules for Line6",
            "Irrigation ON <35% soil moisture (deep watering), OFF >70%. Fans at >30°C aggressively. Supplemental light only on cloudy winter days. Summer crop—heating rarely needed.",
            "melon_relay"));
  }

  // ===========================================================================
  // GRAPE (WINE)
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initGrapeDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("grape", "Grape (wine) climate and irrigation",
            "Daytime 22-30°C, nighttime 12-18°C. Needs temp differential for sugar. Above 35°C stops photosynthesis. Humidity 50-65%, >75% fungal risk. Soil moisture 30-55%. Deep infrequent irrigation. Stop 2-3 weeks before harvest for sugar concentration.",
            "grape_care"),
        new DocumentIngestionService.DocumentInput("grape", "Grape relay control rules for wine pump",
            "Irrigate via relay_wine_pump ON <25% soil moisture, OFF >55%. Deep water every 5-7 days. Active Apr-Sep, minimal Oct-Mar. Check rain_indicator before irrigating in wet weather.",
            "grape_relay"));
  }

  // ===========================================================================
  // GROWTH STAGE MANAGEMENT
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initGrowthStageDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "Growth stage seedling management",
            "First 2-3 weeks: temperature 22-26°C constant (2-3°C warmer than mature). Humidity 70-80%. Light 16h/day at 200-400 lux. Soil moisture 60-75% consistently. CO2 600-800 ppm. Avoid direct fans on seedlings.",
            "growth_seedling"),
        new DocumentIngestionService.DocumentInput("general", "Growth stage vegetative management",
            "Temperature 22-28°C day, 16-20°C night. Humidity 60-70%. Light 14-16h/day at 300-600 lux. Allow dry-back to 35-40%. CO2 800-1000 ppm during peak light.",
            "growth_vegetative"),
        new DocumentIngestionService.DocumentInput("general", "Growth stage flowering and fruiting management",
            "Temperature 20-26°C day, 15-18°C night. >30°C during flowering = poor fruit set. Humidity 50-60% for pollination. Light 12-14h/day. Soil moisture 50-65% consistent. CO2 800-1000 ppm daylight. Good airflow critical.",
            "growth_fruiting"),
        new DocumentIngestionService.DocumentInput("general", "Growth stage ripening and harvest management",
            "Temperature 20-26°C. Humidity 50-55%. Reduce irrigation 30-40% to concentrate flavors. Light 12h/day. Increase airflow around fruits to prevent rot.",
            "growth_ripening"));
  }

  // ===========================================================================
  // TREND INTERPRETATION
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initTrendInterpretationDocuments() {
    return List.of(
        new DocumentIngestionService.DocumentInput("general", "How to interpret sensor trends",
            "Rising temp >3°C/24h near 28°C: activate fans proactively. Falling temp <-3°C/24h near 15°C: check heating. Rapidly falling soil moisture: irrigate soon even if in range. Rising humidity >10%/24h approaching 80%: increase ventilation preemptively.",
            "trend_interpretation"),
        new DocumentIngestionService.DocumentInput("general", "Rate of change thresholds for action",
            "Temp rising >2°C/h: pre-activate fans. Temp falling >2°C/h: check heating, close vents. Humidity rising >5%/h: ventilate immediately. Soil moisture falling >5%/h: schedule irrigation soon. CO2 rising >100ppm/h above 1200ppm: ventilation insufficient.",
            "rate_of_change"),
        new DocumentIngestionService.DocumentInput("general", "Combined multi-sensor risk matrix",
            "HIGH TEMP >30°C + LOW SOIL <30%: CRITICAL - irrigate AND ventilate. HIGH TEMP >30°C + LOW HUMIDITY <40%: high VPD - humidify AND ventilate. HIGH HUMIDITY >85% + LOW LIGHT <200 lux: fungal risk - fans ON. HIGH HUMIDITY >85% + HIGH SOIL >85%: root rot - stop irrigation, fans ON. LOW TEMP <12°C + HIGH HUMIDITY >80%: mold - heat AND ventilate. LOW CO2 <350ppm + HIGH LIGHT >800 lux: ventilate or enrich. HIGH TEMP NIGHT >24°C: plant stress.",
            "risk_matrix"),
        new DocumentIngestionService.DocumentInput("general", "VPD calculation and interpretation",
            "VPD = 0.6108 * exp(17.27*T/(T+237.3)) * (1-RH/100) kPa. Ideal 0.8-1.2 kPa. Low (<0.4): fungal risk, Ca deficiency. High (>1.6): water stress, stomatal closure. Low VPD: increase temp, decrease humidity. High VPD: decrease temp, increase humidity.",
            "vpd_calculation"));
  }
}

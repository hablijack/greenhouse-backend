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
  void initInternal() {
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
            "cooling"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Time-of-day temperature interpretation",
            "The same temperature must be interpreted differently depending on the time of day. "
                + "In the early morning (5-9am) plants have naturally cooled overnight, so 12-15°C is acceptable. "
                + "At midday (10am-4pm) plants are photosynthesizing actively and need warmth; "
                + "12°C at noon is dangerously cold and indicates a heating failure. "
                + "High temperatures at night (above 24°C) prevent plants from resting and recovering. "
                + "Always consider the time of day when evaluating whether a temperature is problematic.",
            "time_of_day_sensor"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Seasonal greenhouse management",
            "Greenhouse management must adapt to the current season. In winter (Dec-Feb) the focus is on "
                + "heating, insulation, and supplemental lighting due to short daylight hours and cold temperatures. "
                + "In spring (Mar-May) watch for sudden temperature swings between warm days and cold nights. "
                + "In summer (Jun-Aug) prioritize cooling, shading, ventilation, and morning CO2 enrichment. "
                + "In autumn (Sep-Nov) reduce irrigation as temperatures drop and prepare heating systems. "
                + "Season-appropriate strategies prevent plant stress and save energy.",
            "seasonal_management"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Day-night temperature differential for plant growth",
            "Many greenhouse crops benefit from a day-night temperature differential (DIF). "
                + "A cooler night (4-8°C below daytime) promotes strong stems, compact growth, "
                + "and better fruit set in tomatoes. "
                + "Consistently warm nights cause elongation, weak growth, and poor fruit quality. "
                + "However, night temperatures below 10°C damage sensitive crops. "
                + "The ideal night temperature for tomatoes is 16-20°C, for cucumbers 18-22°C.",
            "dif_temperature")

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

  // ===========================================================================
  // RELAY CONTROL LOGIC
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initRelayControlDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Relay overview and mapping",
            "The greenhouse has 8 controllable relays on the main ESP32 and 1 on the wine satellite. "
                + "relay_line1 to relay_line6 control irrigation for planting lines 1 through 6. "
                + "relay_line7 controls the supplemental LED grow lights. "
                + "relay_line8 controls the ventilation fans. "
                + "relay_wine_pump controls the irrigation pump for the wine grapes. "
                + "Each relay is normally in the OFF (false) state and turns ON (true) when activated.",
            "relay_overview"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Irrigation relay control rules",
            "Each irrigation relay (relay_line1 through relay_line6, relay_wine_pump) waters a specific planting line. "
                + "Irrigation should be triggered when the corresponding soil moisture sensor drops below 35%. "
                + "Never irrigate when soil moisture is above 80% - this causes waterlogging and root rot. "
                + "Irrigation duration should be 15-30 minutes per session. "
                + "Avoid irrigating during peak midday heat (11am-3pm) to reduce evaporation loss. "
                + "Morning irrigation (6-9am) is preferred. Reduce winter irrigation frequency by 40%.",
            "relay_irrigation"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Fan relay control rules",
            "relay_line8 controls the ventilation fans. Fans should be activated when: "
                + "inside temperature exceeds 28°C, or humidity exceeds 80%, or CO2 exceeds 1200ppm. "
                + "Fans should run continuously during heat waves (above 32°C). "
                + "Nighttime fan operation (below 20°C inside) should be minimized to retain heat. "
                + "During winter, use intermittent fan cycles (10min on, 30min off) to balance "
                + "ventilation with heat retention. Fans must run during high humidity periods (>85%) "
                + "regardless of temperature to prevent fungal outbreaks.",
            "relay_fans"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Light relay control rules",
            "relay_line7 controls the supplemental LED grow lights. "
                + "Lights should be activated when brightness drops below 200 lux during daytime hours (6am-8pm). "
                + "Lights should NEVER be on during nighttime (9pm-5am) to maintain natural day-night rhythm. "
                + "In winter, extend lighting period to 12-14 hours total. In summer, use only on cloudy days. "
                + "Target daily light integral: 15-25 mol/m2/day for tomatoes, 10-15 for lettuce.",
            "relay_lighting"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Relay priority and conflict resolution",
            "When multiple conditions conflict, follow this priority order (highest first): "
                + "1. Emergency: never override safety limits. "
                + "2. Fan operation during critical humidity (>85%) or temperature (>35°C) must always activate. "
                + "3. Fan and light can run simultaneously - no conflict. "
                + "4. Irrigation and fan can run simultaneously - no conflict. "
                + "5. During extreme heat (>35°C) prioritize cooling over CO2 retention and irrigation scheduling. "
                + "6. During power constraints, fans take priority over lights.",
            "relay_priority"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Relay automation decision framework",
            "When deciding whether to activate a relay, consider: "
                + "1. Current sensor values and their severity. "
                + "2. Time of day (morning irrigation preferred, lights off at night, fans moderate at night). "
                + "3. Season (winter: heat retention priority, summer: cooling priority). "
                + "4. Trend direction (rapidly rising temp needs proactive fan activation). "
                + "5. Combined risks (high temp + low moisture = urgent irrigation + fan). "
                + "6. Rate of change - act proactively before thresholds are breached.",
            "relay_decision_framework")

    );
  }

  // ===========================================================================
  // OPERATIONAL SAFETY LIMITS (Duty Cycles, Power, Sensor Failure)
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initOperationalSafetyDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Irrigation duty cycle limits - sensor failure protection",
            "CRITICAL: Irrigation relays (relay_line1-6, relay_wine_pump) have hard operational limits "
                + "to prevent flooding from sensor failures or LLM hallucinations: "
                + "MAXIMUM IRRIGATIONS PER DAY: 3 cycles per line. "
                + "MAXIMUM ON DURATION PER CYCLE: 60 seconds (60000ms). "
                + "MINIMUM COOLDOWN BETWEEN CYCLES: 4 hours. "
                + "If a soil moisture sensor reports 'dry' (below 30%) but the relay has already "
                + "irrigated 3 times today, do NOT irrigate again - the sensor is likely faulty. "
                + "Instead, flag the sensor as potentially defective. "
                + "Total daily irrigation budget per line: max 180 seconds (3 x 60s). "
                + "These limits are NEVER overridden - they prevent catastrophic overwatering.",
            "operational_irrigation"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Fan duty cycle limits - power management",
            "CRITICAL: The ventilation fan (relay_line8) runs on solar battery power. "
                + "Operational limits to prevent battery drain: "
                + "MAXIMUM CONTINUOUS ON DURATION: 4 hours (14400000ms). "
                + "MINIMUM COOLDOWN AFTER MAX DURATION: 5 minutes (300000ms). "
                + "MINIMUM ON TIME: 3 minutes (180000ms) - prevents rapid cycling. "
                + "MINIMUM OFF TIME: 3 minutes (180000ms) - allows settling. "
                + "Hysteresis ON threshold: temperature >= 29°C OR humidity >= 90%. "
                + "Hysteresis OFF threshold: temperature <= 27°C AND humidity <= 85%. "
                + "If fan has run for 4 hours continuously, force it OFF for 5 minutes cooldown. "
                + "These limits protect the battery and fan motor from damage.",
            "operational_fan"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Fan power availability - solar dependency",
            "CRITICAL: Fans require sufficient solar power to operate without draining the battery. "
                + "Fans should ONLY be activated when BOTH conditions are met: "
                + "1. Brightness sensor >= 2500 lux (sufficient sunlight for solar panels). "
                + "2. Current time is within the activation window: 8:00-17:00 (8am-5pm). "
                + "If brightness is below 2500 lux OR outside 8am-5pm, fans must remain OFF "
                + "regardless of temperature or humidity, UNLESS emergency conditions exist. "
                + "Emergency exception: if temperature exceeds 38°C OR humidity exceeds 95%, "
                + "fans may run briefly (max 10 minutes) even without solar power. "
                + "This solar-gated operation prevents battery depletion during cloudy periods and at night.",
            "operational_fan_power"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Sensor failure detection and handling",
            "CRITICAL: Detect and handle sensor failures to prevent incorrect relay actions. "
                + "A sensor is likely faulty if: "
                + "1. Reading has not changed by more than 1% in the last 24 hours (stuck sensor). "
                + "2. Reading is outside physically plausible range (e.g. temperature > 60°C inside greenhouse). "
                + "3. Multiple related sensors disagree wildly (e.g. one soil line at 20% while all others at 60%). "
                + "4. Reading shows sudden impossible jumps (>20% change in 5 minutes). "
                + "When a sensor is suspected faulty: "
                + "- Do NOT act on its readings for relay decisions. "
                + "- Fall back to other sensors of the same type if available (e.g. other soil lines). "
                + "- If no fallback sensor exists, use the last reliable reading. "
                + "- Flag the sensor for human inspection. "
                + "A stuck-dry sensor is especially dangerous - it would cause endless irrigation. "
                + "Track each relay's daily cycle count to detect this condition.",
            "operational_sensor_failure"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Manual override detection",
            "If a human has manually toggled a relay, the LLM must NOT override that action. "
                + "Check RelayLog.isLastActionManualActivated(relay) - if true, the last action "
                + "was performed by a human via the web interface. "
                + "When a relay has been manually activated: skip automated control for that relay "
                + "until a human manually turns it off again. "
                + "This prevents the LLM from fighting with manual operator decisions. "
                + "The only exception is the safety layer - hard safety limits still apply regardless.",
            "operational_manual_override"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Hysteresis to prevent rapid relay cycling",
            "All relays must use hysteresis (different ON and OFF thresholds) to prevent rapid cycling: "
                + "IRRIGATION: ON at <35% soil moisture, OFF at >55% (20% hysteresis band). "
                + "FAN: ON at >=29°C, OFF at <=27°C (2°C hysteresis). "
                + "FAN humidity: ON at >=90%, OFF at <=85% (5% hysteresis). "
                + "LIGHT: ON at <200 lux, OFF at >400 lux. "
                + "Minimum time between state changes: 3 minutes for fans, 4 hours for irrigation. "
                + "Hysteresis prevents relays from oscillating on/off rapidly, "
                + "which damages relay hardware and stresses plants.",
            "operational_hysteresis")

    );
  }

  // ===========================================================================
  // SAFETY LIMITS
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initSafetyLimitDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Temperature safety limits - never exceed",
            "Absolute safety limits that must never be exceeded: "
                + "Minimum safe temperature: 5°C (all plants risk frost damage below this). "
                + "Maximum safe temperature: 40°C (all plants risk severe heat damage above this). "
                + "Activate emergency heating when temp drops below 8°C. "
                + "Activate emergency cooling when temp exceeds 38°C. "
                + "These are hard safety limits - not advisory thresholds.",
            "safety_temperature"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Humidity safety limits - never exceed",
            "Absolute humidity safety limits: "
                + "Minimum safe humidity: 20% (below this causes desiccation and severe stress). "
                + "Maximum safe humidity: 95% (above this causes condensation and immediate mold risk). "
                + "Activate humidification below 30%. Activate dehumidification above 90%. "
                + "These are hard safety limits for all crops.",
            "safety_humidity"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "CO2 safety limits - never exceed",
            "CO2 safety limits for plant and human safety: "
                + "Minimum safe CO2: 200 ppm (below this severely limits photosynthesis). "
                + "Maximum safe CO2 for plants: 2000 ppm (above this causes stomatal closure). "
                + "Maximum safe CO2 for humans: 5000 ppm (workplace safety limit). "
                + "Activate ventilation above 1500 ppm CO2. "
                + "CO2 enrichment should target 800-1200 ppm during daylight hours only.",
            "safety_co2"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Soil moisture safety limits - never exceed",
            "Soil moisture safety limits: "
                + "Minimum safe soil moisture: 15% (below this roots dehydrate and die). "
                + "Maximum safe soil moisture: 95% (above this causes root asphyxiation). "
                + "Activate emergency irrigation below 20%. "
                + "Stop all irrigation above 85% to prevent waterlogging.",
            "safety_soil_moisture"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Emergency response protocol",
            "When any sensor exceeds a safety limit: "
                + "1. IMMEDIATELY activate the appropriate relay to counter the condition. "
                + "2. If temperature >38°C: fans ON, lights OFF, check irrigation. "
                + "3. If temperature <8°C: heaters ON, close all ventilation. "
                + "4. If humidity >90%: fans ON, reduce irrigation. "
                + "5. If humidity <30%: reduce ventilation, consider humidification. "
                + "6. If CO2 >2000ppm: fans ON immediately. "
                + "7. If soil moisture <18%: irrigate immediately regardless of time. "
                + "Emergency actions override all scheduled operations.",
            "emergency_protocol"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Sensor drift and plausibility checks",
            "If a sensor reading seems implausible, compare with related sensors: "
                + "Air temperature and soil temperature should be within 5-10°C of each other. "
                + "Brightness at night should be below 50 lux - higher values suggest light leak or sensor fault. "
                + "Battery voltage below 3.0V indicates the sensor node needs maintenance. "
                + "If all soil moisture sensors read identically (within 1%), there may be a communication fault. "
                + "Sudden 20%+ swings in any sensor within 5 minutes "
                + "likely indicate a sensor fault, not a real change.",
            "sensor_plausibility")

    );
  }

  // ===========================================================================
  // THRESHOLD TABLES
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initThresholdTablesDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Complete sensor threshold reference for all crops",
            "Central threshold reference for relay control decisions: "
                + "TEMPERATURE daytime optimal: 20-28°C (most crops), 22-30°C (cucumber, melon). "
                + "TEMPERATURE nighttime optimal: 15-20°C (most crops), 18-22°C (cucumber, melon). "
                + "TEMPERATURE too cold: <12°C daytime, <8°C nighttime. "
                + "TEMPERATURE too hot: >32°C daytime, >25°C nighttime. "
                + "HUMIDITY optimal: 55-75% all crops. "
                + "HUMIDITY fungal risk: >85% all crops. "
                + "HUMIDITY too dry: <40% all crops. "
                + "SOIL MOISTURE optimal: 40-75% all crops. "
                + "SOIL MOISTURE too dry: <30%. "
                + "SOIL MOISTURE waterlogged: >85%. "
                + "LIGHT minimum for growth: 200 lux. "
                + "LIGHT too bright: >1500 lux (risk of leaf burn). "
                + "CO2 optimal: 800-1200 ppm during daylight. "
                + "CO2 too low: <400 ppm. CO2 too high: >1500 ppm.",
            "threshold_reference"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Time-of-day temperature thresholds for relay decisions",
            "Temperature thresholds vary by time of day for relay control: "
                + "NIGHT (21-4): heat if <10°C, fans off unless humidity >90%, lights OFF. "
                + "MORNING (5-9): heat if <12°C, minimal ventilation, lights ON if dark. "
                + "NOON (10-16): fans ON if >28°C, shade if >30°C, lights OFF. "
                + "EVENING (17-20): fans reduce gradually, lights ON if dark. "
                + "These time-aware thresholds prevent unnecessary relay cycling "
                + "while ensuring plants get appropriate conditions throughout the day.",
            "threshold_time_of_day"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Seasonal threshold adjustments for relay control",
            "Thresholds shift by season for optimal relay management: "
                + "WINTER (Dec-Feb): heating active at <15°C daytime, <12°C nighttime. "
                + "Lights on 6am-8pm daily. Fans limited to prevent heat loss. "
                + "Irrigation reduced by 40%. Target humidity 60-70%. "
                + "SPRING (Mar-May): heating at <12°C night, fans at >26°C day. "
                + "Lights supplemental only. Irrigation normal. "
                + "SUMMER (Jun-Aug): no heating needed. Fans at >25°C aggressive cooling. "
                + "Lights off except cloudy days. CO2 enrichment pre-dawn. "
                + "AUTUMN (Sep-Nov): heating at <10°C night. Fans reduced. "
                + "Lights increasing as days shorten. Irrigation reduced 20%.",
            "threshold_seasonal")

    );
  }

  // ===========================================================================
  // LETTUCE
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initLettuceDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "lettuce",
            "Lettuce climate requirements",
            "Lettuce is a cool-season crop. Optimal daytime temperature: 15-22°C. "
                + "Nighttime temperature: 10-15°C. Temperatures above 25°C cause bolting, bitter flavor, "
                + "and poor head formation. Lettuce tolerates light frost (down to -2°C) but is damaged "
                + "by hard frost. Humidity should be 60-75%. Low humidity causes tipburn on leaf edges.",
            "lettuce_climate"),

        new DocumentIngestionService.DocumentInput(
            "lettuce",
            "Lettuce irrigation requirements",
            "Lettuce has shallow roots and requires frequent, light irrigation. "
                + "Soil moisture should remain consistently at 60-80%. Unlike tomatoes, lettuce cannot "
                + "tolerate dry-back cycles. Uneven irrigation causes tipburn and poor growth. "
                + "In hot weather (>22°C), irrigate twice daily. Reduce irrigation in cool weather.",
            "lettuce_irrigation"),

        new DocumentIngestionService.DocumentInput(
            "lettuce",
            "Lettuce light requirements",
            "Lettuce requires 10-14 hours of light per day at 150-300 µmol/m2/s. "
                + "Daily light integral target: 10-15 mol/m2/day. Excessive light (>1000 lux) "
                + "combined with heat causes bolting. Supplemental lighting in winter is beneficial.",
            "lettuce_light"),

        new DocumentIngestionService.DocumentInput(
            "lettuce",
            "Lettuce relay control rules",
            "Lettuce-specific relay decisions for relay_line3 (Salat line): "
                + "Activate irrigation when soil moisture drops below 55% (higher threshold than tomatoes). "
                + "Stop irrigation above 85%. Prefer early morning irrigation (6-8am). "
                + "Activate fans when temperature exceeds 24°C - lettuce is heat sensitive. "
                + "Supplemental lighting at 200-400 lux during dark winter days.",
            "lettuce_relay")

    );
  }

  // ===========================================================================
  // RADISH
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initRadishDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "radish",
            "Radish climate and irrigation",
            "Radishes are fast-growing cool-season crops (25-35 days to harvest). "
                + "Optimal temperature: 15-20°C. Above 25°C radishes become woody, pithy, and overly spicy. "
                + "Night temperature: 8-15°C. Radishes need consistent moisture - fluctuations cause "
                + "splitting and poor root quality. Soil moisture target: 50-70%. "
                + "Irrigate daily in warm weather, every 2-3 days in cool weather. "
                + "Radishes need 6-8 hours of direct light daily. Low light produces leafy tops with small roots.",
            "radish_care"),

        new DocumentIngestionService.DocumentInput(
            "radish",
            "Radish relay control rules",
            "Radish-specific relay decisions for relay_line5: "
                + "Irrigation when soil moisture <45% (radishes need consistent moisture). "
                + "Stop irrigation above 75%. Fans at >24°C to prevent woody roots. "
                + "Radish season is primarily spring and autumn - adjust fan/heating accordingly.",
            "radish_relay")

    );
  }

  // ===========================================================================
  // MELON
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initMelonDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "melon",
            "Melon climate and management",
            "Melons are warm-season crops requiring high temperatures: 24-32°C daytime, 18-24°C nighttime. "
                + "Growth slows below 18°C. Fruits fail to ripen properly below 20°C average. "
                + "Above 35°C flowers abort and fruit set fails. Humidity: 60-75% during growth, "
                + "reduce to 50-60% during fruit ripening to maximize sugar content. "
                + "Melons are deep-rooted and require deep, less frequent irrigation. "
                + "Soil moisture target: 40-65%. Reduce irrigation during fruit ripening for better flavor.",
            "melon_climate"),

        new DocumentIngestionService.DocumentInput(
            "melon",
            "Melon relay control rules",
            "Melon-specific relay decisions for relay_line6: "
                + "Irrigation when soil moisture <35% - deep watering. Stop at 70%. "
                + "Fans at >30°C aggressively. Melons need high heat but also airflow to prevent mildew. "
                + "Supplemental lighting only during cloudy winter days if greenhouse is used year-round. "
                + "Melons are summer crops - heating is rarely needed if seasonally appropriate.",
            "melon_relay")

    );
  }

  // ===========================================================================
  // GRAPE (WINE)
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initGrapeDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "grape",
            "Grape (wine) climate and irrigation",
            "Wine grapes prefer 22-30°C daytime and 12-18°C nighttime during growing season. "
                + "Grapes require a distinct temperature differential for optimal sugar development. "
                + "Above 35°C photosynthesis stops and grapes may sunburn. Below 10°C growth stops. "
                + "Humidity: 50-65%. High humidity (>75%) increases fungal disease risk (powdery mildew, botrytis). "
                + "Grapes are drought-tolerant once established. Soil moisture target: 30-55%. "
                + "Deep, infrequent irrigation encourages deep root growth. "
                + "Stop irrigation 2-3 weeks before harvest to concentrate sugars.",
            "grape_care"),

        new DocumentIngestionService.DocumentInput(
            "grape",
            "Grape relay control rules for wine pump",
            "Wine grape irrigation uses relay_wine_pump to pump water from a rain barrel. "
                + "Irrigate when soil moisture drops below 25% (grapes are drought tolerant). "
                + "Stop at 55%. Water deeply once every 5-7 days rather than frequent shallow watering. "
                + "Grapes are outdoor plants - the wine satellite only controls irrigation, not climate. "
                + "Seasonal pattern: irrigate actively Apr-Sep, minimal Oct-Mar. "
                + "During wet weather, check rain_indicator sensor before irrigating.",
            "grape_relay")

    );
  }

  // ===========================================================================
  // GROWTH STAGE MANAGEMENT
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initGrowthStageDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "Growth stage seedling management",
            "Seedlings and young transplants (first 2-3 weeks after planting): "
                + "Temperature: 2-3°C warmer than mature plants, 22-26°C constant. "
                + "Humidity: higher at 70-80% to reduce transplant shock. "
                + "Light: 16 hours/day at moderate intensity (200-400 lux). "
                + "Soil moisture: consistently moist at 60-75%, never allowed to dry out. "
                + "CO2: 600-800 ppm to accelerate early growth. "
                + "Avoid fans directly on seedlings - use gentle indirect airflow.",
            "growth_seedling"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Growth stage vegetative management",
            "Vegetative growth phase (after establishment, before flowering): "
                + "Temperature standard: 22-28°C daytime, 16-20°C nighttime. "
                + "Humidity: 60-70%. Increase airflow to strengthen stems. "
                + "Light: 14-16 hours/day at 300-600 lux. "
                + "Soil moisture: allow moderate dry-back to 35-40% between irrigation. "
                + "CO2: 800-1000 ppm during peak light hours for maximum growth.",
            "growth_vegetative"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Growth stage flowering and fruiting management",
            "Flowering and fruiting stage: "
                + "Temperature: slightly cooler at 20-26°C daytime, 15-18°C nighttime. "
                + "High temperatures (>30°C) during flowering cause poor fruit set. "
                + "Humidity: reduce to 50-60% during flowering to improve pollination. "
                + "Light: maintain 12-14 hours/day - shorten for some short-day plants. "
                + "Soil moisture: consistent at 50-65%, avoid drought stress during fruit development. "
                + "CO2: continue 800-1000 ppm during daylight. "
                + "Good airflow is critical during flowering for pollination and disease prevention.",
            "growth_fruiting"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Growth stage ripening and harvest management",
            "Ripening and harvest stage: "
                + "Temperature: maintain 20-26°C for fruit ripening - avoid extremes. "
                + "Humidity: reduce to 50-55% to prevent fruit rot and fungal issues. "
                + "Reduce irrigation by 30-40% to concentrate flavors (for tomatoes, melons, grapes). "
                + "Light: maintain 12 hours/day. CO2 enrichment less critical. "
                + "Increase airflow around ripening fruits to prevent botrytis and rot.",
            "growth_ripening")

    );
  }

  // ===========================================================================
  // TREND INTERPRETATION
  // ===========================================================================

  private List<DocumentIngestionService.DocumentInput> initTrendInterpretationDocuments() {

    return List.of(

        new DocumentIngestionService.DocumentInput(
            "general",
            "How to interpret sensor trends",
            "Historical sensor trends provide critical context for decisions: "
                + "A rising temperature trend (>3°C/24h) with current temp near 28°C means "
                + "fans should be activated proactively before the temperature hits 30°C. "
                + "A falling temperature trend (<-3°C/24h) with current temp near 15°C means "
                + "heating should be checked proactively. "
                + "A rapidly falling soil moisture trend indicates irrigation is needed soon, "
                + "even if current moisture is still in range. "
                + "Rising humidity trend (>10%/24h) approaching 80% means increase ventilation preemptively. "
                + "Use trend direction to act proactively rather than reactively.",
            "trend_interpretation"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Rate of change thresholds for action",
            "Rate of change triggers for proactive relay control: "
                + "Temperature rising >2°C/hour: pre-activate fans, check shading. "
                + "Temperature falling >2°C/hour: check heating, close vents. "
                + "Humidity rising >5%/hour: increase ventilation immediately. "
                + "Soil moisture falling >5%/hour in optimal range: schedule irrigation soon. "
                + "CO2 rising >100ppm/hour above 1200ppm: ventilation may be insufficient. "
                + "These rate-of-change triggers enable proactive rather than reactive control.",
            "rate_of_change"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "Combined multi-sensor risk matrix",
            "Combined sensor patterns requiring immediate action: "
                + "HIGH TEMP (>30°C) + LOW SOIL (<30%): CRITICAL - irrigate AND ventilate NOW. "
                + "HIGH TEMP (>30°C) + LOW HUMIDITY (<40%): HIGH VPD - humidify AND ventilate. "
                + "HIGH HUMIDITY (>85%) + LOW LIGHT (<200 lux): FUNGAL RISK - fans ON, lights ON. "
                + "HIGH HUMIDITY (>85%) + HIGH SOIL (>85%): ROOT ROT RISK - stop irrigation, fans ON. "
                + "LOW TEMP (<12°C) + HIGH HUMIDITY (>80%): MOLD RISK - heat AND ventilate carefully. "
                + "LOW CO2 (<350ppm) + HIGH LIGHT (>800 lux): Photosynthesis limited - ventilate or enrich. "
                + "HIGH TEMP NIGHT (>24°C): Plant stress - ventilate at night if possible. "
                + "Each combined pattern requires addressing BOTH factors simultaneously.",
            "risk_matrix"),

        new DocumentIngestionService.DocumentInput(
            "general",
            "VPD calculation and interpretation",
            "Vapor Pressure Deficit (VPD) combines temperature and humidity into a single stress metric: "
                + "VPD = 0.6108 * exp(17.27 * T / (T + 237.3)) * (1 - RH/100) in kPa. "
                + "Ideal VPD range: 0.8-1.2 kPa for most crops. "
                + "Low VPD (<0.4 kPa): transpiration too slow, fungal risk, calcium deficiency risk. "
                + "High VPD (>1.6 kPa): transpiration too fast, water stress, stomatal closure. "
                + "Action for low VPD: increase temperature, decrease humidity (ventilate). "
                + "Action for high VPD: decrease temperature, increase humidity (humidify, reduce ventilation).",
            "vpd_calculation")

    );
  }
}

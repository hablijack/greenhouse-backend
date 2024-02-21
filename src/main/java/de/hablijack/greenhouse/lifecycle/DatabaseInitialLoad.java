package de.hablijack.greenhouse.lifecycle;

import de.hablijack.greenhouse.entity.ConditionTrigger;
import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.entity.TimeTrigger;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@SuppressWarnings("checkstyle:RegexpSingleline")
@Startup
@ApplicationScoped
public class DatabaseInitialLoad {

  private static final Logger LOGGER = Logger.getLogger(DatabaseInitialLoad.class.getName());
  private final Double MIN_AIR_INSIDE_TEMP = 15.0;
  private final Double MAX_AIR_INSIDE_TEMP = 40.0;
  private final Double MIN_AIR_OUTSIDE_TEMP = 15.0;
  private final Double MAX_AIR_OUTSIDE_TEMP = 40.0;
  private final Double MIN_SOIL_TEMP = 15.0;
  private final Double MAX_SOIL_TEMP = 40.0;
  private final Double MIN_BATTERY = 60.0;
  private final Double MAX_BATTERY = 100.0;
  private final Double MIN_AIR_HUMIDITY_INSIDE = 50.0;
  private final Double MAX_AIR_HUMIDITY_INSIDE = 70.0;
  private final Double MIN_WIFI_STRENGTH = 60.0;
  private final Double MAX_WIFI_STRENGTH = 100.0;
  private final Double MIN_CO2_VALUE = 300.0;
  private final Double MAX_CO2_VALUE = 2800.0;
  private final Double MIN_LIGHT_VALUE = 3000.0;
  private final Double MAX_LIGHT_VALUE = 7000.0;
  private final Double MIN_SOIL_HUMIDITY_INSIDE = 10.0;
  private final Double MAX_SOIL_HUMIDITY_INSIDE = 30.0;

  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  SatelliteService satelliteService;

  @Transactional
  @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:MethodLength", "checkstyle:LineLength", "PMD"})
  public void initializeWithBaseData(@Observes StartupEvent event) throws InterruptedException {
    LOGGER.info("... filling database...");
    new Satellite(
        "greenhouse_cam",
        "Gewächshaus Webcam",
        "satelite.png",
        "ESP-EYE Webcam-webserver um regelmäßig Bilder innerhalb des Gewächshauses aufzunehmen.",
        "192.168.178.73",
        true).persistIfNotExist();
    Satellite greenhouseControl = new Satellite(
        "greenhouse_control",
        "Gewächshaus Steuerung",
        "satelite.png",
        "ESP32 Webserver mit Relays, Sensoren und Aktoren um das gesamte Gewächshaus fernzusteuern.",
        "192.168.178.37",
        true).persistIfNotExist();
    Satellite wineSatellite = new Satellite(
        "wine_satellite",
        "Wein Bewässerung",
        "satelite.png",
        "ESP-Lolin32 Webserver mit einem Relays, um eine Pumpe für die Weinbewässerung anzustarten.",
        "192.168.178.198",
        true).persistIfNotExist();
    /* ============================================================================================================= */
    Sensor airTempInside = new Sensor(
        "air_temp_inside",
        "Luft innen",
        "°C",
        1,
        "Themperatur innerhalb des Gewächshauses",
        "mdi-thermometer",
        MIN_AIR_INSIDE_TEMP,
        MAX_AIR_INSIDE_TEMP,
        1,
        true).persistIfNotExist();
    new Measurement(airTempInside, 20.5, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor airTempOutside = new Sensor(
        "air_temp_outside",
        "Luft außen",
        "°C",
        1,
        "Themperatur außerhalb des Gewächshauses",
        "mdi-thermometer",
        MIN_AIR_OUTSIDE_TEMP,
        MAX_AIR_OUTSIDE_TEMP,
        2,
        true).persistIfNotExist();
    new Measurement(airTempOutside, 18.2, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor soilTempInside = new Sensor(
        "soil_temp_inside",
        "Boden",
        "°C",
        1,
        "Themperatur der Erde im Gewächshaus",
        "mdi-thermometer",
        MIN_SOIL_TEMP,
        MAX_SOIL_TEMP,
        3,
        true).persistIfNotExist();
    new Measurement(soilTempInside, 14.2, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor battery = new Sensor(
        "battery_voltage",
        "Batterie",
        "%",
        0,
        "Ladezustand der Relay-Batterie",
        "mdi-battery",
        MIN_BATTERY,
        MAX_BATTERY,
        14,
        false).persistIfNotExist();
    new Measurement(battery, 90.2, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor airHumidityInside = new Sensor(
        "air_humidity_inside",
        "Luft innen",
        "%",
        0,
        "Luftfeuchte im Gewächshaus",
        "mdi-water",
        MIN_AIR_HUMIDITY_INSIDE,
        MAX_AIR_HUMIDITY_INSIDE,
        4,
        true).persistIfNotExist();
    new Measurement(airHumidityInside, 99.1, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor wifi = new Sensor(
        "wifi",
        "WiFi",
        "dBm",
        0,
        "W-Lan Empfangsstärke im Gewächshaus",
        "mdi-wifi",
        MIN_WIFI_STRENGTH,
        MAX_WIFI_STRENGTH,
        7,
        true).persistIfNotExist();
    new Measurement(wifi, 70.0, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor brightness = new Sensor(
        "brightness",
        "Helligkeit",
        "lux",
        0,
        "Intensität der Sonneneinstrahlung im Gewächshaus",
        "mdi-white-balance-sunny",
        MIN_LIGHT_VALUE,
        MAX_LIGHT_VALUE,
        6,
        true).persistIfNotExist();
    new Measurement(brightness, 1800.8, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor co2 = new Sensor(
        "co2",
        "CO2",
        "ppm",
        0,
        "CO2 Sättigung im Gewächshaus",
        "mdi-soundcloud",
        MIN_CO2_VALUE,
        MAX_CO2_VALUE,
        5,
        true).persistIfNotExist();
    new Measurement(co2, 7000.8, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor soilHumidityLine1 = new Sensor(
        "soil_humidity_line1",
        "Bodenfeuchte Line1",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 1",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE,
        8,
        true).persistIfNotExist();
    new Measurement(soilHumidityLine1, 70.8, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor soilHumidityLine2 = new Sensor(
        "soil_humidity_line2",
        "Bodenfeuchte Line2",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 2",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE,
        9,
        true).persistIfNotExist();
    new Measurement(soilHumidityLine2, 74.2, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor soilHumidityLine3 = new Sensor(
        "soil_humidity_line3",
        "Bodenfeuchte Line3",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 3",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE,
        10,
        true).persistIfNotExist();
    new Measurement(soilHumidityLine3, 88.8, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor soilHumidityLine4 = new Sensor(
        "soil_humidity_line4",
        "Bodenfeuchte Line4",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 4",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE,
        11,
        true).persistIfNotExist();
    new Measurement(soilHumidityLine4, 88.8, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor soilHumidityLine5 = new Sensor(
        "soil_humidity_line5",
        "Bodenfeuchte Line5",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 5",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE,
        12,
        true).persistIfNotExist();
    new Measurement(soilHumidityLine5, 88.8, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor soilHumidityLine6 = new Sensor(
        "soil_humidity_line6",
        "Bodenfeuchte Line6",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 6",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE,
        13,
        true).persistIfNotExist();
    new Measurement(soilHumidityLine6, 88.8, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Sensor rainIndicator = new Sensor(
        "rain_indicator",
        "Regensensor",
        "%",
        0,
        "Regen oder nicht",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE,
        13,
        true).persistIfNotExist();
    new Measurement(rainIndicator, 0.0, new Date()).persistIfInitForThisSensor();
    /* ============================================================================================================= */
    Relay relayLine1 = new Relay(
        "relay_line1",
        "Bewässerung Linie 1",
        "Tomaten",
        false,
        "Bewässert Linie 1 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        greenhouseControl,
        1).persistIfNotExist();
    ConditionTrigger line1ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine1, false, relayLine1).persistIfNotExist();
    TimeTrigger line1TimeTrigger = new TimeTrigger(
        "0-3 8,12,18 * * *",
        true,
        relayLine1
    ).persistIfNotExist();
    relayLine1.conditionTrigger = line1ConditionalTrigger;
    relayLine1.timeTrigger = line1TimeTrigger;
    relayLine1.persist();
    new RelayLog(relayLine1, "DB-INIT", new Date(), false).persistIfInitForThisRelay();

    Relay relayLine2 = new Relay(
        "relay_line2",
        "Bewässerung Linie 2",
        "Tomaten",
        false,
        "Bewässert Linie 2 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        greenhouseControl,
        2).persistIfNotExist();
    ConditionTrigger line2ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine2, false, relayLine2).persistIfNotExist();
    TimeTrigger line2TimeTrigger = new TimeTrigger(
        "3-6 8,12,18 * * *",
        true,
        relayLine2).persistIfNotExist();
    relayLine2.conditionTrigger = line2ConditionalTrigger;
    relayLine2.timeTrigger = line2TimeTrigger;
    relayLine2.persist();
    new RelayLog(relayLine2, "DB-INIT", new Date(), false).persistIfInitForThisRelay();

    Relay relayLine3 = new Relay(
        "relay_line3",
        "Bewässerung Linie 3",
        "Salat",
        false,
        "Bewässert Linie3 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        greenhouseControl,
        3
    ).persistIfNotExist();
    ConditionTrigger line3ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine3, false, relayLine3).persistIfNotExist();
    TimeTrigger line3TimeTrigger = new TimeTrigger(
        "6-9 8,12,18 * * *",
        true,
        relayLine3).persistIfNotExist();
    relayLine3.conditionTrigger = line3ConditionalTrigger;
    relayLine3.timeTrigger = line3TimeTrigger;
    relayLine3.persist();
    new RelayLog(relayLine3, "DB-INIT", new Date(), false).persistIfInitForThisRelay();

    Relay relayLine4 = new Relay(
        "relay_line4",
        "Bewässerung Linie 4",
        "Gurken",
        false,
        "Bewässert Linie4 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        greenhouseControl,
        4
    ).persistIfNotExist();
    ConditionTrigger line4ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine4, false, relayLine4).persistIfNotExist();
    TimeTrigger line4TimeTrigger = new TimeTrigger(
        "9-12 8,12,18 * * *",
        true,
        relayLine4).persistIfNotExist();
    relayLine4.conditionTrigger = line4ConditionalTrigger;
    relayLine4.timeTrigger = line4TimeTrigger;
    relayLine4.persist();
    new RelayLog(relayLine4, "DB-INIT", new Date(), false).persistIfInitForThisRelay();

    Relay relayLine5 = new Relay(
        "relay_line5",
        "Bewässerung Linie 5",
        "Radieschen",
        false,
        "Bewässert Linie5 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        greenhouseControl,
        5
    ).persistIfNotExist();
    ConditionTrigger line5ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine5, false, relayLine5).persistIfNotExist();
    TimeTrigger line5TimeTrigger = new TimeTrigger(
        "12-15 8,12,18 * * *",
        true,
        relayLine5).persistIfNotExist();
    relayLine5.conditionTrigger = line5ConditionalTrigger;
    relayLine5.timeTrigger = line5TimeTrigger;
    relayLine5.persist();
    new RelayLog(relayLine5, "DB-INIT", new Date(), false).persistIfInitForThisRelay();

    Relay relayLine6 = new Relay(
        "relay_line6",
        "Bewässerung Linie 6",
        "Melonen",
        false,
        "Bewässert Linie6 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        greenhouseControl,
        6
    ).persistIfNotExist();
    ConditionTrigger line6ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine6, false, relayLine6).persistIfNotExist();
    TimeTrigger line6TimeTrigger = new TimeTrigger(
        "15-18 8,12,18 * * *",
        true,
        relayLine6).persistIfNotExist();
    relayLine6.conditionTrigger = line6ConditionalTrigger;
    relayLine6.timeTrigger = line6TimeTrigger;
    relayLine6.persist();
    new RelayLog(relayLine6, "DB-INIT", new Date(), false).persistIfInitForThisRelay();

    Relay relayLight = new Relay(
        "relay_line7",
        "Pflanzenlicht",
        null,
        false,
        "Mit der LED-Decken-Beleuchtung kann das Wachstum und die Qualität von Gemüse gesteigert werden.",
        "mdi-white-balance-sunny",
        "#A092EB",
        greenhouseControl,
        7
    ).persistIfNotExist();
    ConditionTrigger lightConditionalTrigger = new ConditionTrigger(brightness, false, relayLight).persistIfNotExist();
    TimeTrigger lightTimeTrigger = new TimeTrigger(
        "0-2 8,12,18 * * *",
        false,
        relayLight).persistIfNotExist();
    relayLight.conditionTrigger = lightConditionalTrigger;
    relayLight.timeTrigger = lightTimeTrigger;
    relayLight.persist();
    new RelayLog(relayLight, "DB-INIT", new Date(), false).persistIfInitForThisRelay();

    Relay relayFans = new Relay(
        "relay_line8",
        "Ventilatoren",
        null,
        false,
        "Durch die richtige Verwendung von Ventilatoren wird die Luft rund um die Pflanze sanft vermischt, wodurch krankheitsfördernde Bereiche mit hoher Luftfeuchtigkeit beseitigt werden und eine starke Transpiration gefördert wird.",
        "mdi-fan",
        "#C89542",
        greenhouseControl,
        8
    ).persistIfNotExist();
    ConditionTrigger fansConditionalTrigger = new ConditionTrigger(airTempInside, false, relayFans).persistIfNotExist();
    TimeTrigger fansTimeTrigger = new TimeTrigger(
        "0-2 8,12,18 * * *",
        false,
        relayFans).persistIfNotExist();
    relayFans.conditionTrigger = fansConditionalTrigger;
    relayFans.timeTrigger = fansTimeTrigger;
    new RelayLog(relayFans, "DB-INIT", new Date(), false).persistIfInitForThisRelay();

    Relay relayWinePump = new Relay(
        "relay_wine_pump",
        "Wein Bewässerung Pumpe",
        "Wein",
        false,
        "Schaltet die Pumpe für die Weinbewässerung um aus dem Regenfass zu saugen.",
        "mdi-water",
        "#0067AF",
        wineSatellite,
        9).persistIfNotExist();
    ConditionTrigger wineConditionalTrigger = new ConditionTrigger(soilHumidityLine4, false, relayWinePump).persistIfNotExist();
    TimeTrigger winePumpTimeTrigger = new TimeTrigger(
        "0-1 8,18 * * *",
        true,
        relayWinePump
    ).persistIfNotExist();
    relayWinePump.conditionTrigger = wineConditionalTrigger;
    relayWinePump.timeTrigger = winePumpTimeTrigger;
    relayLine1.persist();
    new RelayLog(relayWinePump, "DB-INIT", new Date(), false).persistIfInitForThisRelay();
    LOGGER.info("... database filled ...");
  }
}

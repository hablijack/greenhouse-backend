package de.hablijack.greenhouse.lifecycle;

import de.hablijack.greenhouse.entity.ConditionTrigger;
import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.entity.TimeTrigger;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.logging.Logger;

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
  private final Double MIN_AIR_HUMIDITY_INSIDE = 15.0;
  private final Double MAX_AIR_HUMIDITY_INSIDE = 40.0;
  private final Double MIN_WIFI_STRENGTH = 60.0;
  private final Double MAX_WIFI_STRENGTH = 100.0;
  private final Double MIN_CO2_VALUE = 300.0;
  private final Double MAX_CO2_VALUE = 2800.0;
  private final Double MIN_LIGHT_VALUE = 1000.0;
  private final Double MAX_LIGHT_VALUE = 2800.0;
  private final Double MIN_SOIL_HUMIDITY_INSIDE = 10.0;
  private final Double MAX_SOIL_HUMIDITY_INSIDE = 30.0;

  @Transactional
  @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:MethodLength", "checkstyle:LineLength", "PMD"})
  public void initializeWithBaseData(@Observes StartupEvent event) {
    LOGGER.info("... filling database...");
    new Satellite(
        "greenhouse_cam",
        "Gewächshaus Webcam",
        "satelite.png",
        "ESP-EYE Webcam-webserver um regelmäßig Bilder innerhalb des Gewächshauses aufzunehmen.",
        "192.168.178.48",
        true).persistIfNotExist();
    Satellite greenhouseControl = new Satellite(
        "greenhouse_control",
        "Gewächshaus Steuerung",
        "satelite.png",
        "ESP32 Webserver mit Relays, Sensoren und Aktoren um das gesamte Gewächshaus fernzusteuern.",
        "192.168.178.37",
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
        MAX_AIR_INSIDE_TEMP).persistIfNotExist();
    new Measurement(airTempInside, 20.5, new Date()).persist();
    /* ============================================================================================================= */
    Sensor airTempOutside = new Sensor(
        "air_temp_outside",
        "Luft außen",
        "°C",
        1,
        "Themperatur außerhalb des Gewächshauses",
        "mdi-thermometer",
        MIN_AIR_OUTSIDE_TEMP,
        MAX_AIR_OUTSIDE_TEMP).persistIfNotExist();
    new Measurement(airTempOutside, 18.2, new Date()).persist();
    /* ============================================================================================================= */
    Sensor soilTempInside = new Sensor(
        "soil_temp_inside",
        "Boden",
        "°C",
        1,
        "Themperatur der Erde im Gewächshaus",
        "mdi-thermometer",
        MIN_SOIL_TEMP,
        MAX_SOIL_TEMP).persistIfNotExist();
    new Measurement(soilTempInside, 14.2, new Date()).persist();
    /* ============================================================================================================= */
    Sensor battery = new Sensor(
        "battery",
        "Batterie",
        "%",
        0,
        "Ladezustand der Photovoltaik-Batterie",
        "mdi-battery",
        MIN_BATTERY,
        MAX_BATTERY).persistIfNotExist();
    new Measurement(battery, 90.2, new Date()).persist();
    /* ============================================================================================================= */
    Sensor airHumidityInside = new Sensor(
        "air_humidity_inside",
        "Luft innen",
        "%",
        0,
        "Luftfeuchte im Gewächshaus",
        "mdi-water",
        MIN_AIR_HUMIDITY_INSIDE,
        MAX_AIR_HUMIDITY_INSIDE).persistIfNotExist();
    new Measurement(airHumidityInside, 99.1, new Date()).persist();
    /* ============================================================================================================= */
    Sensor wifi = new Sensor(
        "wifi",
        "WiFi",
        "%",
        0,
        "W-Lan Empfangsstärke im Gewächshaus",
        "mdi-wifi",
        MIN_WIFI_STRENGTH,
        MAX_WIFI_STRENGTH).persistIfNotExist();
    new Measurement(wifi, 70.0, new Date()).persist();
    /* ============================================================================================================= */
    Sensor brightness = new Sensor(
        "brightness",
        "Helligkeit",
        "%",
        0,
        "Intensität der Sonneneinstrahlung im Gewächshaus",
        "mdi-white-balance-sunny",
        MIN_LIGHT_VALUE,
        MAX_LIGHT_VALUE).persistIfNotExist();
    new Measurement(brightness, 1800.8, new Date()).persist();
    /* ============================================================================================================= */
    Sensor co2 = new Sensor(
        "co2",
        "CO2",
        "ppa",
        0,
        "CO2 Sättigung im Gewächshaus",
        "mdi-soundcloud",
        MIN_CO2_VALUE,
        MAX_CO2_VALUE).persistIfNotExist();
    new Measurement(co2, 7000.8, new Date()).persist();
    /* ============================================================================================================= */
    Sensor soilHumidityLine1 = new Sensor(
        "soil_humidity_line1",
        "Bodenfeuchte Line1",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 1",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Measurement(soilHumidityLine1, 70.8, new Date()).persist();
    /* ============================================================================================================= */
    Sensor soilHumidityLine2 = new Sensor(
        "soil_humidity_line2",
        "Bodenfeuchte Line2",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 2",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Measurement(soilHumidityLine2, 74.2, new Date()).persist();
    /* ============================================================================================================= */
    Sensor soilHumidityLine3 = new Sensor(
        "soil_humidity_line3",
        "Bodenfeuchte Line3",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 3",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Measurement(soilHumidityLine3, 88.8, new Date()).persist();
    /* ============================================================================================================= */
    Sensor soilHumidityLine4 = new Sensor(
        "soil_humidity_line4",
        "Bodenfeuchte Line4",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 4",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Measurement(soilHumidityLine4, 88.8, new Date()).persist();
    /* ============================================================================================================= */
    Sensor soilHumidityLine5 = new Sensor(
        "soil_humidity_line5",
        "Bodenfeuchte Line5",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 5",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Measurement(soilHumidityLine5, 88.8, new Date()).persist();
    /* ============================================================================================================= */
    Sensor soilHumidityLine6 = new Sensor(
        "soil_humidity_line6",
        "Bodenfeuchte Line6",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 6",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Measurement(soilHumidityLine6, 88.8, new Date()).persist();
    /* ============================================================================================================= */
    Relay relayLine1 = new Relay("relay_line1").persistIfNotExist();
    ConditionTrigger line1ConditionalTrigger = new ConditionTrigger(soilHumidityLine1, false, relayLine1);
    TimeTrigger line1TimeTrigger = new TimeTrigger(
        "0-8 8,10,11,12,13,17,18 * * *",
        true,
        relayLine1
    );
    relayLine1.name = "Bewässerung Linie 1";
    relayLine1.target = "Tomaten";
    relayLine1.value = false;
    relayLine1.description =
        "Bewässert Linie 1 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll";
    relayLine1.icon = "mdi-water";
    relayLine1.color = "#0067AF";
    relayLine1.conditionTrigger = line1ConditionalTrigger;
    relayLine1.timeTrigger = line1TimeTrigger;
    relayLine1.satellite = greenhouseControl;
    relayLine1.persist();
    new RelayLog(relayLine1, "DB-INIT", new Date(), false).persist();

    Relay relayLine2 = new Relay("relay_line2").persistIfNotExist();
    ConditionTrigger line2ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine2, false, relayLine2).persistIfNotExist();
    TimeTrigger line2TimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true,
        relayLine2).persistIfNotExist();
    relayLine2.name = "Bewässerung Linie 2";
    relayLine2.target = "Tomaten";
    relayLine2.value = false;
    relayLine2.description =
        "Bewässert Linie 2 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll";
    relayLine2.icon = "mdi-water";
    relayLine2.color = "#0067AF";
    relayLine2.conditionTrigger = line2ConditionalTrigger;
    relayLine2.timeTrigger = line2TimeTrigger;
    relayLine2.satellite = greenhouseControl;
    relayLine2.persist();
    new RelayLog(relayLine2, "DB-INIT", new Date(), false).persist();

    Relay relayLine3 = new Relay("relay_line3").persistIfNotExist();
    ConditionTrigger line3ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine3, false, relayLine3).persistIfNotExist();
    TimeTrigger line3TimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true,
        relayLine3).persistIfNotExist();
    relayLine3.name = "Bewässerung Linie 3";
    relayLine3.target = "Salat";
    relayLine3.value = false;
    relayLine3.description =
        "Bewässert Linie3 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll";
    relayLine3.icon = "mdi-water";
    relayLine3.color = "#0067AF";
    relayLine3.conditionTrigger = line3ConditionalTrigger;
    relayLine3.timeTrigger = line3TimeTrigger;
    relayLine3.satellite = greenhouseControl;
    relayLine3.persist();
    new RelayLog(relayLine3, "DB-INIT", new Date(), false).persist();

    Relay relayLine4 = new Relay("relay_line4").persistIfNotExist();
    ConditionTrigger line4ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine4, false, relayLine4).persistIfNotExist();
    TimeTrigger line4TimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true,
        relayLine4).persistIfNotExist();
    relayLine4.name = "Bewässerung Linie 4";
    relayLine4.target = "Gurken";
    relayLine4.value = false;
    relayLine4.description =
        "Bewässert Linie4 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll";
    relayLine4.icon = "mdi-water";
    relayLine4.color = "#0067AF";
    relayLine4.conditionTrigger = line4ConditionalTrigger;
    relayLine4.timeTrigger = line4TimeTrigger;
    relayLine4.satellite = greenhouseControl;
    relayLine4.persist();
    new RelayLog(relayLine4, "DB-INIT", new Date(), false).persist();

    Relay relayLine5 = new Relay("relay_line5").persistIfNotExist();
    ConditionTrigger line5ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine5, false, relayLine5).persistIfNotExist();
    TimeTrigger line5TimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true,
        relayLine5).persistIfNotExist();
    relayLine5.name = "Bewässerung Linie 5";
    relayLine5.target = "Radieschen";
    relayLine5.value = false;
    relayLine5.description =
        "Bewässert Linie5 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll";
    relayLine5.icon = "mdi-water";
    relayLine5.color = "#0067AF";
    relayLine5.conditionTrigger = line5ConditionalTrigger;
    relayLine5.timeTrigger = line5TimeTrigger;
    relayLine5.satellite = greenhouseControl;
    relayLine5.persist();
    new RelayLog(relayLine5, "DB-INIT", new Date(), false).persist();

    Relay relayLine6 = new Relay("relay_line6").persistIfNotExist();
    ConditionTrigger line6ConditionalTrigger =
        new ConditionTrigger(soilHumidityLine6, false, relayLine6).persistIfNotExist();
    TimeTrigger line6TimeTrigger = new TimeTrigger(
        "0-20 8,10,12,13,17,18 * * *",
        true,
        relayLine6).persistIfNotExist();
    relayLine6.name = "Bewässerung Linie 6";
    relayLine6.target = "Melonen";
    relayLine6.value = false;
    relayLine6.description =
        "Bewässert Linie6 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll";
    relayLine6.icon = "mdi-water";
    relayLine6.color = "#0067AF";
    relayLine6.conditionTrigger = line6ConditionalTrigger;
    relayLine6.timeTrigger = line6TimeTrigger;
    relayLine6.satellite = greenhouseControl;
    relayLine6.persist();
    new RelayLog(relayLine6, "DB-INIT", new Date(), false).persist();

    Relay relayLight = new Relay("relay_light").persistIfNotExist();
    ConditionTrigger lightConditionalTrigger = new ConditionTrigger(brightness, false, relayLight).persistIfNotExist();
    TimeTrigger lightTimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        false,
        relayLight).persistIfNotExist();
    relayLight.name = "Pflanzenlicht";
    relayLight.target = null;
    relayLight.value = false;
    relayLight.description =
        "Mit der LED-Decken-Beleuchtung kann das Wachstum und die Qualität von Gemüse gesteigert werden.";
    relayLight.icon = "mdi-white-balance-sunny";
    relayLight.color = "#A092EB";
    relayLight.conditionTrigger = lightConditionalTrigger;
    relayLight.timeTrigger = lightTimeTrigger;
    relayLight.satellite = greenhouseControl;
    relayLight.persist();
    new RelayLog(relayLight, "DB-INIT", new Date(), false).persist();

    Relay relayFans = new Relay("relay_fans").persistIfNotExist();
    ConditionTrigger fansConditionalTrigger = new ConditionTrigger(airTempInside, false, relayFans).persistIfNotExist();
    TimeTrigger fansTimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true,
        relayFans).persistIfNotExist();
    relayFans.name = "Ventilatoren";
    relayFans.target = null;
    relayFans.value = false;
    relayFans.description =
        "Durch die richtige Verwendung von Ventilatoren wird die Luft rund um die Pflanze sanft vermischt, wodurch krankheitsfördernde Bereiche mit hoher Luftfeuchtigkeit beseitigt werden und eine starke Transpiration gefördert wird.";
    relayFans.icon = "mdi-fan";
    relayFans.color = "#C89542";
    relayFans.conditionTrigger = fansConditionalTrigger;
    relayFans.timeTrigger = fansTimeTrigger;
    relayFans.satellite = greenhouseControl;
    relayFans.persist();
    new RelayLog(relayFans, "DB-INIT", new Date(), false).persist();

    LOGGER.info("... database filled ...");
  }
}

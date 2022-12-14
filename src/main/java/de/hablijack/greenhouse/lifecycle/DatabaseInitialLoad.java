package de.hablijack.greenhouse.lifecycle;

import de.hablijack.greenhouse.api.sensor.MeasurementSocket;
import de.hablijack.greenhouse.entity.ConditionTrigger;
import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.entity.Satelite;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.entity.TimeTrigger;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import java.util.Date;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

@SuppressWarnings("checkstyle:RegexpSingleline")
@Startup
@ApplicationScoped
public class DatabaseInitialLoad {

  private static final Logger LOGGER = Logger.getLogger(MeasurementSocket.class.getName());
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

  @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:MethodLength", "checkstyle:LineLength", "PMD"})
  @Transactional
  public void initializeWithBaseData(@Observes StartupEvent event) {
    LOGGER.info("... filling database...");
    new Satelite(
        "greenhouse_cam",
        "Gew??chshaus Webcam",
        "satelite.png",
        "ESP-EYE Webcam-webserver um regelm????ig Bilder innerhalb des Gew??chshauses aufzunehmen.",
        "192.168.178.48",
        true).persistIfNotExist();
    new Satelite(
        "greenhouse_control",
        "Gew??chshaus Steuerung",
        "satelite.png",
        "ESP32 Webserver mit Relays, Sensoren und Aktoren um das gesamte Gew??chshaus fernzusteuern.",
        "192.168.178.80",
        true).persistIfNotExist();
    /* ============================================================================================================= */
    Sensor airTempInside = new Sensor(
        "air_temp_inside",
        "Luft innen",
        "??C",
        1,
        "Themperatur innerhalb des Gew??chshauses",
        "mdi-thermometer",
        MIN_AIR_INSIDE_TEMP,
        MAX_AIR_INSIDE_TEMP).persistIfNotExist();
    new Measurement(airTempInside, 20.5, new Date()).persist();
    /* ============================================================================================================= */
    Sensor airTempOutside = new Sensor(
        "air_temp_outside",
        "Luft au??en",
        "??C",
        1,
        "Themperatur au??erhalb des Gew??chshauses",
        "mdi-thermometer",
        MIN_AIR_OUTSIDE_TEMP,
        MAX_AIR_OUTSIDE_TEMP).persistIfNotExist();
    new Measurement(airTempOutside, 18.2, new Date()).persist();
    /* ============================================================================================================= */
    Sensor soilTempInside = new Sensor(
        "soil_temp_inside",
        "Boden",
        "??C",
        1,
        "Themperatur der Erde im Gew??chshaus",
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
        "Luftfeuchte im Gew??chshaus",
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
        "W-Lan Empfangsst??rke im Gew??chshaus",
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
        "Intensit??t der Sonneneinstrahlung im Gew??chshaus",
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
        "CO2 S??ttigung im Gew??chshaus",
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
    ConditionTrigger line1ConditionalTrigger = new ConditionTrigger(soilHumidityLine1, false).persistIfNotExist();
    TimeTrigger line1TimeTrigger = new TimeTrigger(
        "0-8 8,10,11,12,13,17,18 * * *",
        true
    ).persistIfNotExist();
    Relay relayLine1 = new Relay(
        "relay_line1",
        "Bew??sserung Linie 1",
        false,
        "Bew??ssert Linie 1 gezielt in Wurzeln??he und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        line1ConditionalTrigger,
        line1TimeTrigger).persistIfNotExist();
    new RelayLog(relayLine1, "DB-INIT", new Date(), false).persist();

    ConditionTrigger line2ConditionalTrigger = new ConditionTrigger(soilHumidityLine2, false).persistIfNotExist();
    TimeTrigger line2TimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true).persistIfNotExist();
    Relay relayLine2 = new Relay(
        "relay_line2",
        "Bew??sserung Linie 2",
        false,
        "Bew??ssert Linie 2 gezielt in Wurzeln??he und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        line2ConditionalTrigger,
        line2TimeTrigger).persistIfNotExist();

    ConditionTrigger line3ConditionalTrigger = new ConditionTrigger(soilHumidityLine3, false).persistIfNotExist();
    TimeTrigger line3TimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true).persistIfNotExist();
    new RelayLog(relayLine2, "DB-INIT", new Date(), false).persist();
    Relay relayLine3 = new Relay(
        "relay_line3",
        "Bew??sserung Linie 3",
        false,
        "Bew??ssert Linie3 gezielt in Wurzeln??he und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        line3ConditionalTrigger,
        line3TimeTrigger).persistIfNotExist();

    ConditionTrigger line4ConditionalTrigger = new ConditionTrigger(soilHumidityLine4, false).persistIfNotExist();
    TimeTrigger line4TimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true).persistIfNotExist();
    new RelayLog(relayLine3, "DB-INIT", new Date(), false).persist();
    Relay relayLine4 = new Relay(
        "relay_line4",
        "Bew??sserung Linie 4",
        false,
        "Bew??ssert Linie4 gezielt in Wurzeln??he und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF",
        line4ConditionalTrigger,
        line4TimeTrigger).persistIfNotExist();
    new RelayLog(relayLine4, "DB-INIT", new Date(), false).persist();

    ConditionTrigger lightConditionalTrigger = new ConditionTrigger(brightness, false).persistIfNotExist();
    TimeTrigger lightTimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        false).persistIfNotExist();
    Relay relayLight = new Relay(
        "relay_light",
        "Pflanzenlicht",
        false,
        "Mit der LED-Decken-Beleuchtung kann das Wachstum und die Qualit??t von Gem??se gesteigert werden.",
        "mdi-white-balance-sunny",
        "#A092EB",
        lightConditionalTrigger,
        lightTimeTrigger).persistIfNotExist();
    new RelayLog(relayLight, "DB-INIT", new Date(), false).persist();

    ConditionTrigger fansConditionalTrigger = new ConditionTrigger(airTempInside, false).persistIfNotExist();
    TimeTrigger fansTimeTrigger = new TimeTrigger(
        "0-8 8,10,12,13,17,18 * * *",
        true).persistIfNotExist();
    Relay relayFans = new Relay(
        "relay_fans",
        "Ventilatoren",
        false,
        "Durch die richtige Verwendung von Ventilatoren wird die Luft rund um die Pflanze sanft vermischt, wodurch krankheitsf??rdernde Bereiche mit hoher Luftfeuchtigkeit beseitigt werden und eine starke Transpiration gef??rdert wird.",
        "mdi-fan",
        "#C89542",
        fansConditionalTrigger,
        fansTimeTrigger).persistIfNotExist();
    new RelayLog(relayFans, "DB-INIT", new Date(), false).persist();

    LOGGER.info("... database filled ...");
  }
}

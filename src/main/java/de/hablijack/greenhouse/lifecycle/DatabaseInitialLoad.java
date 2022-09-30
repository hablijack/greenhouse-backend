package de.hablijack.greenhouse.lifecycle;

import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.entity.Satelite;
import de.hablijack.greenhouse.entity.Sensor;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

@SuppressWarnings("checkstyle:RegexpSingleline")
@Startup
@ApplicationScoped
public class DatabaseInitialLoad {

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
  private final Double MIN_WIFI_STRENGTH = 15.0;
  private final Double MAX_WIFI_STRENGTH = 40.0;
  private final Double MIN_CO2_VALUE = 15.0;
  private final Double MAX_CO2_VALUE = 40.0;
  private final Double MIN_LIGHT_VALUE = 15.0;
  private final Double MAX_LIGHT_VALUE = 40.0;
  private final Double MIN_SOIL_HUMIDITY_INSIDE = 15.0;
  private final Double MAX_SOIL_HUMIDITY_INSIDE = 40.0;

  @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:MethodLength", "checkstyle:LineLength", "PMD"})
  @Transactional
  public void initializeWithBaseData(@Observes StartupEvent event) {
    new Satelite(
        "greenhouse_cam",
        "Gewächshaus Webcam",
        "satelite.png",
        "ESP-EYE Webcam-webserver um regelmäßig Bilder innerhalb des Gewächshauses aufzunehmen.",
        "192.168.178.48",
        true).persistIfNotExist();
    new Satelite(
        "greenhouse_control",
        "Gewächshaus Steuerung",
        "satelite.png",
        "ESP32 Webserver mit Relays, Sensoren und Aktoren um das gesamte Gewächshaus fernzusteuern.",
        "192.168.178.80",
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
    Relay relayLine1 = new Relay(
        "relay_line1",
        "Bewässerung Linie 1",
        false,
        "Bewässert Linie 1 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF").persistIfNotExist();
    new RelayLog(relayLine1, "Quarkus: DBINIT", new Date(), false).persist();
    Relay relayLine2 = new Relay(
        "relay_line2",
        "Bewässerung Linie 2",
        false,
        "Bewässert Linie 2 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF").persistIfNotExist();
    new RelayLog(relayLine2, "Quarkus: DBINIT", new Date(), false).persist();
    Relay relayLine3 = new Relay(
        "relay_line3",
        "Bewässerung Linie 3",
        false,
        "Bewässert Linie3 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF").persistIfNotExist();
    new RelayLog(relayLine3, "Quarkus: DBINIT", new Date(), false).persist();
    Relay relayLine4 = new Relay(
        "relay_line4",
        "Bewässerung Linie 4",
        false,
        "Bewässert Linie4 gezielt in Wurzelnähe und damit sparsam, weil das Wasser genau da ankommt, wo es hin soll",
        "mdi-water",
        "#0067AF").persistIfNotExist();
    new RelayLog(relayLine4, "Quarkus: DBINIT", new Date(), false).persist();
    Relay relayLight = new Relay(
        "relay_light",
        "Pflanzenlicht",
        false,
        "Mit der LED-Decken-Beleuchtung kann das Wachstum und die Qualität von Gemüse gesteigert werden.",
        "mdi-white-balance-sunny",
        "#A092EB").persistIfNotExist();
    new RelayLog(relayLight, "Quarkus: DBINIT", new Date(), false).persist();
    Relay relayFans = new Relay(
        "relay_fans",
        "Ventilatoren",
        false,
        "Durch die richtige Verwendung von Ventilatoren wird die Luft rund um die Pflanze sanft vermischt, wodurch krankheitsfördernde Bereiche mit hoher Luftfeuchtigkeit beseitigt werden und eine starke Transpiration gefördert wird.",
        "mdi-fan",
        "#C89542").persistIfNotExist();
    new RelayLog(relayFans, "Quarkus: DBINIT", new Date(), false).persist();
  }
}

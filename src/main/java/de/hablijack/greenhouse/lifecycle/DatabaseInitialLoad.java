package de.hablijack.greenhouse.lifecycle;

import de.hablijack.greenhouse.entity.Sensor;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

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


  @Transactional
  public void initializeWithBaseData(@Observes StartupEvent event) {
    new Sensor(
        "air_temp_inside",
        "Luft innen",
        "°C",
        1,
        "Themperatur innerhalb des Gewächshauses",
        "mdi-thermometer",
        MIN_AIR_INSIDE_TEMP,
        MAX_AIR_INSIDE_TEMP).persistIfNotExist();
    new Sensor(
        "air_temp_outside",
        "Luft außen",
        "°C",
        1,
        "Themperatur außerhalb des Gewächshauses",
        "mdi-thermometer",
        MIN_AIR_OUTSIDE_TEMP,
        MAX_AIR_OUTSIDE_TEMP).persistIfNotExist();
    new Sensor(
        "soil_temp_inside",
        "Boden",
        "°C",
        1,
        "Themperatur der Erde im Gewächshaus",
        "mdi-thermometer",
        MIN_SOIL_TEMP,
        MAX_SOIL_TEMP).persistIfNotExist();
    new Sensor(
        "battery",
        "Batterie",
        "%",
        0,
        "Ladezustand der Photovoltaik-Batterie",
        "mdi-battery",
        MIN_BATTERY,
        MAX_BATTERY).persistIfNotExist();
    new Sensor(
        "air_humidity_inside",
        "Luft innen",
        "%",
        0,
        "Luftfeuchte im Gewächshaus",
        "mdi-water",
        MIN_AIR_HUMIDITY_INSIDE,
        MAX_AIR_HUMIDITY_INSIDE).persistIfNotExist();
    new Sensor(
        "wifi",
        "WiFi",
        "%",
        0,
        "W-Lan Empfangsstärke im Gewächshaus",
        "mdi-wifi",
        MIN_WIFI_STRENGTH,
        MAX_WIFI_STRENGTH).persistIfNotExist();
    new Sensor(
        "brightness",
        "Helligkeit",
        "%",
        0,
        "Intensität der Sonneneinstrahlung im Gewächshaus",
        "mdi-white-balance-sunny",
        MIN_LIGHT_VALUE,
        MAX_LIGHT_VALUE).persistIfNotExist();
    new Sensor(
        "co2",
        "CO2",
        "ppa",
        0,
        "CO2 Sättigung im Gewächshaus",
        "mdi-soundcloud",
        MIN_CO2_VALUE,
        MAX_CO2_VALUE).persistIfNotExist();
    new Sensor(
        "soil_humidity_line1",
        "Bodenfeuchte Line1",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 1",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Sensor(
        "soil_humidity_line2",
        "Bodenfeuchte Line2",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 2",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Sensor(
        "soil_humidity_line3",
        "Bodenfeuchte Line3",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 3",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
    new Sensor(
        "soil_humidity_line4",
        "Bodenfeuchte Line4",
        "%",
        0,
        "Bodenfeuchte in Pflanzspur 4",
        "mdi-water",
        MIN_SOIL_HUMIDITY_INSIDE,
        MAX_SOIL_HUMIDITY_INSIDE).persistIfNotExist();
  }
}

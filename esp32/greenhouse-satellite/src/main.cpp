#include <Arduino.h>
#include <OneWire.h>
#include <Wire.h>
#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <map>
#include "ESPAutoWiFiConfig.h"
// SENSORS
#include "Adafruit_VEML7700.h"
#include <Adafruit_Sensor.h>
#include <Adafruit_INA260.h>
#include "DHT.h"
#include "Adafruit_SGP30.h"
#include <DallasTemperature.h>

Adafruit_INA260 batterySensor = Adafruit_INA260();
Adafruit_VEML7700 lightSensor = Adafruit_VEML7700();
DHT dht(22, DHT22);
Adafruit_SGP30 gasSensor;
// Temperature sensor probes via OneWire-BUS
OneWire oneWire(2);
DallasTemperature oneWireBus(&oneWire);

std::map<std::string, int> relays;
std::map<std::string, int>::iterator it;

WebServer server(80);

StaticJsonDocument<250> jsonDocument;
char buffer[250];

void healthcheck();
void getRelayState();
void setRelayState();
void getSensorMeasurements();

void setup_routing()
{
  server.on("/health", healthcheck);
  server.on("/relays/state", getRelayState);
  server.on("/relay/set", HTTP_POST, setRelayState);
  server.on("/sensors/measurements", getSensorMeasurements);
  server.begin();
}

void getSensorMeasurements()
{
  oneWireBus.requestTemperatures();
  gasSensor.IAQmeasure();
  jsonDocument.clear();
  JsonObject obj = jsonDocument.createNestedObject();
  obj["rain_indicator"] = analogRead(36);
  obj["soil_humidity_line5"] = analogRead(35);
  obj["soil_humidity_line4"] = analogRead(34);
  obj["wifi"] = map(WiFi.RSSI(), -100, 0, 0, 100);
  obj["soil_humidity_line6"] = analogRead(33);
  obj["soil_humidity_line1"] = analogRead(32);
  obj["soil_humidity_line3"] = analogRead(31);
  obj["co2"] = gasSensor.eCO2;
  obj["soil_humidity_line2"] = analogRead(23);
  obj["air_humidity_inside"] = dht.readHumidity();
  obj["battery_voltage"] = batterySensor.readBusVoltage();
  obj["power_consumption"] = batterySensor.readPower();
  obj["air_temp_inside"] = dht.readTemperature();
  obj["air_temp_outside"] = oneWireBus.getTempCByIndex(1);
  obj["soil_temp_inside"] = oneWireBus.getTempCByIndex(0);
  obj["brightness"] = lightSensor.readLux();
  serializeJson(obj, buffer);
  server.send(200, "application/json", buffer);
}

void getRelayState()
{
  jsonDocument.clear();
  JsonObject obj = jsonDocument.createNestedObject();
  for (it = relays.begin(); it != relays.end(); it++)
  {
    obj[it->first] = (digitalRead(it->second) == LOW);
  }
  serializeJson(obj, buffer);
  server.send(200, "application/json", buffer);
}

void healthcheck()
{
  jsonDocument.clear();
  JsonObject obj = jsonDocument.createNestedObject();
  obj["status"] = "ok";
  serializeJson(obj, buffer);
  server.send(200, "application/json", buffer);
}

void setRelayState()
{
  String body = server.arg("plain");
  deserializeJson(jsonDocument, body);
  for (it = relays.begin(); it != relays.end(); it++)
  {
    if (jsonDocument.containsKey(it->first))
    {
      bool value = jsonDocument[it->first];
      if (value)
      {
        digitalWrite(it->second, LOW);
      }
      else
      {
        digitalWrite(it->second, HIGH);
      }
    }
  }
  server.send(200, "application/json", "{}");
}

void WiFiStationDisconnected(WiFiEvent_t event, WiFiEventInfo_t info)
{
  WiFi.reconnect();
}

void setup()
{
  Serial.begin(115200);
  if (ESPAutoWiFiConfigSetup(-8, true, 0))
  {
    return; // in config mode so skip rest of setup
  }
  // INIT Temperature OneWire Sensors
  oneWireBus.begin();
  // INIT LIGHT SENSOR
  if (!lightSensor.begin())
  {
    Serial.println("Couldn't find Light Sensor");
    while (1)
      ;
  }
  lightSensor.setGain(VEML7700_GAIN_1_8);
  lightSensor.setIntegrationTime(VEML7700_IT_25MS);
  // INIT BATTERY SENSOR
  if (!batterySensor.begin())
  {
    Serial.println("Couldn't find Battery Sensor");
    while (1)
      ;
  }
  // INIT GAS SENSOR
  if (!gasSensor.begin())
  {
    Serial.println("Couldn't find GAS Sensor");
    while (1)
      ;
  }
  // INIT HUMIDITY & TEMPERATURE SENSOR
  dht.begin();

  // configure all relays with their pins
  relays.insert(std::make_pair("relay1", 23));
  relays.insert(std::make_pair("relay2", 23));
  relays.insert(std::make_pair("relay3", 23));
  relays.insert(std::make_pair("relay4", 23));
  relays.insert(std::make_pair("relay5", 23));
  relays.insert(std::make_pair("relay6", 23));
  relays.insert(std::make_pair("relay7", 23));
  relays.insert(std::make_pair("relay8", 23));
  for (it = relays.begin(); it != relays.end(); it++)
  {
    pinMode(it->second, OUTPUT);
    digitalWrite(it->second, HIGH);
  }
  WiFi.onEvent(WiFiStationDisconnected, ARDUINO_EVENT_WIFI_STA_DISCONNECTED);
  setup_routing();
}

void loop()
{
  if (ESPAutoWiFiConfigLoop())
  {         // handle WiFi config webpages
    return; // skip the rest of the loop until config finished
  }
  server.handleClient();
}
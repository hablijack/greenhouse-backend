#include <Arduino.h>
#include <esp_wifi.h>
#include <esp_bt.h>
#include <OneWire.h>
#include <Wire.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include "AsyncJson.h"
#include "ArduinoJson.h"
#include <map>
// SENSORS
#include "Adafruit_VEML7700.h"
#include <Adafruit_INA260.h>
#include "Adafruit_SGP30.h"
#include <DallasTemperature.h>

#define SSID "Olymp"
#define PWD "XXXX"

#define WET_STRING "wet"
#define DRY_STRING "dry"

Adafruit_INA260 batterySensor = Adafruit_INA260();
Adafruit_VEML7700 lightSensor = Adafruit_VEML7700();

Adafruit_SGP30 gasSensor;
// Temperature sensor probes via OneWire-BUS
OneWire oneWire(13);
DallasTemperature oneWireBus(&oneWire);

std::map<std::string, int> relays;
std::map<std::string, int>::iterator it;

AsyncWebServer server(80);

void homepageHandler(AsyncWebServerRequest *request)
{
  AsyncResponseStream *response = request->beginResponseStream("text/html");
  response->print("<!DOCTYPE html><html><head><title>Greenhouse-Satellite</title></head>");
  response->print("<body style='background-color: black; color: white; font-family: monospace;'>");
  response->print("<h1>Greenhouse-Satellite</h1>");
  response->printf("<p><b>IP:</b> http://%s</p>", WiFi.softAPIP().toString().c_str());
  response->printf("<p><b>WiFI-Strength:</b> %i db", WiFi.RSSI());
  response->print("</body></html>");
  request->send(response);
}

void healthcheckHandler(AsyncWebServerRequest *request)
{
  float tempC = oneWireBus.getTempCByIndex(0);
  AsyncResponseStream *response = request->beginResponseStream("application/json");
  DynamicJsonDocument json(1024);
  json["status"] = "ok";
  json["temp"] = tempC;
  serializeJson(json, *response);
  request->send(response);
}

void relayStateHandler(AsyncWebServerRequest *request)
{
  AsyncResponseStream *response = request->beginResponseStream("application/json");
  DynamicJsonDocument json(1024);
  for (it = relays.begin(); it != relays.end(); it++)
  {
    json[it->first] = (digitalRead(it->second) == LOW);
  }
  serializeJson(json, *response);
  request->send(response);
}

void deepSleepRequest(AsyncWebServerRequest *request, JsonVariant &jsonInput)
{
  AsyncResponseStream *response = request->beginResponseStream("application/json");
  DynamicJsonDocument jsonResponse(1024);
  if (jsonInput.containsKey("sleep_time"))
  {
    jsonResponse["success"] = true;
    uint64_t sleepDuration = jsonInput["sleep_time"];
    jsonResponse["sleep_time"] = sleepDuration;
    serializeJson(jsonResponse, *response);
    request->send(response);
    esp_sleep_enable_timer_wakeup(sleepDuration * 1000000ULL);
    delay(1000);
    Serial.flush();
    esp_deep_sleep_start();
  }
  else
  {
    jsonResponse["success"] = false;
    jsonResponse["error"] = "No parameter 'sleep_time' specified! Ignoring request...";
    serializeJson(jsonResponse, *response);
    request->send(response);
  }
}

void setRelaysHandler(AsyncWebServerRequest *request, JsonVariant &jsonInput)
{
  for (it = relays.begin(); it != relays.end(); it++)
  {
    if (jsonInput.containsKey(it->first.c_str()))
    {
      boolean value = jsonInput[it->first.c_str()];
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
  AsyncResponseStream *response = request->beginResponseStream("application/json");
  DynamicJsonDocument json(1024);
  for (it = relays.begin(); it != relays.end(); it++)
  {
    json[it->first] = (digitalRead(it->second) == LOW);
  }
  serializeJson(json, *response);
  request->send(response);
}

void getSensorMeasurementsHandler(AsyncWebServerRequest *request)
{
  AsyncResponseStream *response = request->beginResponseStream("application/json");
  DynamicJsonDocument json(1024);
  oneWireBus.requestTemperatures();
  gasSensor.IAQmeasure();

  json["rain_indicator"] = (digitalRead(27) == LOW ? WET_STRING : DRY_STRING);
  json["soil_humidity_line5"] = (digitalRead(25) == LOW ? WET_STRING : DRY_STRING);
  json["soil_humidity_line4"] = (digitalRead(33) == LOW ? WET_STRING : DRY_STRING);
  json["wifi"] = WiFi.RSSI();
  json["soil_humidity_line6"] = (digitalRead(32) == LOW ? WET_STRING : DRY_STRING);
  json["soil_humidity_line1"] = (digitalRead(35) == LOW ? WET_STRING : DRY_STRING);
  json["soil_humidity_line3"] = (digitalRead(34) == LOW ? WET_STRING : DRY_STRING);
  json["co2"] = gasSensor.eCO2;
  json["soil_humidity_line2"] = (digitalRead(26) == LOW ? WET_STRING : DRY_STRING);
  // json["air_humidity_inside"] = XXXXXXXXXXXXXXXXXXXXXXXXXXXX;
  json["battery_voltage"] = batterySensor.readBusVoltage();
  json["power_consumption"] = batterySensor.readPower();
  // json["air_temp_inside"] = XXXXXXXXXXXXXXXXXXXXXXXXXXXX;
  json["air_temp_outside"] = oneWireBus.getTempCByIndex(1);
  json["soil_temp_inside"] = oneWireBus.getTempCByIndex(0);
  json["brightness"] = lightSensor.readLux();
  serializeJson(json, *response);
  request->send(response);
}

void WiFiStationDisconnected(WiFiEvent_t event, WiFiEventInfo_t info)
{
  WiFi.begin(SSID, PWD);
}

void setup()
{
  // FIRST OF ALL: configure all relays with their pins
  relays.insert(std::make_pair("relay1", 19));
  relays.insert(std::make_pair("relay2", 18));
  relays.insert(std::make_pair("relay3", 17));
  relays.insert(std::make_pair("relay4", 5));
  relays.insert(std::make_pair("relay5", 2));
  relays.insert(std::make_pair("relay6", 4));
  relays.insert(std::make_pair("relay7", 16));
  relays.insert(std::make_pair("relay8", 15));
  for (it = relays.begin(); it != relays.end(); it++)
  {
    pinMode(it->second, OUTPUT);
    digitalWrite(it->second, HIGH);
  }

  // DEFINE MOISTURE SENSORS INPUT PINS
  pinMode(27, INPUT);
  pinMode(25, INPUT);
  pinMode(33, INPUT);
  pinMode(32, INPUT);
  pinMode(35, INPUT);
  pinMode(34, INPUT);
  pinMode(26, INPUT);

  Serial.begin(115200);
  setCpuFrequencyMhz(80);
  btStop();
  esp_bt_controller_disable();

  WiFi.mode(WIFI_STA);
  WiFi.disconnect(true);
  WiFi.onEvent(WiFiStationDisconnected, ARDUINO_EVENT_WIFI_STA_DISCONNECTED);
  WiFi.begin(SSID, PWD);
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
  }

  // ===> INIT Temperature OneWire Sensors
  oneWireBus.begin();
  // ===> INIT LIGHT SENSOR
  if (lightSensor.begin())
  {
    lightSensor.setGain(VEML7700_GAIN_1_8);
    lightSensor.setIntegrationTime(VEML7700_IT_25MS);
  }
  // ===> INIT BATTERY SENSOR
  batterySensor.begin();
  // ===> INIT GAS SENSOR
  gasSensor.begin();

  server.on("/", HTTP_GET, homepageHandler);
  server.on("/health", HTTP_GET, healthcheckHandler);
  server.on("/relays/state", HTTP_GET, relayStateHandler);
  server.addHandler(new AsyncCallbackJsonWebHandler("/relays/set", setRelaysHandler));
  server.addHandler(new AsyncCallbackJsonWebHandler("/system/deepsleep", deepSleepRequest));
  server.on("/sensors/measurements", HTTP_GET, getSensorMeasurementsHandler);
  server.begin();
}

void loop()
{
}
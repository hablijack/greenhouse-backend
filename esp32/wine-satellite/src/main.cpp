#include <Arduino.h>
#include <esp_wifi.h>
#include <esp_bt.h>
#include <Wire.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include "AsyncJson.h"
#include "ArduinoJson.h"
#include <map>

#define SSID "XXX"
#define PWD "XXX"

std::map<std::string, int> relays;
std::map<std::string, int>::iterator it;

AsyncWebServer server(80);

void homepageHandler(AsyncWebServerRequest *request)
{
  char aString[200];
  AsyncResponseStream *response = request->beginResponseStream("text/html");
  response->print("<!DOCTYPE html><html><head><title>Greenhouse-Satellite</title></head>");
  response->print("<body style='background-color: black; color: white; font-family: monospace;'>");
  response->print("<h1>Wine-Satellite</h1>");
  response->printf("<p><b>IP:</b> http://%s</p>", WiFi.localIP().toString().c_str());
  response->printf("<p><b>WiFI-Strength:</b> %i db", WiFi.RSSI());
  response->print("</body></html>");
  request->send(response);
}

void healthcheckHandler(AsyncWebServerRequest *request)
{
  AsyncResponseStream *response = request->beginResponseStream("application/json");
  DynamicJsonDocument json(1024);
  json["status"] = "ok";
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

void WiFiStationDisconnected(WiFiEvent_t event, WiFiEventInfo_t info)
{
  WiFi.begin(SSID, PWD);
}

void setup()
{
  // FIRST OF ALL: configure all relays with their pins
  relays.insert(std::make_pair("relay_wine_pump", 19));
  for (it = relays.begin(); it != relays.end(); it++)
  {
    pinMode(it->second, OUTPUT);
    digitalWrite(it->second, HIGH);
  }

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

  server.on("/", HTTP_GET, homepageHandler);
  server.on("/health", HTTP_GET, healthcheckHandler);
  server.on("/relays/state", HTTP_GET, relayStateHandler);
  server.addHandler(new AsyncCallbackJsonWebHandler("/relays/set", setRelaysHandler));
  server.addHandler(new AsyncCallbackJsonWebHandler("/system/deepsleep", deepSleepRequest));
  server.begin();
}

void loop()
{
}
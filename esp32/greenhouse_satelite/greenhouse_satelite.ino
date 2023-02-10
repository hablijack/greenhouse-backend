#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>

const char *SSID = "SSID";
const char *PWD = "PWD";

const int relay8 = 27;
const int relay7 = 26;
const int relay6 = 25;
const int relay5 = 24;
const int relay4 = 18;
const int relay3 = 19;
const int relay2 = 22;
const int relay1 = 23;

WebServer server(80);

StaticJsonDocument<250> jsonDocument;
char buffer[250];

void setup_routing() {  
  server.on("/health", healthcheck);        
  server.on("/relay/state", getRelayState);     
  server.on("/relay/set", HTTP_POST, setRelayState);    
  server.begin();    
}
 
void getRelayState() {
  jsonDocument.clear();
  JsonObject obj = jsonDocument.createNestedObject();
  obj["relay1"] = (digitalRead(relay1) == LOW);
  obj["relay2"] = (digitalRead(relay2) == LOW);
  obj["relay3"] = (digitalRead(relay3) == LOW);
  obj["relay4"] = (digitalRead(relay4) == LOW);
  obj["relay5"] = (digitalRead(relay5) == LOW);
  obj["relay6"] = (digitalRead(relay6) == LOW);
  obj["relay7"] = (digitalRead(relay7) == LOW); 
  obj["relay8"] = (digitalRead(relay8) == LOW);
  serializeJson(obj, buffer);
  server.send(200, "application/json", buffer);
}

void healthcheck() {
  jsonDocument.clear();
  JsonObject obj = jsonDocument.createNestedObject();
  obj["status"] = "ok";
  serializeJson(obj, buffer);
  server.send(200, "application/json", buffer);
}

void setRelayState() {
  String body = server.arg("plain");
  deserializeJson(jsonDocument, body);
  if(jsonDocument.containsKey("relay1")){
    bool value = jsonDocument["relay1"];
    if(value){
      digitalWrite(relay1, LOW);
    } else {
      digitalWrite(relay1, HIGH);
    }
  }
  if(jsonDocument.containsKey("relay2")){
    bool value = jsonDocument["relay2"];
    if(value){
      digitalWrite(relay2, LOW);
    } else {
      digitalWrite(relay2, HIGH);
    }
  }
  if(jsonDocument.containsKey("relay3")){
    bool value = jsonDocument["relay3"];
    if(value){
      digitalWrite(relay3, LOW);
    } else {
      digitalWrite(relay3, HIGH);
    }
  }
  if(jsonDocument.containsKey("relay4")){
    bool value = jsonDocument["relay4"];
    if(value){
      digitalWrite(relay4, LOW);
    } else {
      digitalWrite(relay4, HIGH);
    }
  }
  server.send(200, "application/json", "{}");
}

void WiFiStationDisconnected(WiFiEvent_t event, WiFiEventInfo_t info){
  WiFi.begin(SSID, PWD);
}

void setup() {     
  pinMode(relay1, OUTPUT);
  pinMode(relay2, OUTPUT);
  pinMode(relay3, OUTPUT);
  pinMode(relay4, OUTPUT);

  digitalWrite(relay1, HIGH);
  digitalWrite(relay2, HIGH);
  digitalWrite(relay3, HIGH);
  digitalWrite(relay4, HIGH);

  WiFi.disconnect(true);
  WiFi.onEvent(WiFiStationDisconnected, ARDUINO_EVENT_WIFI_STA_DISCONNECTED);
  WiFi.begin(SSID, PWD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }
  
  setup_routing();     
}    
       
void loop() {    
  server.handleClient();     
}

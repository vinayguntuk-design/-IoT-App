#include <ArduinoJson.h>
#include <ESP8266WebServer.h>
#include <ESP8266WiFi.h>
#include <PubSubClient.h>

const char *WIFI_SSID = "YOUR_WIFI_NAME";
const char *WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
const char *MQTT_HOST = "192.168.1.10";
const uint16_t MQTT_PORT = 1883;
const char *DEVICE_ID = "kitchen-fan-01";

WiFiClient wifiClient;
PubSubClient mqtt(wifiClient);
ESP8266WebServer server(80);

bool powerState = false;
int speedValue = 0;
float sensorValue = 24.0f;

String controlTopic() {
  return "device/" + String(DEVICE_ID) + "/control";
}

String statusTopic() {
  return "device/" + String(DEVICE_ID) + "/status";
}

void publishStatus() {
  StaticJsonDocument<192> doc;
  doc["power"] = powerState;
  doc["speed"] = speedValue;
  doc["temperature"] = sensorValue;
  doc["online"] = true;

  char payload[192];
  serializeJson(doc, payload);
  mqtt.publish(statusTopic().c_str(), payload, true);
}

void applyControl(JsonDocument &doc) {
  if (doc["power"].is<bool>()) {
    powerState = doc["power"];
    digitalWrite(LED_BUILTIN, powerState ? LOW : HIGH);
  }
  if (doc["speed"].is<int>()) {
    speedValue = constrain((int)doc["speed"], 0, 5);
  }
  if (doc["intensity"].is<int>()) {
    speedValue = map(constrain((int)doc["intensity"], 0, 100), 0, 100, 0, 5);
  }
  publishStatus();
}

void mqttCallback(char *topic, byte *payload, unsigned int length) {
  StaticJsonDocument<256> doc;
  DeserializationError err = deserializeJson(doc, payload, length);
  if (!err) {
    applyControl(doc);
  }
}

void connectMqtt() {
  while (!mqtt.connected()) {
    String clientId = "premium-iot-" + String(DEVICE_ID);
    if (mqtt.connect(clientId.c_str(), nullptr, nullptr, statusTopic().c_str(), 1, true, "{\"online\":false}")) {
      mqtt.subscribe(controlTopic().c_str());
      publishStatus();
    } else {
      delay(2000);
    }
  }
}

void handleHealth() {
  server.send(200, "application/json", "{\"ok\":true,\"chip\":\"esp8266\"}");
}

void handleControl() {
  StaticJsonDocument<256> doc;
  DeserializationError err = deserializeJson(doc, server.arg("plain"));
  if (err) {
    server.send(400, "application/json", "{\"accepted\":false}");
    return;
  }
  applyControl(doc);
  server.send(200, "application/json", "{\"accepted\":true}");
}

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
  Serial.begin(115200);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }

  mqtt.setServer(MQTT_HOST, MQTT_PORT);
  mqtt.setCallback(mqttCallback);

  server.on("/health", HTTP_GET, handleHealth);
  server.on("/control", HTTP_POST, handleControl);
  server.begin();
}

void loop() {
  if (!mqtt.connected()) {
    connectMqtt();
  }
  mqtt.loop();
  server.handleClient();

  static unsigned long lastReport = 0;
  if (millis() - lastReport > 10000) {
    lastReport = millis();
    sensorValue += 0.1f;
    if (sensorValue > 31.0f) {
      sensorValue = 24.0f;
    }
    publishStatus();
  }
}

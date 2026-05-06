# Premium IoT Usage Manual

This project is a cost-free IoT platform for Android + ESP32 + ESP8266/NodeMCU.

## Login Credentials

```text
Username: admin
Password: admin123
```

These are local demo credentials stored in the app code. They are useful for testing APK access.

## Features Added

- Android Kotlin app using MVVM.
- Premium dark smart-home UI with device cards, room filters, sliders, toggles, buttons, and value displays.
- Login screen to protect access to app controls.
- Dynamic JSON device templates, so new IoT projects can be added without creating a new screen.
- Local control over same WiFi using HTTP.
- Remote/control sync using MQTT through your own free broker.
- ESP32 Arduino firmware.
- ESP8266/NodeMCU Arduino firmware.
- Optional free Mosquitto MQTT broker using Docker.

## Communication Modes

### Local WiFi Mode

```text
Android App -> HTTP -> ESP32/ESP8266
```

Health check:

```text
GET http://DEVICE_IP/health
```

Control:

```text
POST http://DEVICE_IP/control
Content-Type: application/json
```

Example:

```json
{
  "power": true,
  "speed": 3
}
```

### MQTT Mode

```text
Android App -> MQTT Broker -> ESP32/ESP8266
```

Control topic:

```text
device/{device_id}/control
```

Status topic:

```text
device/{device_id}/status
```

Example status:

```json
{
  "power": true,
  "speed": 3,
  "temperature": 25.1,
  "online": true
}
```

## Connect NodeMCU ESP8266

1. Open Arduino IDE.
2. Install libraries: `PubSubClient`, `ArduinoJson`.
3. Open `firmware/arduino_esp8266_premium_iot/arduino_esp8266_premium_iot.ino`.
4. Set WiFi:

```cpp
const char *WIFI_SSID = "YOUR_WIFI_NAME";
const char *WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
```

5. Keep or set device ID:

```cpp
const char *DEVICE_ID = "kitchen-fan-01";
```

6. Upload to NodeMCU.
7. Open Serial Monitor and find the NodeMCU IP from your router/device list.
8. In `TemplateCatalog.kt`, set Kitchen Fan `local_host` to that IP.
9. Build and install APK.
10. Login with `admin / admin123` and control Kitchen Fan.

## Connect ESP32

1. Open `firmware/arduino_esp32_premium_iot/arduino_esp32_premium_iot.ino`.
2. Install libraries: `PubSubClient`, `ArduinoJson`.
3. Set WiFi credentials.
4. Keep or set:

```cpp
const char *DEVICE_ID = "living-light-01";
```

5. Upload to ESP32.
6. In `TemplateCatalog.kt`, set Living Light `local_host` to ESP32 IP.
7. Build/install APK and control Living Light.

## Optional MQTT Broker

With Docker:

```powershell
cd broker
docker compose up -d
```

Set Android in `FreeIotConfig.kt`:

```kotlin
const val mqttBrokerUrl = "tcp://YOUR_PC_IP:1883"
```

Set firmware:

```cpp
const char *MQTT_HOST = "YOUR_PC_IP";
```

## Build APK

Open project in Android Studio, wait for Gradle sync, then use Build > Build Bundle(s) / APK(s) > Build APK(s).

Or terminal:

```powershell
.\gradlew.bat assembleDebug
```

APK path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

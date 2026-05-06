# NodeMCU Test Guide

Use the ESP8266 firmware with a NodeMCU board.

## Arduino IDE Libraries

Install:

- `PubSubClient`
- `ArduinoJson`

## Firmware

Open:

```text
firmware/arduino_esp8266_premium_iot/arduino_esp8266_premium_iot.ino
```

Set:

```cpp
const char *WIFI_SSID = "YOUR_WIFI_NAME";
const char *WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
const char *MQTT_HOST = "192.168.1.10";
const char *DEVICE_ID = "kitchen-fan-01";
```

For local-only testing, the Android phone and NodeMCU must be on the same WiFi.

## Android Template Match

The Android template already contains:

```json
{
  "device_id": "kitchen-fan-01",
  "device_name": "Kitchen Fan",
  "room": "Kitchen",
  "local_host": "192.168.1.92"
}
```

Change `local_host` in `TemplateCatalog.kt` to the IP address of your NodeMCU.

## APK Build

After installing Android Studio/JDK:

```powershell
.\gradlew.bat assembleDebug
```

APK path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

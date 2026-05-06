# Premium IoT

A complete cost-free IoT platform starter for Android + ESP32 + ESP8266/NodeMCU.

## What Is Included

- Kotlin Android app with MVVM.
- Material 3 premium dark smart-home UI.
- Login screen with local demo credentials.
- Dynamic JSON device templates.
- Local WiFi control using HTTP.
- MQTT control/status sync through your own free broker.
- ESP32 Arduino firmware.
- ESP8266/NodeMCU Arduino firmware.
- Optional Mosquitto MQTT broker config.
- Working browser preview.
- Usage manual and NodeMCU testing guide.

## Login

```text
Username: admin
Password: admin123
```

## Main Communication

Local HTTP:

```text
GET  http://DEVICE_IP/health
POST http://DEVICE_IP/control
```

MQTT:

```text
device/{device_id}/control
device/{device_id}/status
```

Example payload:

```json
{
  "power": true,
  "speed": 3
}
```

## Build APK

Open this repository in Android Studio, let Gradle sync, then run:

```powershell
.\gradlew.bat assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Full setup is in `docs/USAGE_MANUAL.md`.

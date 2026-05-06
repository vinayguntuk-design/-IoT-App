package com.premiumiot.app.data

import kotlinx.serialization.json.Json

class TemplateCatalog {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun loadTemplates(): List<DeviceTemplate> = defaultTemplates.map {
        json.decodeFromString(DeviceTemplate.serializer(), it)
    }

    private val defaultTemplates = listOf(
        """
        {
          "device_id": "living-light-01",
          "device_name": "Living Light",
          "device_type": "smart_light",
          "room": "Living Room",
          "local_host": "192.168.1.91",
          "controls": [
            {"type": "toggle", "topic": "/power", "label": "Power"},
            {"type": "slider", "topic": "/intensity", "label": "Brightness", "min": 1, "max": 100, "step": 1, "unit": "%"},
            {"type": "scheduler", "topic": "/schedule", "label": "Schedule"}
          ]
        }
        """.trimIndent(),
        """
        {
          "device_id": "kitchen-fan-01",
          "device_name": "Kitchen Fan",
          "device_type": "fan",
          "room": "Kitchen",
          "local_host": "192.168.1.92",
          "controls": [
            {"type": "toggle", "topic": "/power", "label": "Power"},
            {"type": "slider", "topic": "/speed", "label": "Speed", "min": 0, "max": 5, "step": 1},
            {"type": "value", "topic": "/temperature", "label": "Temperature", "unit": "C"}
          ]
        }
        """.trimIndent(),
        """
        {
          "device_id": "bedroom-scene-01",
          "device_name": "Bedside Scene",
          "device_type": "scene_button",
          "room": "Bedroom",
          "controls": [
            {"type": "button", "topic": "/trigger", "label": "Run Scene"},
            {"type": "value", "topic": "/last_run", "label": "Last Run"}
          ]
        }
        """.trimIndent()
    )
}

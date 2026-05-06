package com.premiumiot.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DeviceTemplate(
    @SerialName("device_id") val deviceId: String,
    @SerialName("device_name") val deviceName: String,
    @SerialName("device_type") val deviceType: String,
    val room: String = "Home",
    @SerialName("local_host") val localHost: String? = null,
    val controls: List<DeviceControl>
)

@Serializable
data class DeviceControl(
    val type: String,
    val topic: String,
    val label: String = topic.trim('/').replaceFirstChar { it.titlecase() },
    val min: Float = 0f,
    val max: Float = 100f,
    val step: Float = 1f,
    val unit: String = ""
)

data class DeviceState(
    val deviceId: String,
    val online: Boolean = false,
    val transport: TransportMode = TransportMode.MQTT,
    val values: Map<String, JsonElement> = emptyMap()
)

enum class TransportMode {
    LOCAL,
    MQTT
}

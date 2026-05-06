package com.premiumiot.app.domain

import com.premiumiot.app.data.DeviceTemplate
import com.premiumiot.app.data.TransportMode
import com.premiumiot.app.network.LocalDeviceClient
import com.premiumiot.app.network.MqttManager
import kotlinx.serialization.json.JsonObject

class ControlRouter(
    private val localDeviceClient: LocalDeviceClient,
    private val mqttManager: MqttManager
) {
    suspend fun resolveTransport(device: DeviceTemplate): TransportMode {
        val localHost = device.localHost ?: return TransportMode.MQTT
        return if (localDeviceClient.isReachable(localHost)) TransportMode.LOCAL else TransportMode.MQTT
    }

    suspend fun send(device: DeviceTemplate, payload: JsonObject): TransportMode {
        val encoded = payload.toString()
        val localHost = device.localHost
        if (localHost != null && localDeviceClient.publish(localHost, encoded)) {
            return TransportMode.LOCAL
        }

        val topic = "device/${device.deviceId}/control"
        mqttManager.publish(topic, encoded)
        return TransportMode.MQTT
    }
}

package com.premiumiot.app.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

data class MqttEvent(val topic: String, val payload: String)

class MqttManager(
    private val brokerUrl: String,
    private val username: String? = null,
    private val password: String? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val clientId = "premium-iot-android-${UUID.randomUUID()}"
    private val client = MqttClient(brokerUrl, clientId, MemoryPersistence())
    private val _events = MutableSharedFlow<MqttEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<MqttEvent> = _events

    fun connect(statusTopics: List<String>) {
        if (client.isConnected) return

        client.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                statusTopics.forEach { topic -> client.subscribe(topic, 1) }
            }

            override fun connectionLost(cause: Throwable?) = Unit

            override fun messageArrived(topic: String, message: MqttMessage) {
                scope.launch { _events.emit(MqttEvent(topic, message.toString())) }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
        })

        scope.launch {
            runCatching {
                val options = MqttConnectOptions().apply {
                    isAutomaticReconnect = true
                    isCleanSession = false
                    connectionTimeout = 10
                    keepAliveInterval = 30
                    username?.let { userName = it }
                    password?.let { this.password = it.toCharArray() }
                }
                client.connect(options)
            }
        }
    }

    fun publish(topic: String, payload: String, retained: Boolean = false) {
        scope.launch {
            runCatching {
                val message = MqttMessage(payload.toByteArray()).apply {
                    qos = 1
                    isRetained = retained
                }
                if (client.isConnected) {
                    client.publish(topic, message)
                }
            }
        }
    }

    fun disconnect() {
        if (client.isConnected) client.disconnect()
    }
}

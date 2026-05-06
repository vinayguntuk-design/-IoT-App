package com.premiumiot.app.domain

import com.premiumiot.app.data.DeviceState
import com.premiumiot.app.data.DeviceTemplate
import com.premiumiot.app.data.TemplateCatalog
import com.premiumiot.app.network.MqttEvent
import com.premiumiot.app.network.MqttManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class DeviceRepository(
    private val catalog: TemplateCatalog,
    private val controlRouter: ControlRouter,
    private val mqttManager: MqttManager,
    private val scope: CoroutineScope
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val _templates = MutableStateFlow<List<DeviceTemplate>>(emptyList())
    private val _states = MutableStateFlow<Map<String, DeviceState>>(emptyMap())

    val templates: StateFlow<List<DeviceTemplate>> = _templates
    val states: StateFlow<Map<String, DeviceState>> = _states
    private var started = false

    fun start() {
        if (started) return
        started = true
        val devices = catalog.loadTemplates()
        _templates.value = devices
        _states.value = devices.associate { it.deviceId to DeviceState(deviceId = it.deviceId) }
        mqttManager.connect(devices.map { "device/${it.deviceId}/status" })
        scope.launch { mqttManager.events.collect(::handleMqttEvent) }
        scope.launch { refreshTransports() }
    }

    suspend fun control(device: DeviceTemplate, field: String, value: JsonElement) {
        val payload = JsonObject(mapOf(field to value))
        val transport = controlRouter.send(device, payload)
        val current = _states.value[device.deviceId] ?: DeviceState(device.deviceId)
        _states.value = _states.value + (device.deviceId to current.copy(
            online = true,
            transport = transport,
            values = current.values + (field to value)
        ))
    }

    private suspend fun refreshTransports() {
        _templates.value.forEach { device ->
            val transport = controlRouter.resolveTransport(device)
            val current = _states.value[device.deviceId] ?: DeviceState(device.deviceId)
            _states.value = _states.value + (device.deviceId to current.copy(transport = transport))
        }
    }

    private fun handleMqttEvent(event: MqttEvent) {
        val deviceId = event.topic.removePrefix("device/").removeSuffix("/status")
        val payload = runCatching { json.parseToJsonElement(event.payload) as JsonObject }.getOrNull() ?: return
        val current = _states.value[deviceId] ?: DeviceState(deviceId)
        _states.value = _states.value + (deviceId to current.copy(
            online = true,
            values = current.values + payload
        ))
    }
}

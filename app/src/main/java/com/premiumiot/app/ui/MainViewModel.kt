package com.premiumiot.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.premiumiot.app.data.DeviceTemplate
import com.premiumiot.app.domain.DeviceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

data class DashboardUiState(
    val selectedRoom: String = "All",
    val rooms: List<String> = listOf("All"),
    val devices: List<DeviceCardModel> = emptyList()
)

data class DeviceCardModel(
    val template: DeviceTemplate,
    val state: com.premiumiot.app.data.DeviceState
)

class MainViewModel(
    private val repository: DeviceRepository
) : ViewModel() {
    private val selectedRoom = kotlinx.coroutines.flow.MutableStateFlow("All")

    val uiState = combine(repository.templates, repository.states, selectedRoom) { templates, states, room ->
        val rooms = listOf("All") + templates.map { it.room }.distinct()
        val cards = templates
            .filter { room == "All" || it.room == room }
            .map { template ->
                DeviceCardModel(
                    template = template,
                    state = states[template.deviceId] ?: com.premiumiot.app.data.DeviceState(template.deviceId)
                )
            }
        DashboardUiState(selectedRoom = room, rooms = rooms, devices = cards)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun start() {
        repository.start()
    }

    fun selectRoom(room: String) {
        selectedRoom.value = room
    }

    fun sendControl(device: DeviceTemplate, field: String, value: JsonElement) {
        viewModelScope.launch { repository.control(device, field, value) }
    }
}

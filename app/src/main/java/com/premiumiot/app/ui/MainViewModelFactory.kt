package com.premiumiot.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.premiumiot.app.data.TemplateCatalog
import com.premiumiot.app.domain.ControlRouter
import com.premiumiot.app.domain.DeviceRepository
import com.premiumiot.app.network.FreeIotConfig
import com.premiumiot.app.network.LocalDeviceClient
import com.premiumiot.app.network.MqttManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val mqttManager = MqttManager(
            brokerUrl = FreeIotConfig.mqttBrokerUrl,
            scope = appScope
        )
        val router = ControlRouter(
            localDeviceClient = LocalDeviceClient(),
            mqttManager = mqttManager
        )
        val repository = DeviceRepository(
            catalog = TemplateCatalog(),
            controlRouter = router,
            mqttManager = mqttManager,
            scope = appScope
        )
        return MainViewModel(repository) as T
    }
}

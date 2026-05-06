package com.premiumiot.app.network

object FreeIotConfig {
    // Cost-free options:
    // 1. Local only: run Mosquitto on a laptop/Raspberry Pi, e.g. tcp://192.168.1.10:1883.
    // 2. Remote free: expose your own broker through a home VPN, port forward, or free VPS.
    // Public anonymous brokers are fine for testing but not safe for real devices.
    const val mqttBrokerUrl = "tcp://192.168.1.10:1883"
}

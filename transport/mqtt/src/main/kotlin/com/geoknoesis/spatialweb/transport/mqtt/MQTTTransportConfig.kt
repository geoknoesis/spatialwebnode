package com.geoknoesis.spatialweb.transport.mqtt

import kotlinx.serialization.Serializable

/**
 * Configuration for MQTT Transport Binding.
 */
@Serializable
data class MQTTTransportConfig(
    val name: String = "mqtt",
    val brokerUrl: String = "tcp://localhost:1883",
    val clientId: String? = null,
    val username: String? = null,
    val password: String? = null,
    val cleanSession: Boolean = true,
    val connectionTimeout: Int = 30,
    val keepAliveInterval: Int = 60,
    val maxInflight: Int = 1000,
    val automaticReconnect: Boolean = true,
    val maxReconnectDelay: Int = 10000,
    val topicPrefix: String = "spatialweb",
    val qos: Int = 1,
    val retainMessages: Boolean = false,
    val enableTLS: Boolean = false,
    val tlsVersion: String = "TLSv1.2",
    val enableLogging: Boolean = true,
    val enableMetrics: Boolean = false,
    val customProperties: Map<String, String> = emptyMap()
) {
    companion object {
        fun local() = MQTTTransportConfig(
            name = "mqtt-local",
            brokerUrl = "tcp://localhost:1883",
            topicPrefix = "spatialweb/local",
            enableLogging = true
        )
        
        fun testnet() = MQTTTransportConfig(
            name = "mqtt-testnet",
            brokerUrl = "tcp://test.mosquitto.org:1883",
            topicPrefix = "spatialweb/testnet",
            automaticReconnect = true,
            enableLogging = true
        )
        
        fun production() = MQTTTransportConfig(
            name = "mqtt-production",
            brokerUrl = "tcp://mqtt.example.com:1883",
            username = "spatialweb",
            password = "secure_password",
            topicPrefix = "spatialweb/prod",
            qos = 2,
            enableTLS = true,
            automaticReconnect = true,
            enableLogging = true,
            enableMetrics = true
        )
    }
} 
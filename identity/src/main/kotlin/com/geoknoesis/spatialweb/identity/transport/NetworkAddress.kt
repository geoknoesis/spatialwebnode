package com.geoknoesis.spatialweb.identity.transport

sealed interface NetworkAddress

data class IpAddress(val host: String, val port: Int) : NetworkAddress
data class Libp2pPeer(val peerId: String) : NetworkAddress
data class WebSocketSessionId(val sessionId: String) : NetworkAddress
data class MqttClientAddress(val clientId: String, val brokerUrl: String) : NetworkAddress 
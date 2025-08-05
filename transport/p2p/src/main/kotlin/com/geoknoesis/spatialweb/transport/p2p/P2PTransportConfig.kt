package com.geoknoesis.spatialweb.transport.p2p

import kotlinx.serialization.Serializable

/**
 * Configuration for P2P Transport Binding using libp2p.
 */
@Serializable
data class P2PTransportConfig(
    val name: String = "p2p",
    val listenAddresses: List<String> = listOf("/ip4/0.0.0.0/tcp/4001"),
    val bootstrapPeers: List<String> = emptyList(),
    val enableDiscovery: Boolean = true,
    val enablePubSub: Boolean = true,
    val enablePing: Boolean = true,
    val maxConnections: Int = 100,
    val connectionTimeoutMs: Long = 30000,
    val messageTimeoutMs: Long = 10000,
    val enableRelay: Boolean = false,
    val enableNAT: Boolean = true,
    val enableMetrics: Boolean = false,
    val privateKeyPath: String? = null,
    val enableCompression: Boolean = true,
    val enableLogging: Boolean = true,
    val customProtocols: List<String> = emptyList()
) {
    companion object {
        fun local() = P2PTransportConfig(
            name = "p2p-local",
            listenAddresses = listOf("/ip4/127.0.0.1/tcp/4001"),
            enableDiscovery = false,
            enablePubSub = true,
            enablePing = true,
            maxConnections = 10
        )
        
        fun testnet() = P2PTransportConfig(
            name = "p2p-testnet",
            listenAddresses = listOf("/ip4/0.0.0.0/tcp/4001", "/ip6/::/tcp/4001"),
            bootstrapPeers = listOf(
                "/dnsaddr/bootstrap.libp2p.io/p2p/QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN",
                "/dnsaddr/bootstrap.libp2p.io/p2p/QmQCU2EcMqAqQPR2i9bChDtGNJchTbq5TbXJJ16u19uLTa"
            ),
            enableDiscovery = true,
            enablePubSub = true,
            enablePing = true,
            maxConnections = 200
        )
    }
} 
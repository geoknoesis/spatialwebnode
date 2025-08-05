package com.geoknoesis.spatialweb.core.transport

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TransportProtocolsTest {
    
    @Test
    fun `test HTTP protocols`() {
        assertEquals("http", TransportProtocols.HTTP.HTTP)
        assertEquals("https", TransportProtocols.HTTP.HTTPS)
        assertEquals("ws", TransportProtocols.HTTP.WEBSOCKET)
        assertEquals("wss", TransportProtocols.HTTP.WEBSOCKET_SECURE)
        
        val expectedHttpProtocols = listOf("http", "https", "ws", "wss")
        assertEquals(expectedHttpProtocols, TransportProtocols.HTTP.ALL)
    }
    
    @Test
    fun `test MQTT protocols`() {
        assertEquals("mqtt", TransportProtocols.MQTT.MQTT)
        assertEquals("mqtts", TransportProtocols.MQTT.MQTTS)
        assertEquals("ws", TransportProtocols.MQTT.MQTT_WS)
        assertEquals("wss", TransportProtocols.MQTT.MQTT_WSS)
        
        val expectedMqttProtocols = listOf("mqtt", "mqtts", "ws", "wss")
        assertEquals(expectedMqttProtocols, TransportProtocols.MQTT.ALL)
    }
    
    @Test
    fun `test P2P protocols`() {
        assertEquals("p2p", TransportProtocols.P2P.P2P)
        assertEquals("libp2p", TransportProtocols.P2P.LIBP2P)
        assertEquals("ipfs", TransportProtocols.P2P.IPFS)
        
        val expectedP2PProtocols = listOf("p2p", "libp2p", "ipfs")
        assertEquals(expectedP2PProtocols, TransportProtocols.P2P.ALL)
    }
    
    @Test
    fun `test GraphQL protocols`() {
        assertEquals("graphql+http", TransportProtocols.GraphQL.GRAPHQL_HTTP)
        assertEquals("graphql+https", TransportProtocols.GraphQL.GRAPHQL_HTTPS)
        assertEquals("graphql+ws", TransportProtocols.GraphQL.GRAPHQL_WS)
        assertEquals("graphql+wss", TransportProtocols.GraphQL.GRAPHQL_WSS)
        
        val expectedGraphQLProtocols = listOf("graphql+http", "graphql+https", "graphql+ws", "graphql+wss")
        assertEquals(expectedGraphQLProtocols, TransportProtocols.GraphQL.ALL)
    }
    
    @Test
    fun `test protocol validation`() {
        assertTrue(TransportProtocols.Utils.isValidProtocol("http"))
        assertTrue(TransportProtocols.Utils.isValidProtocol("https"))
        assertTrue(TransportProtocols.Utils.isValidProtocol("mqtt"))
        assertTrue(TransportProtocols.Utils.isValidProtocol("p2p"))
        assertTrue(TransportProtocols.Utils.isValidProtocol("graphql+http"))
        
        assertFalse(TransportProtocols.Utils.isValidProtocol("invalid"))
        assertFalse(TransportProtocols.Utils.isValidProtocol("ftp"))
        assertFalse(TransportProtocols.Utils.isValidProtocol(""))
    }
    
    @Test
    fun `test protocol type checking`() {
        // HTTP protocols
        assertTrue(TransportProtocols.Utils.isHttpProtocol("http"))
        assertTrue(TransportProtocols.Utils.isHttpProtocol("https"))
        assertTrue(TransportProtocols.Utils.isHttpProtocol("ws"))
        assertTrue(TransportProtocols.Utils.isHttpProtocol("wss"))
        assertFalse(TransportProtocols.Utils.isHttpProtocol("mqtt"))
        
        // MQTT protocols
        assertTrue(TransportProtocols.Utils.isMqttProtocol("mqtt"))
        assertTrue(TransportProtocols.Utils.isMqttProtocol("mqtts"))
        assertTrue(TransportProtocols.Utils.isMqttProtocol("ws"))
        assertTrue(TransportProtocols.Utils.isMqttProtocol("wss"))
        assertFalse(TransportProtocols.Utils.isMqttProtocol("http"))
        
        // P2P protocols
        assertTrue(TransportProtocols.Utils.isP2PProtocol("p2p"))
        assertTrue(TransportProtocols.Utils.isP2PProtocol("libp2p"))
        assertTrue(TransportProtocols.Utils.isP2PProtocol("ipfs"))
        assertFalse(TransportProtocols.Utils.isP2PProtocol("http"))
        
        // GraphQL protocols
        assertTrue(TransportProtocols.Utils.isGraphQLProtocol("graphql+http"))
        assertTrue(TransportProtocols.Utils.isGraphQLProtocol("graphql+https"))
        assertTrue(TransportProtocols.Utils.isGraphQLProtocol("graphql+ws"))
        assertTrue(TransportProtocols.Utils.isGraphQLProtocol("graphql+wss"))
        assertFalse(TransportProtocols.Utils.isGraphQLProtocol("http"))
    }
    
    @Test
    fun `test secure protocol detection`() {
        assertTrue(TransportProtocols.Utils.isSecureProtocol("https"))
        assertTrue(TransportProtocols.Utils.isSecureProtocol("wss"))
        assertTrue(TransportProtocols.Utils.isSecureProtocol("mqtts"))
        assertTrue(TransportProtocols.Utils.isSecureProtocol("graphql+https"))
        assertTrue(TransportProtocols.Utils.isSecureProtocol("graphql+wss"))
        
        assertFalse(TransportProtocols.Utils.isSecureProtocol("http"))
        assertFalse(TransportProtocols.Utils.isSecureProtocol("ws"))
        assertFalse(TransportProtocols.Utils.isSecureProtocol("mqtt"))
        assertFalse(TransportProtocols.Utils.isSecureProtocol("p2p"))
    }
    
    @Test
    fun `test WebSocket protocol detection`() {
        assertTrue(TransportProtocols.Utils.isWebSocketProtocol("ws"))
        assertTrue(TransportProtocols.Utils.isWebSocketProtocol("wss"))
        assertTrue(TransportProtocols.Utils.isWebSocketProtocol("graphql+ws"))
        assertTrue(TransportProtocols.Utils.isWebSocketProtocol("graphql+wss"))
        
        assertFalse(TransportProtocols.Utils.isWebSocketProtocol("http"))
        assertFalse(TransportProtocols.Utils.isWebSocketProtocol("https"))
        assertFalse(TransportProtocols.Utils.isWebSocketProtocol("mqtt"))
        assertFalse(TransportProtocols.Utils.isWebSocketProtocol("p2p"))
    }
    
    @Test
    fun `test base protocol extraction from WebSocket`() {
        assertEquals("http", TransportProtocols.Utils.getBaseProtocolFromWebSocket("ws"))
        assertEquals("https", TransportProtocols.Utils.getBaseProtocolFromWebSocket("wss"))
        assertEquals("unknown", TransportProtocols.Utils.getBaseProtocolFromWebSocket("unknown"))
    }
    
    @Test
    fun `test all protocols list`() {
        val allProtocols = TransportProtocols.Utils.getAllProtocols()
        
        // Should contain all protocol types
        assertTrue(allProtocols.containsAll(TransportProtocols.HTTP.ALL))
        assertTrue(allProtocols.containsAll(TransportProtocols.MQTT.ALL))
        assertTrue(allProtocols.containsAll(TransportProtocols.P2P.ALL))
        assertTrue(allProtocols.containsAll(TransportProtocols.GraphQL.ALL))
        
        // Should not have duplicates
        assertEquals(allProtocols.size, allProtocols.toSet().size)
    }
    
    @Test
    fun `test case insensitive protocol validation`() {
        assertTrue(TransportProtocols.Utils.isValidProtocol("HTTP"))
        assertTrue(TransportProtocols.Utils.isValidProtocol("HTTPS"))
        assertTrue(TransportProtocols.Utils.isValidProtocol("MQTT"))
        assertTrue(TransportProtocols.Utils.isValidProtocol("P2P"))
        
        assertTrue(TransportProtocols.Utils.isHttpProtocol("HTTP"))
        assertTrue(TransportProtocols.Utils.isMqttProtocol("MQTT"))
        assertTrue(TransportProtocols.Utils.isP2PProtocol("P2P"))
    }
} 
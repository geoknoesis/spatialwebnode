package com.geoknoesis.spatialweb.core.transport

import com.geoknoesis.spatialweb.core.hstp.engine.HSTPEngine
import com.geoknoesis.spatialweb.core.hstp.model.HSTPMessage
import com.geoknoesis.spatialweb.identity.did.Did
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.ServiceLoader
import java.io.InputStream
import kotlin.coroutines.CoroutineContext


/**
 * Manages multiple TransportBinding implementations, routing messages
 * and delegating to the HSTP engine for processing.
 *
 * Discovers bindings via Java SPI and allows explicit configuration.
 */
class TransportManager(
    private val hstpEngine: HSTPEngine
) : TransportBinding, PubSubBinding {

    private val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    private val scope = CoroutineScope(coroutineContext)

    // Load bindings via SPI
    private val spiBindings: List<TransportBinding> = ServiceLoader
        .load(TransportBinding::class.java)
        .toList()
        
    // Load providers via SPI for configuration-driven instantiation
    private val providers: List<TransportBindingProvider> = ServiceLoader
        .load(TransportBindingProvider::class.java)
        .toList()

    // Explicitly configured bindings override SPI
    private var explicitBindings: List<TransportBinding>? = null

    /** Effective bindings in use */
    private val bindings: List<TransportBinding>
        get() = explicitBindings ?: spiBindings

    /** Configure explicit bindings, replacing SPI-discovered ones */
    fun configure(bindings: List<TransportBinding>) {
        explicitBindings = bindings
    }
    
    /**
     * Creates transport binding instances from configuration using providers.
     * 
     * @param configStream Input stream containing configuration (typically YAML)
     * @return List of created transport binding instances
     */
    fun createFromConfiguration(configStream: InputStream): List<TransportBinding> {
        val configuredBindings = mutableListOf<TransportBinding>()
        
        providers.forEach { provider ->
            try {
                val instances = provider.createInstances(configStream)
                configuredBindings.addAll(instances)
            } catch (e: Exception) {
                // Log error but continue with other providers
                println("Error creating instances from provider ${provider.getProviderName()}: ${e.message}")
            }
        }
        
        return configuredBindings
    }
    
    /**
     * Gets all available providers.
     */
    fun getProviders(): List<TransportBindingProvider> = providers
    
    /**
     * Gets a specific provider by name.
     */
    fun getProvider(name: String): TransportBindingProvider? {
        return providers.find { it.getProviderName() == name }
    }

    /** Start all configured bindings and attach handlers to feed HSTP engine */
    override fun start() {
        bindings.forEach { it.start() }
        bindings.forEach { bindIncoming(it) }
    }

    /** Stop all configured bindings */
    override fun stop() {
        bindings.forEach { it.stop() }
    }

    /**
     * Delegate outgoing HSTP messages to the engine to add headers/logic,
     * then route through appropriate bindings.
     */
    override fun send(message: HSTPMessage) {
        // For now, send directly via bindings
        // TODO: Add engine preprocessing when prepareOutbound is implemented
        when {
            message.header.destination != null ->
                bindings
                    .filter { it.supportsPointToPoint() }
                    .forEach { it.send(message) }

            message.header.channel != null ->
                bindings
                    .filterIsInstance<PubSubBinding>()
                    .forEach { it.send(message) }

            else -> throw IllegalArgumentException(
                "HSTPMessage must have destination or channel"
            )
        }
    }

    override fun onReceive(handler: (HSTPMessage) -> Unit) {
        TODO("Not yet implemented")
    }

    /**
     * Subscribe to a channel across all pub/sub-capable bindings
     * and notify engine for subscription logic.
     */
    override fun subscribe(channel: Did) {
        // TODO: Add engine notification when onSubscribe is implemented
        bindings.filterIsInstance<PubSubBinding>()
            .forEach { it.subscribe(channel) }
    }

    /** Unsubscribe from a channel and notify engine. */
    override fun unsubscribe(channel: Did) {
        // TODO: Add engine notification when onUnsubscribe is implemented
        bindings.filterIsInstance<PubSubBinding>()
            .forEach { it.unsubscribe(channel) }
    }

    /**
     * Incoming messages are passed directly to the HSTP engine for parsing and dispatch.
     */
    private fun bindIncoming(binding: TransportBinding) {
        binding.onReceive { message ->
            scope.launch {
                // TODO: Add engine parsing when feedInboundChunk and handleCompleteFrame are implemented
                // For now, handle the message directly
                hstpEngine.handleMessage(message)
            }
        }
    }
}

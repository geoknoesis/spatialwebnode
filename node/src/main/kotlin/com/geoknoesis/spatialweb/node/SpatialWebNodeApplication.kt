package com.geoknoesis.spatialweb.node

import com.geoknoesis.spatialweb.core.hstp.engine.DefaultHSTPEngine
import com.geoknoesis.spatialweb.core.hstp.operation.OperationManager
import com.geoknoesis.spatialweb.core.hstp.operation.PingOperationHandler
import com.geoknoesis.spatialweb.core.hstp.operation.PongOperationHandler
import com.geoknoesis.spatialweb.core.transport.TransportManager
import com.geoknoesis.spatialweb.identity.did.DefaultDidDocumentManager
import com.geoknoesis.spatialweb.identity.vc.DefaultCredentialVerifier
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * Main application class for the Spatial Web Node.
 * 
 * This application initializes the TransportManager and HSTPEngine using YAML configuration,
 * and provides a command-line interface for starting and stopping the node.
 */
class SpatialWebNodeApplication(
    private val configPath: String = "config/node.yml"
) {
    private val logger = LoggerFactory.getLogger(SpatialWebNodeApplication::class.java)
    private val isRunning = AtomicBoolean(false)
    private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Core components
    private lateinit var transportManager: TransportManager
    private lateinit var hstpEngine: DefaultHSTPEngine
    private lateinit var operationManager: OperationManager
    
    // Configuration
    private lateinit var nodeConfig: NodeConfig
    
    /**
     * Starts the Spatial Web Node application.
     */
    suspend fun start() {
        if (isRunning.compareAndSet(false, true)) {
            try {
                logger.info("Starting Spatial Web Node...")
                
                // Load configuration
                loadConfiguration()
                
                // Initialize components
                initializeComponents()
                
                // Start transport manager
                startTransportManager()
                
                // Register operation handlers
                registerOperationHandlers()
                
                // Set up shutdown hooks
                setupShutdownHooks()
                
                logger.info("Spatial Web Node started successfully")
                logger.info("Node ID: ${nodeConfig.nodeId}")
                logger.info("Transport providers: ${transportManager.getProviders().size}")
                
            } catch (e: Exception) {
                logger.error("Failed to start Spatial Web Node", e)
                isRunning.set(false)
                throw e
            }
        } else {
            logger.warn("Spatial Web Node is already running")
        }
    }
    
    /**
     * Stops the Spatial Web Node application.
     */
    suspend fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            try {
                logger.info("Stopping Spatial Web Node...")
                
                // Stop transport manager
                transportManager.stop()
                
                // Cancel all coroutines
                applicationScope.cancel()
                
                logger.info("Spatial Web Node stopped successfully")
                
            } catch (e: Exception) {
                logger.error("Error stopping Spatial Web Node", e)
                throw e
            }
        } else {
            logger.warn("Spatial Web Node is not running")
        }
    }
    
    /**
     * Checks if the application is currently running.
     */
    fun isRunning(): Boolean = isRunning.get()
    
    /**
     * Loads the node configuration from YAML file.
     */
    private fun loadConfiguration() {
        logger.info("Loading configuration from: $configPath")
        
        val configFile = File(configPath)
        if (!configFile.exists()) {
            logger.warn("Configuration file not found: $configPath")
            logger.info("Creating default configuration...")
            createDefaultConfiguration(configFile)
        }
        
        val inputStream = FileInputStream(configFile)
        nodeConfig = NodeConfigLoader.loadFromYaml(inputStream)
        
        logger.info("Configuration loaded successfully")
        logger.debug("Node configuration: $nodeConfig")
    }
    
    /**
     * Creates a default configuration file if none exists.
     */
    private fun createDefaultConfiguration(configFile: File) {
        val defaultConfig = NodeConfig.getDefault()
        NodeConfigLoader.saveToYaml(defaultConfig, configFile)
        logger.info("Default configuration created: ${configFile.absolutePath}")
    }
    
    /**
     * Initializes core components.
     */
    private fun initializeComponents() {
        logger.info("Initializing core components...")
        
        // Initialize operation manager
        operationManager = OperationManager()
        
        // Initialize DID document manager
        val didDocumentManager = DefaultDidDocumentManager()
        
        // Initialize credential verifier
        val credentialVerifier = DefaultCredentialVerifier()
        
        // Initialize HSTP engine
        hstpEngine = DefaultHSTPEngine(
            operationManager = operationManager,
            didDocumentManager = didDocumentManager,
            credentialVerifier = credentialVerifier
        )
        
        // Initialize transport manager
        transportManager = TransportManager(hstpEngine)
        
        logger.info("Core components initialized successfully")
    }
    
    /**
     * Starts the transport manager with configuration.
     */
    private suspend fun startTransportManager() {
        logger.info("Starting transport manager...")
        
        // Load transport configurations
        val transportConfigs = loadTransportConfigurations()
        
        // Configure transport bindings
        transportConfigs.forEach { config ->
            logger.info("Configuring transport: ${config.name} (${config.type})")
            // The transport manager will automatically discover and configure bindings via SPI
        }
        
        // Start transport manager
        transportManager.start()
        
        logger.info("Transport manager started successfully")
    }
    
    /**
     * Loads transport configurations from YAML files.
     */
    private fun loadTransportConfigurations(): List<TransportConfig> {
        val configs = mutableListOf<TransportConfig>()
        
        nodeConfig.transports.forEach { transportPath ->
            try {
                val transportFile = File(transportPath)
                if (transportFile.exists()) {
                    val inputStream = FileInputStream(transportFile)
                    val transportConfigs = TransportConfigLoader.loadFromYaml(inputStream)
                    configs.addAll(transportConfigs)
                    logger.info("Loaded ${transportConfigs.size} transport configurations from: $transportPath")
                } else {
                    logger.warn("Transport configuration file not found: $transportPath")
                }
            } catch (e: Exception) {
                logger.error("Failed to load transport configuration from: $transportPath", e)
            }
        }
        
        return configs
    }
    
    /**
     * Registers operation handlers.
     */
    private fun registerOperationHandlers() {
        logger.info("Registering operation handlers...")
        
        // Register ping/pong handlers
        operationManager.register(PingOperationHandler())
        operationManager.register(PongOperationHandler())
        
        // TODO: Register additional operation handlers here
        
        logger.info("Operation handlers registered successfully")
    }
    
    /**
     * Sets up shutdown hooks for graceful shutdown.
     */
    private fun setupShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Shutdown signal received, stopping Spatial Web Node...")
            runBlocking {
                stop()
            }
        })
    }
    
    /**
     * Gets the transport manager instance.
     */
    fun getTransportManager(): TransportManager = transportManager
    
    /**
     * Gets the HSTP engine instance.
     */
    fun getHstpEngine(): DefaultHSTPEngine = hstpEngine
    
    /**
     * Gets the operation manager instance.
     */
    fun getOperationManager(): OperationManager = operationManager
}
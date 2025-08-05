package com.geoknoesis.spatialweb.node

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * Main entry point for the hstpd (HSTP daemon) application.
 */
object NodeApplication {
    
    private val logger = LoggerFactory.getLogger(NodeApplication::class.java)
    
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            // Parse command line arguments
            val configPath = parseArguments(args)
            
            // Create and start the application
            val application = SpatialWebNodeApplication(configPath)
            
            // Start the application
            runBlocking {
                application.start()
                
                // Keep the application running
                logger.info("hstpd is running. Press Ctrl+C to stop.")
                
                // Wait for shutdown signal
                try {
                    // This will keep the application running until interrupted
                    while (application.isRunning()) {
                        kotlinx.coroutines.delay(1000)
                    }
                } catch (e: InterruptedException) {
                    logger.info("Shutdown signal received")
                }
                
                // Stop the application
                application.stop()
            }
            
        } catch (e: Exception) {
            logger.error("Failed to start hstpd", e)
            exitProcess(1)
        }
    }
    
    /**
     * Parses command line arguments.
     */
    private fun parseArguments(args: Array<String>): String {
        var configPath = "config/node.yml"
        
        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "--config", "-c" -> {
                    if (i + 1 < args.size) {
                        configPath = args[i + 1]
                        i++
                    } else {
                        logger.error("--config requires a file path")
                        printUsage()
                        exitProcess(1)
                    }
                }
                "--help", "-h" -> {
                    printUsage()
                    exitProcess(0)
                }
                "--version", "-v" -> {
                    printVersion()
                    exitProcess(0)
                }
                else -> {
                    logger.error("Unknown argument: ${args[i]}")
                    printUsage()
                    exitProcess(1)
                }
            }
            i++
        }
        
        return configPath
    }
    
    /**
     * Prints usage information.
     */
    private fun printUsage() {
        println("""
            hstpd - HSTP (Hypermedia Spatial Transport Protocol) daemon
            
            Usage: hstpd [options]
            
            Options:
              -c, --config <file>    Configuration file path (default: config/node.yml)
              -h, --help            Show this help message
              -v, --version         Show version information
            
            Examples:
              hstpd
              hstpd --config /path/to/config.yml
              hstpd -c config/production.yml
        """.trimIndent())
    }
    
    /**
     * Prints version information.
     */
    private fun printVersion() {
        println("hstpd v1.0.0")
        println("Built with Kotlin ${KotlinVersion.CURRENT}")
        println("JVM ${System.getProperty("java.version")}")
    }
} 
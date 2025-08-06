package com.geoknoesis.spatialweb.core.udg.model

import java.time.Instant

/**
 * Represents a hyperspace in the UDG
 *
 * A hyperspace defines the spatial and temporal context for a domain.
 * It includes coordinate systems, dimensions, and spatial relationships
 * that define how entities within the domain are positioned and related.
 */
data class Hyperspace(
    /** The IRI of the hyperspace */
    val iri: IRI,
    
    /** The name of the hyperspace */
    val name: String,
    
    /** Optional description of the hyperspace */
    val description: String? = null,
    
    /** The coordinate system used by this hyperspace */
    val coordinateSystem: CoordinateSystem,
    
    /** The dimensions of the hyperspace */
    val dimensions: List<Dimension> = emptyList(),
    
    /** The spatial bounds of the hyperspace */
    val bounds: SpatialBounds? = null,
    
    /** The temporal bounds of the hyperspace */
    val temporalBounds: TemporalBounds? = null,
    
    /** Properties of the hyperspace */
    val properties: Map<Property, RdfTerm> = emptyMap(),
    
    /** Creation timestamp */
    val createdAt: Instant = Instant.now(),
    
    /** Last modification timestamp */
    val modifiedAt: Instant = Instant.now(),
    
    /** Version of the hyperspace */
    val version: String = "1.0",
    
    /** Additional metadata */
    val metadata: Map<String, Any> = emptyMap()
) {
    
    /**
     * Gets a property value by its IRI
     */
    fun getProperty(property: Property): RdfTerm? = properties[property]
    
    /**
     * Sets a property value
     */
    fun setProperty(property: Property, value: RdfTerm): Hyperspace {
        return copy(
            properties = properties + (property to value),
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Removes a property
     */
    fun removeProperty(property: Property): Hyperspace {
        return copy(
            properties = properties - property,
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Checks if the hyperspace has a specific property
     */
    fun hasProperty(property: Property): Boolean = properties.containsKey(property)
    
    /**
     * Gets all property IRIs
     */
    fun getPropertyNames(): Set<Property> = properties.keys
    
    /**
     * Gets a dimension by name
     */
    fun getDimension(name: String): Dimension? = dimensions.find { it.name == name }
    
    /**
     * Adds a dimension to the hyperspace
     */
    fun addDimension(dimension: Dimension): Hyperspace {
        return copy(
            dimensions = dimensions + dimension,
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Removes a dimension from the hyperspace
     */
    fun removeDimension(name: String): Hyperspace {
        return copy(
            dimensions = dimensions.filter { it.name != name },
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Creates a new version of this hyperspace
     */
    fun withVersion(newVersion: String): Hyperspace {
        return copy(
            version = newVersion,
            modifiedAt = Instant.now()
        )
    }
    
    /**
     * Updates the modification timestamp
     */
    fun touch(): Hyperspace {
        return copy(modifiedAt = Instant.now())
    }
    
    companion object {
        /**
         * Creates a simple 3D Cartesian hyperspace
         */
        fun create3DCartesian(name: String, description: String? = null): Hyperspace {
            val dimensions = listOf(
                Dimension("x", DimensionType.SPATIAL, "X coordinate"),
                Dimension("y", DimensionType.SPATIAL, "Y coordinate"),
                Dimension("z", DimensionType.SPATIAL, "Z coordinate")
            )
            
            return Hyperspace(
                iri = IRI("http://example.com/hyperspaces/$name"),
                name = name,
                description = description,
                coordinateSystem = CoordinateSystem.CARTESIAN_3D,
                dimensions = dimensions
            )
        }
        
        /**
         * Creates a 2D Cartesian hyperspace
         */
        fun create2DCartesian(name: String, description: String? = null): Hyperspace {
            val dimensions = listOf(
                Dimension("x", DimensionType.SPATIAL, "X coordinate"),
                Dimension("y", DimensionType.SPATIAL, "Y coordinate")
            )
            
            return Hyperspace(
                iri = IRI("http://example.com/hyperspaces/$name"),
                name = name,
                description = description,
                coordinateSystem = CoordinateSystem.CARTESIAN_2D,
                dimensions = dimensions
            )
        }
        
        /**
         * Creates a temporal hyperspace
         */
        fun createTemporal(name: String, description: String? = null): Hyperspace {
            val dimensions = listOf(
                Dimension("time", DimensionType.TEMPORAL, "Time coordinate")
            )
            
            return Hyperspace(
                iri = IRI("http://example.com/hyperspaces/$name"),
                name = name,
                description = description,
                coordinateSystem = CoordinateSystem.TEMPORAL,
                dimensions = dimensions
            )
        }
    }
}

/**
 * Represents a coordinate system
 */
enum class CoordinateSystem {
    /** 2D Cartesian coordinate system */
    CARTESIAN_2D,
    
    /** 3D Cartesian coordinate system */
    CARTESIAN_3D,
    
    /** Polar coordinate system */
    POLAR,
    
    /** Spherical coordinate system */
    SPHERICAL,
    
    /** Geographic coordinate system (latitude/longitude) */
    GEOGRAPHIC,
    
    /** Temporal coordinate system */
    TEMPORAL,
    
    /** Custom coordinate system */
    CUSTOM
}

/**
 * Represents a dimension in a hyperspace
 */
data class Dimension(
    /** The name of the dimension */
    val name: String,
    
    /** The type of the dimension */
    val type: DimensionType,
    
    /** Optional description of the dimension */
    val description: String? = null,
    
    /** The unit of measurement for this dimension */
    val unit: String? = null,
    
    /** The minimum value for this dimension */
    val minValue: Double? = null,
    
    /** The maximum value for this dimension */
    val maxValue: Double? = null,
    
    /** Additional metadata */
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Types of dimensions
 */
enum class DimensionType {
    /** Spatial dimension */
    SPATIAL,
    
    /** Temporal dimension */
    TEMPORAL,
    
    /** Categorical dimension */
    CATEGORICAL,
    
    /** Ordinal dimension */
    ORDINAL,
    
    /** Custom dimension */
    CUSTOM
}

/**
 * Represents spatial bounds
 */
data class SpatialBounds(
    /** Minimum coordinates */
    val min: List<Double>,
    
    /** Maximum coordinates */
    val max: List<Double>,
    
    /** Coordinate system */
    val coordinateSystem: CoordinateSystem
) {
    init {
        require(min.size == max.size) { "Min and max coordinates must have the same number of dimensions" }
        require(min.zip(max).all { (minVal, maxVal) -> minVal <= maxVal }) { "Min values must be less than or equal to max values" }
    }
    
    /**
     * Gets the number of dimensions
     */
    val dimensions: Int = min.size
    
    /**
     * Checks if a point is within these bounds
     */
    fun contains(point: List<Double>): Boolean {
        if (point.size != dimensions) return false
        return point.zip(min.zip(max)).all { (pointValue, bounds) ->
            val (minVal, maxVal) = bounds
            pointValue >= minVal && pointValue <= maxVal
        }
    }
}

/**
 * Represents temporal bounds
 */
data class TemporalBounds(
    /** Start time */
    val start: Instant,
    
    /** End time */
    val end: Instant
) {
    init {
        require(start <= end) { "Start time must be before or equal to end time" }
    }
    
    /**
     * Checks if a time is within these bounds
     */
    fun contains(time: Instant): Boolean = time >= start && time <= end
    
    /**
     * Gets the duration in milliseconds
     */
    val durationMs: Long = end.toEpochMilli() - start.toEpochMilli()
} 
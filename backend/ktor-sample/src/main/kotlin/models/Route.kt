package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
// Optional Field(s): estimatedTime
data class Route(
    val routeID: Int,
    val userID: Int,
    val name: String,
    val distance: Double,
    val estimatedTime: Double? = 0.0,
    val date: String?,
    val isPublic: Boolean,
)

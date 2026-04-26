package models

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val routeID: Int,
    val userID: Int,
    val name: String,
    val distance: Double,
    val estimatedTime: Double?,
    val date: String?,
    val isPublic: Boolean,
)

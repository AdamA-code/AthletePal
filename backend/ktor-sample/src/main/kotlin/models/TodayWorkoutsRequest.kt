package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class TodayWorkoutsRequest(
    val email: String?,
)

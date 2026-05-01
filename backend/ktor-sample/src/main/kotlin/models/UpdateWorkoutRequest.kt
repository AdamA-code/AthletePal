package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class UpdateWorkoutRequest(
    val date: String,
    val duration: Int,
    val workoutType: String,
    val notes: String?,
    val routeID: Int?,
)

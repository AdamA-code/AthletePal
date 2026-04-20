package models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateWorkoutRequest(
    val date: String,
    val duration: Int,
    val workoutType: String,
    val notes: String?,
    val routeID: Int?
)
package models

import kotlinx.serialization.Serializable

@Serializable
data class Workout(
    val workoutID: Int,
    val createdBy: Int,
    val assignedTo: Int,
    val date: String,
    val duration: Int?,
    val isRoutine: Boolean? = false,
)

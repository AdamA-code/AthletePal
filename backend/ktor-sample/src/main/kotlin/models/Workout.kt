package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class Workout(
    val workoutID: Int,
    val createdBy: Int,
    val assignedTo: Int,
    val date: String,
    val duration: Int?,
)

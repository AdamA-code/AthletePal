package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class WorkoutExerciseResponse(
    val exerciseID: Int,
    val sets: Int?,
    val reps: Int?,
    val weight: Double?,
)

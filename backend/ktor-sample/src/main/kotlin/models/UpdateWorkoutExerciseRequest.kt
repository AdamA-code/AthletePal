package models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateWorkoutExerciseRequest (
    val sets: Int?,
    val reps: Int?,
    val weight: Double?
)
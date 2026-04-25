package models
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutExerciseResponse(
    val exerciseID: Int,
    val sets: Int?,
    val reps: Int?,
    val weight: Double?,
)

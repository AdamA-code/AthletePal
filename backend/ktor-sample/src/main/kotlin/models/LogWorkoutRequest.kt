package models

import kotlinx.serialization.Serializable
import services.DatabaseService.Workout as Workout
import services.DatabaseService.WorkoutExercise as WorkoutExercise

@Serializable
data class LogWorkoutRequest(
    val workout: Workout,
    val exercises: List<WorkoutExercise>,
)

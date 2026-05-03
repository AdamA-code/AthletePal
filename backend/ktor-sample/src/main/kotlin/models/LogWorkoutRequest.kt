package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation
import services.DatabaseService.Workout as Workout // Import Workout mode, for inheritance to sub-type of LogWorkoutRequest
import services.DatabaseService.WorkoutExercise as WorkoutExercise // Import WorkoutExercise model, for inheritance to subtype of this model

@Serializable // Declare model to be serialisable
data class LogWorkoutRequest(
    val workout: Workout,
    val exercises: List<WorkoutExercise>,
)

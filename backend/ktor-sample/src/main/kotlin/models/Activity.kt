package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation
import services.DatabaseService.Competition as Competition // Import Competition model, for inheritance as sub-type in Activity
import services.DatabaseService.Route as Route // Import Route model, for inheritance as sub-type in Activity
import services.DatabaseService.Workout as Workout // Import Workout model, for inheritance as sub-type in Activity

@Serializable // Declare model to be serializable
data class Activity(
    val workouts: List<Workout>,
    val competitions: List<Competition>,
    val routes: List<Route>,
)

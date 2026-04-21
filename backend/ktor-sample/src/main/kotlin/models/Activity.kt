package models

import kotlinx.serialization.Serializable
import services.DatabaseService.Competition as Competition
import services.DatabaseService.Route as Route
import services.DatabaseService.Workout as Workout

@Serializable
data class Activity(
    val workouts: List<Workout>,
    val competitions: List<Competition>,
    val routes: List<Route>,
)

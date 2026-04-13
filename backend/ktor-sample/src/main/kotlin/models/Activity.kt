package models

import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val workouts: List<Workout>,
    val competitions: List<Competitions>,
    val routes: List<Route>
)
package models

import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val date: String,
    val workoutType: String,
    val duration: String,
)
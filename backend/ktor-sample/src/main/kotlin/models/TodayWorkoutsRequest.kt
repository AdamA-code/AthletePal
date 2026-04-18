package models

import kotlinx.serialization.Serializable

@Serializable
data class TodayWorkoutsRequest(
    val email: String?,
    val username: String? = null
)
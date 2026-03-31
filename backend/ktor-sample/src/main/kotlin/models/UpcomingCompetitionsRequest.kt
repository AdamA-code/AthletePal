package services

import kotlinx.serialization.Serializable

@Serializable
data class UpcomingCompetitionsRequest(
    val email: String?,
    val username: String?
)
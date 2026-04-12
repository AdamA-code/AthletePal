package models

import kotlinx.serialization.Serializable

@Serializable
data class RecentActivityRequest(
    val email: String?,
    val username: String?
)
package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class RecentActivityRequest(
    val email: String?,
)

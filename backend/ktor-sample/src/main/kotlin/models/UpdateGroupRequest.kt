package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
// Optional Field(s): description, goal
data class UpdateGroupRequest(
    val name: String,
    val description: String? = null,
    val goal: String? = null,
)

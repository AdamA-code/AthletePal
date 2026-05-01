package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class UpdateGroupRequest(
    val name: String,
    val description: String? = null, // Optional Field: Defaults to null
    val goal: String? = null, // Optional Field: Defaults to null
)

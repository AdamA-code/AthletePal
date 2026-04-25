package models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGroupRequest(
    val name: String,
    val description: String? = null,
    val goal: String? = null
)
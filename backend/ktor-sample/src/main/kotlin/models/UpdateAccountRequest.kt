package models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateAccountRequest(
    val email: String,
    val gender: String? = null,
    val age: Int? = 0,
    val height: Double? = 0.0,
    val weight: Double? = 0.0,
)

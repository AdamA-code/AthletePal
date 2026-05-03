package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
// Optional Field(s): gender, age, height, weight
data class UpdateAccountRequest(
    val email: String,
    val gender: String? = null,
    val age: Int? = 0,
    val height: Double? = 0.0,
    val weight: Double? = 0.0,
)

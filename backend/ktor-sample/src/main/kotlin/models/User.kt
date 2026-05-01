package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class User(
    val userID: Int,
    val name: String,
    val email: String?,
    val passwordHash: String,
    val passwordKey: String,
    val gender: String? = null, // Optional Field: Defaults to null
    val age: Int? = 0, // Optional Field: Defaults to 0
    val height: Double? = 0.0, // Optional Field: Defaults to 0.0
    val weight: Double? = 0.0, // Optional Field: Defaults to 0.0
    val accountType: Boolean,
)

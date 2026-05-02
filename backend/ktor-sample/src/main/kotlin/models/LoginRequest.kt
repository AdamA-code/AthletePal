package models // Denotes a model type

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
// Represents format for JSON in frontend's request
data class LoginRequest(
    val email: String,
    val password: String,
)

package models // Denotes a model type

import kotlinx.serialization.Serializable

@Serializable
// Represents format for JSON in frontend's request
data class LoginRequest(
    val email: String,
    val password: String,
)

package models // Denotes a model type

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest( // Represents format for JSON in frontend's request
    val username: String,
    val password: String
)

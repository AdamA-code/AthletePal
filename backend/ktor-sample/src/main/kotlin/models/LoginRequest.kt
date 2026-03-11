package models // Denotes a model type

import kotlinx.serialization.Serializable

data class LoginRequest( // Represents format for JSON in frontend's request
    val username: String,
    val password: String
)
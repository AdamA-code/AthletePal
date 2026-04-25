package models

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest (
    val token: String,
    val newPassword: String
)

package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class ForgotPasswordRequest (
    val token: String,
    val newPassword: String
)

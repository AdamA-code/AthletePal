package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Delare model to be serialisable
data class PasswordUpdateRequest (
    val oldPassword: String,
    val newPassword: String
)
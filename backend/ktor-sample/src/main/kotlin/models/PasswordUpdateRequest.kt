package models

import kotlinx.serialization.Serializable

@Serializable
data class PasswordUpdateRequest (
    val oldPassword: String,
    val newPassword: String
)
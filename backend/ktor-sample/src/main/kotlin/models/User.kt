package models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userID: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    val passwordKey: String,
    val gender: String?,
    val age: Int?,
    val height: Double?,
    val weight: Double?,
    val accountType: Boolean
)
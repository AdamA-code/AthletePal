package models
import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val username: String,
    val email: String,
    val password: String,

    val gender: String?,
    val age: Int,
    val height: Int?,
    val weight: Float?,

    val accountType: Boolean

)
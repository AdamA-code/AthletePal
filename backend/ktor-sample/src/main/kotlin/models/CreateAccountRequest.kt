package models
import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val name: String,
    val email: String,
    val password: String,
    val gender: String? = null,
    val age: Int? = 0,
    val height: Int? = 0,
    val weight: Double? = 0.0,
    val accountType: Boolean,
)

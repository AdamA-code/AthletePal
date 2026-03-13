package models

import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val username: String,
    val email: String,
    val password: String,
    
    /*
    POTENTIAL DATA TO EXPECT

    val gender: String,
    val age: Int,
    val height: Int,
    val weight: Float,
    val isPT: Boolean

    */

)
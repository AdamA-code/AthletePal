package models

import kotlinx.serialization.Serializable

@Serializable
data class Competitions (
    val competitionID: Int,
    val userID: Int,
    val name: String,
    val date: String,
    val rank: Int?,
    val result: Int?

)
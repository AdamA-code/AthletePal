package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable by kotlinx
data class Competitions(
    val competitionID: Int,
    val userID: Int,
    val name: String,
    val date: String,
    val rank: Int?,
    val result: Int?,
)

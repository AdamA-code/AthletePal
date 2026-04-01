package models

import kotlinx.serialization.Serializable

@Serializable

data class Competitions (
    val competitionName: String,
    val date: String,
    val competitionType: String,
    val competitionRank: String

)
package models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCompetitionRequest(
    val name: String,
    val date: String,
    val rank: Int? = null,
    val result: Int? = null,
)

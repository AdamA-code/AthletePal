package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
// Optional Field(s): rank, result
data class UpdateCompetitionRequest(
    val name: String,
    val date: String,
    val rank: Int? = null,
    val result: Int? = null,
)

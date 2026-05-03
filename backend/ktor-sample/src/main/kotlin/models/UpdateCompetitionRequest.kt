package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class UpdateCompetitionRequest(
    val name: String,
    val date: String,
    val rank: Int? = null, // Optional Field: Defaults to null
    val result: Int? = null, // Optional Field: Defaults to null
)

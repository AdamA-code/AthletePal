package models

import kotlinx.serialization.Serializable // Import kotlinx serialisation

@Serializable // Declare model to be serialisable
data class UpdateStatisticRequest(
    val scoreValue: Double,
    val date: String,
)

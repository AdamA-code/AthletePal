package models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStatisticRequest(
    val scoreValue: Double,
    val date: String
)
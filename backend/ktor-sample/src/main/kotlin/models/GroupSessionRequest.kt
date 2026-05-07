package models

import kotlinx.serialization.Serializable
import services.DatabaseService.GroupSession as GroupSession

@Serializable
data class GroupSessionRequest(
    val createdBy: Int,
    val session: GroupSession,
)

package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.* // Import functionalityy to read JSON requests
import io.ktor.server.response.* // Import functionality to send responses to frontend
import io.ktor.http.*

import models.UpdateGroupRequest

import services.DatabaseService.TrainingGroup as TrainingGroup
import services.DatabaseService.GroupMember as GroupMember
import services.DatabaseService.GroupSession as GroupSession
import services.DatabaseService.User as User

import services.DatabaseService

fun Route.socialRoutes() {
    val db = DatabaseService()

    route("/social") {

        // =====================
        // GROUP CRUD ENDPOINTS
        // =====================

        // Create new training group
        post("/group") {
            try {
                val trainingGroup = call.receive<TrainingGroup>()
                val id: Int = db.createGroup(trainingGroup)

                if (id != -1) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                }
                else if (id == -1) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Could not create group"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Update training group details
        put("/group/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val body = call.receive<UpdateGroupRequest>()

                val updated: Boolean = db.updateGroup(groupID, body.name, body.description, body.goal)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Training group updated"))
                }
                else if (!updated) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Training group not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Delete training group
        delete("/group/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val deleted: Boolean = db.deleteGroup(groupID)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Training group deleted"))
                }
                else if (!deleted) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Training group not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Get single, specific training group
        get("/group/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val trainingGroup: TrainingGroup? = db.findGroup(groupID)

                if (trainingGroup != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Training Group" to trainingGroup))
                }
                else if (trainingGroup == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Training group not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Get all training groups a user is in
        get("/groups/{userID}") {
            try {
                val userID: Int = call.parameters["userID"]!!.toInt()
                val trainingGroups: List<TrainingGroup> = db.findAllGroups(userID)

                if (!trainingGroups.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Training Groups" to trainingGroups))
                }
                else if (trainingGroups.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No training groups found for user"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Add user to an existing training group
        put("/group/{groupID}/{userID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val userID: Int = call.parameters["userID"]!!.toInt()

                val userAdded: Boolean = db.joinGroup(groupID, userID)

                if (userAdded) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User added to group"))
                }
                else if (!userAdded) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Group not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Remove a user from a training group
        delete("/group/{groupID}/{userID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val userID: Int = call.parameters["userID"]!!.toInt()

                val userRemoved: Boolean = db.leaveGroup(groupID, userID)

                if (userRemoved) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User removed from group"))
                }
                else if (!userRemoved) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "User OR Group not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Get all members in a training group
        get("/group/members/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val groupMembers: List<User> = db.findGroupMembers(groupID)

                if (!groupMembers.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Group Members" to groupMembers))
                }
                else if (groupMembers.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No group members found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }


        // ========================
        // GROUP SESSION ENDPOINTS
        // ========================

        // Create a new group training session
        post("/session") {
            try {
                val groupSession: GroupSession = call.receive<GroupSession>()
                val id: Int = db.addGroupSession(groupSession)

                if (id != -1) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                }
                else if (id == -1) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Group session couldn't be created"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Get all upcoming group training sessions
        get("/sessions/upcoming/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val upcomingSessions: List<GroupSession> = db.getUpcomingGroupSessions(groupID)

                if (!upcomingSessions.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Upcoming Sessions" to upcomingSessions))
                }
                else if (upcomingSessions.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No upcoming sessions found for group"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Get all past group training sessions
        get("/sessions/past/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val pastSessions: List<GroupSession> = db.getPastGroupSessions(groupID)

                if (!pastSessions.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Past Sessions" to pastSessions))
                }
                else if (pastSessions.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No past sessions found for group"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Delete a specific group session
        delete("/session/{sessionID}") {
            try {
                val sessionID: Int = call.parameters["sessionID"]!!.toInt()
                val deleted: Boolean = db.deleteGroupSession(sessionID)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Group session deleted"))
                }
                else if (!deleted) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Group session not found"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

    }
}
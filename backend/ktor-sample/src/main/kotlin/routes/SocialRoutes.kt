package routes

import io.ktor.http.HttpStatusCode // Import different HTTP status codes, for more detailed HTTP responses
import io.ktor.server.request.receive // Import functionality to recieve HTTP requests
import io.ktor.server.response.respond // Import functionality to respond to recieved HTTP requests
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete // Import DELETE HTTP request routes
import io.ktor.server.routing.get // Import GET HTTP request routes
import io.ktor.server.routing.post // Import POST HTTP request routes
import io.ktor.server.routing.put // Import PUT HTTP request routes
import io.ktor.server.routing.route
import models.UpdateGroupRequest // Import the data class model for requesting to update a group
import services.DatabaseService // Import functions and methods for CRUD operations to db
import services.DatabaseService.GroupSession as GroupSession
import services.DatabaseService.TrainingGroup as TrainingGroup
import services.DatabaseService.User as User

/*
Defines endpoints for CRUD operations for social-aspects of the webapp.
This includes:
    - Creating, leaving, joining workout groups
    - Adding people, updating, and removing people from workout groups
    - Viewing workout groups
    - Creating, and deleting a workout group's session(s)
    - Viewing upcoming and past workout group sessions
*/
fun Route.socialRoutes() {
    val db = DatabaseService()

    /*
    Social Routes ('/social/...')
    Serves as a subdivider in order to seperate following social-based endpoints
    from other endpoints (such as activity, statistics, authentication, etc)
     */
    route("/social") {
        // =====================
        // GROUP CRUD ENDPOINTS
        // =====================

        /*
        POST '/group' requests are responsible for allowing users to create a new training
        group, with specified details, and (by default) adding themselves to it as creator.

        The endpoint expects a syntactically sound HTTP request, which contains values for
        creator's ID (createdBy) and name (name) as well as, optionally, a group description
        (description) and goal (goal).

        The endpoint returns:
            - OK (200) and the ID of the new group, if the group is successfully created
            - InternalServerError (500) and a relevent message, if the group could not be created
            - BadRequest (400) and a relevent message, if a malformed HTTP request is provided
         */
        post("/group") {
            try {
                val trainingGroup = call.receive<TrainingGroup>()
                val id: Int = db.createGroup(trainingGroup)

                if (id != -1) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else if (id == -1) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Could not create group"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        PUT '/group/{groupID}' endpoint is responsible for handling HTTP requests for updating
        an existing group.

        The endpoint expects a valid, syntactically correct HTTP request with group ID (groupID)
        given as a parameter in the route; it expects the body to contain updated group name (name),
        and optionally updated group description (description) and updated goal (goal).

        The endpoint returns:
            - OK (200) and a relevent message, if the group has its info successfully updated
            - NotFound (404) and a relevent message, if the target group could not be found in the database
            - BadRequest (400) and a relevent message, if given a malformed HTTP request
         */
        put("/group/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val body = call.receive<UpdateGroupRequest>()

                val updated: Boolean = db.updateGroup(groupID, body.name, body.description, body.goal)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Training group updated"))
                } else if (!updated) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Training group not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        DELETE '/group/{groupID}' endpoint is responsible for handling HTTP requests for deleting a
        specific, existing group.

        The endpoint expects a valid HTTP request, which contains the target group's ID (groupID)
        given as a parameter at the end of the route.

        The endpoint returns:
            - OK (200) and a relevant message, if the specified group is successfully deleted from the database
            - NotFound (404) and a relevant message, if the target group could not be found in the database (i.e. wrong groupID?)
            - BadRequest (400) and a relevant message, if given a malformed HTTP request
         */
        delete("/group/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val deleted: Boolean = db.deleteGroup(groupID)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Training group deleted"))
                } else if (!deleted) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Training group not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        GET '/group/{groupID}' is the endpoint responsible for returning all stored information for
        a specific group in the database.

        The endpoint expects to be given the target group's group ID (groupID), as a parameter at the
        end of the route.

        The endpoint returns:
            - OK (200) and the group's stored information, if the group was found in the database
            - NotFound (404) and a relevant message, if the group could not be found using the groupID (may be incorrect)
            - BadRequest (400) and a relevant message, if given a malformed HTTP request
         */
        get("/group/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val trainingGroup: TrainingGroup? = db.findGroup(groupID)

                if (trainingGroup != null) {
                    call.respond(HttpStatusCode.OK, mapOf("Training Group" to trainingGroup))
                } else if (trainingGroup == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Training group not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        GET '/groups/{userID}' is the endpoint for retrieving the information of all the groups
        which a specified user is a member, or creator, of.

        The endpoint expects the ID of the user we want to search groups by, given as a parameter
        at the end of the route.

        The endpoint returns:
            - OK (200) and a list of all training groups' information, if groups with the user as a member could be found
            - NotFound (404) and a relevant message, if no training groups with the user as a member could be found
            - BadRequest (400) and a relevant message, if given a malformed HTTP request
         */
        get("/groups/{userID}") {
            try {
                val userID: Int = call.parameters["userID"]!!.toInt()
                val trainingGroups: List<TrainingGroup> = db.findAllUserGroups(userID)

                if (!trainingGroups.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Training Groups" to trainingGroups))
                } else if (trainingGroups.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No training groups found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The GET '/groups' endpoint is responsible for handling HTTP requests for finding any and all training
        groups which are stored in the database, created by and available to anyone (i.e. groups are not filtered).
        It is therefore also responsible for calling the neccessary backend service's functions to fulfil this
        request from the databse, for return to the requesting client.

        The endpoint does not expect any parameters in the request, and only requires a valid GET HTTP request
        to be made to the correct address.

        The endpoint returns:
            - OK (200) and a list of all groups in the database, if there were training groups stored in the db retrieved by the backend
            - InternalServerError (500) and a relevant message, if no groups were found in the database (likely indicating a database request error)
            - BadRequest (400) and a relevant message, if a malformed HTTP request was recieved by the endpoint
         */
        get("/groups") {
            try {
                val allGroups: List<TrainingGroup> = db.findAllGroups()

                if (!allGroups.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Groups" to allGroups))
                } else if (allGroups.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "No groups found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The GET '/groups/no-user/{userID}' endpoint is responsible for handling HTTP requests made to
        retrieve all groups which the specified user is not a member of (and can therefore join). The
        endpoint is therefore also responsible for calling the correct, required backend service's
        functions which are needed to fulfil this request from the database, for return to the requesting
        client.

        The endpoint epxects the target user's ID (userID) to be passed as a integer-serialisable
        parameter at the end of the endpoint's route, providing the service function a value to
        search by.

        The endpoint returns:
            - OK (200) and a list of all groups the user isnt a member of, if there were groups found not containing the user
            - NotFound (404) and a relevant message, if there were no groups found without the specified user in (i.e. none to join)
            - BadRequest (400) and a relevant message, if the endpoint was provided with a malformed HTTP request
         */
        get("/groups/no-user/{userID}") {
            try {
                val userID: Int = call.parameters["userID"]!!.toInt()
                val nonUserGroups: List<TrainingGroup> = db.findAllNonUserGroups(userID)

                if (!nonUserGroups.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Available Groups" to nonUserGroups))
                } else if (nonUserGroups.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No available groups"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The PUT '/group/{groupID}/{userID} endpoint is responsible for adding a specific user to
        a single, specified group.

        The endpoint expects to be given the IDs of both the user to be added, and group they are
        to be added to. These are both to be passed as parameters at the end of the route, with
        group's ID (groupID) passed first THEN the user's ID (userID) after.

        The endpoint returns:
            - OK (200) and a relevant message, if the user was successfully added to the group
            - NotFound (404) and a relevant message, if the user/group could not be found, or user already in group
            - BadRequest (400) and a relevant message, if given a malformed HTTP request
         */
        put("/group/{groupID}/{userID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val userID: Int = call.parameters["userID"]!!.toInt()

                val userAdded: Boolean = db.joinGroup(groupID, userID)

                if (userAdded) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User added to group"))
                } else if (!userAdded) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Group not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The DELETE '/group/{groupID}/{userID}' endpoint is responsible for handling requests for deleting
        a specific user from a group (i.e. a user leaves a group).

        The endpoint expects both the group's ID (groupID) and user's ID (userID) to be passed as parameters
        at the end of the endpoint's route - with groupID followed by userID.

        The endpoint returns:
            - OK (200) and a relevent message, if the user was successfully removed from the group
            - NotFound (404) and a relevant message, if the group couldn't be found, or the user is not in said group
            - BadRequest (400) and a relevant message, if given a malformed HTTP request which can't be handled
         */
        delete("/group/{groupID}/{userID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val userID: Int = call.parameters["userID"]!!.toInt()

                val userRemoved: Boolean = db.leaveGroup(groupID, userID)

                if (userRemoved) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User removed from group"))
                } else if (!userRemoved) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "User OR Group not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The GET '/group/members/{groupID}' endpoint is responsible for handling requests where the
        user wants to be given a list of all members of a specific training group.

        The endpoint expects the target group's ID to be given as a parameter at the end of endpoint's
        route.

        The endpoint returns:
            - OK (200) and a list of all the group's members, if the group was found and had active members
            - NotFound (404) and a relevent message, if either the group wasn't found, or the group had no members
            - BadRequest (400) and a relevant message, if given a malformed HTTP request
         */
        get("/group/members/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val groupMembers: List<User> = db.findGroupMembers(groupID)

                if (!groupMembers.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Group Members" to groupMembers))
                } else if (groupMembers.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No group members found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // ========================
        // GROUP SESSION ENDPOINTS
        // ========================

        /*
        The POST '/session' endpoint is responsible for handling HTTP requests for making new group sessions for a
        training group.

        The endpoint expects to be given a valid, syntactically correct HTTP request where the body contains values for
        the associated group's ID (groupID), the planned workout's ID (workoutID) and the date the session is scheduled
        for (date). All values are required, none are optional.

        The endpoint returns:
            - OK (200) and the ID of the newly created group session, if the group session was successfully created
            - InternalServerError (500) and a relevant message, if the group session couldn't be created
            - BadRequest (400) and a relevant message, if provided a malformed HTTP request
         */
        post("/session") {
            try {
                val groupSession: GroupSession = call.receive<GroupSession>()
                val id: Int = db.addGroupSession(groupSession)

                if (id != -1) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to id))
                } else if (id == -1) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Group session couldn't be created"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The GET '/sessions/upcoming/{groupID}' endpoint is responsible for getting and responding with group sessions for a
        specific group which are upcoming (i.e. in future from current date).

        The endpoint expects the target group's ID (groupID) given as a parameter at the end of the endpoint's route.

        The endpoint returns:
            - OK (200) and a list of upcoming group sessions, if there could be upcoming sessions found for the target group
            - NotFound (404) and a relevant message, if either the group wasn't found, or no sessions could be found for the group
            - BadRequest (400) and a relevant message, if provided with a malformed HTTP request
         */
        get("/sessions/upcoming/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val upcomingSessions: List<GroupSession> = db.getUpcomingGroupSessions(groupID)

                if (!upcomingSessions.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Upcoming Sessions" to upcomingSessions))
                } else if (upcomingSessions.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No upcoming sessions found for group"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The GET '/sessions/past/{groupID}' endpoint is responsible for getting and responding with a specified group's
        sessions which have already passed (i.e. they have already happened, based on the current date).

        The endpoint expects the target group's ID (groupID) ot be passed as a parameter at the end of the endpoint's
        route.

        The endpoint returns:
            - OK (200) and a list of all past sessions, if the group was found and had sessions which have passed
            - NotFound (404) and a relevant message, if the target group wasn't found, or had no past sessions associated with it
            - BadRequest (400) and a relevant message if provided with a malformed HTTP request
         */
        get("/sessions/past/{groupID}") {
            try {
                val groupID: Int = call.parameters["groupID"]!!.toInt()
                val pastSessions: List<GroupSession> = db.getPastGroupSessions(groupID)

                if (!pastSessions.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("Past Sessions" to pastSessions))
                } else if (pastSessions.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "No past sessions found for group"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        /*
        The DELETE '/session/{sessionID}' endpoint is responsible for deleting a specific group session, and
        disassociating it from the training group which created it.

        The endpoint expects to be provided with the group session's ID (sessionID) as a parameter at the end
        of the endpoint's route.

        The endpoint returns:
            - OK (200) and a relevant message, if the group session was successfully deleted from the database
            - NotFound (404) and a relevant message, if the targeted group session could not be found in the database
            - BadRequest (400) and a relevant message, if provided with a malformed HTTP request
         */
        delete("/session/{sessionID}") {
            try {
                val sessionID: Int = call.parameters["sessionID"]!!.toInt()
                val deleted: Boolean = db.deleteGroupSession(sessionID)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Group session deleted"))
                } else if (!deleted) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Group session not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }
    }
}

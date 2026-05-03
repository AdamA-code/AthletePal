package routes

import com.example.module
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import services.AuthService
import services.DatabaseService
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import services.DatabaseService.GroupSession as GroupSession
import services.DatabaseService.TrainingGroup as TrainingGroup
import services.DatabaseService.Workout as Workout
import services.DatabaseService.WorkoutExercise as WorkoutExercise

class SocialRoutesTest {
    // ====================== PRE-TEST SETUP ============================

    private fun ApplicationTestBuilder.setup() {
        application { module() } // Install Ktor module, such that SocialRoutesTest.kt can be accessed
    }

    var userID: Int? = null
    var addToGroupUserID: Int? = null
    var loggedWorkoutID: Int? = null
    var groupID: Int? = null
    var upcomingGroupSessionID: Int? = null
    var pastGroupSessionID: Int? = null

    @BeforeTest
    fun setup() =
        testApplication {
            application { module() }

            // Create two test users for use in tests
            userID = AuthService().registerUser("TEST1", "TEST1@TEST.COM", "wordpass", false)
            addToGroupUserID = AuthService().registerUser("TEST2", "TEST2@TEST.COM", "wordpass", false)

            // Create a test workout, containing two workoutExercises, for use in tests
            val workout =
                Workout(
                    createdBy = userID!!,
                    assignedTo = userID!!,
                    date = "2026-01-01",
                    notes = "TEST WORKOUT",
                )
            val workoutExercise =
                WorkoutExercise(
                    exerciseID = 2,
                    sets = 3,
                    reps = 15,
                    weight = 15.5,
                )
            val exercises: List<WorkoutExercise> = listOf(workoutExercise, workoutExercise)
            loggedWorkoutID = DatabaseService().logWorkout(workout, exercises)

            // Create a test group for use in tests
            val trainingGroup =
                TrainingGroup(
                    createdBy = userID!!,
                    name = "Test Group",
                    description = "Test Description",
                    goal = "Test Goal",
                )
            groupID = DatabaseService().createGroup(trainingGroup)

            // Create a upcoming test group training session
            val upcominigGroupSession =
                GroupSession(
                    groupID = groupID!!,
                    workoutID = loggedWorkoutID!!,
                    scheduledDate = "2099-01-01",
                ) // Date in future from expected test script execution
            upcomingGroupSessionID = DatabaseService().addGroupSession(upcominigGroupSession)

            // Create a past test group training session
            val pastGroupSession =
                GroupSession(
                    groupID = groupID!!,
                    workoutID = loggedWorkoutID!!,
                    scheduledDate = "2026-01-01",
                ) // Date in past from expected test script execution
            pastGroupSessionID = DatabaseService().addGroupSession(pastGroupSession)
        }

    // ==================== TRAINING GROUP CRUD TESTS =====================

    // ============================
    // CREATE GROUP ENDPOINT TESTS
    // ============================

    @Test
    fun createGroup_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request as all required (and optional fields) passed in correct format so can be serialised
            val response =
                client.post("/social/group") {
                    contentType(ContentType.Application.Json) // Declare that body is of content type JSON (for serialisation)
                    setBody(
                        """
                        {
                            "createdBy": ${userID!!},
                            "name": "Valid Group Test",
                            "description": "Valid Group Test Description",
                            "goal": "Valid Group Test Goal"
                        }
                        """.trimIndent(),
                    ) // All required fields (createdBy and name) passed in correct data type, as well as optional fields (description and goal)
                }

            // Expect OK response code, as group can be created with all data provided in HTTP request body
            assertEquals(HttpStatusCode.OK, response.status)

            // Get ID of newly created test group
            val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val testGroupID = jsonResponse["ID"]?.jsonPrimitive?.content?.toInt()

            // Delete group created in test, so as to not have impact on real db
            DatabaseService().deleteGroup(testGroupID!!)
        }

    @Test
    fun createGroup_missingRequiredFields() =
        testApplication {
            application { module() }

            // Invalid HTTP request, as required fields are missing preventing JSON from being serialised
            val response =
                client.post("/social/group") {
                    contentType(ContentType.Application.Json) // Declare content type of body as JSON (for serialisation)
                    setBody(
                        """
                        {
                            "description": "Missing Fields Group Description",
                            "goal": "Missing Fields Group Goal"
                        }
                        """.trimIndent(),
                    ) // Body is missing 'createdBy' and 'name' fields (both required)
                }

            // Expect BadRequest status code, as request cannot be dealt with unless al required fields are given
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun createGroup_missingOptionalFields() =
        testApplication {
            application { module() }

            // Valid HTTP request as all required fields are given, allowing serialisation to be performed and backend to handle request
            val response =
                client.post("/social/group") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "createdBy": ${userID!!},
                            "name": "Only Required Fields Group Name"
                        }
                        """.trimIndent(),
                    ) // Optional fields ('description' and 'goal') omitted from HTTP request body
                }

            // Expect OK status code as backend logic can create group with only required fields given
            assertEquals(HttpStatusCode.OK, response.status)

            // Get groupID of the newly created test group
            val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val testGroupID = jsonResponse["ID"]?.jsonPrimitive?.content?.toInt()

            // Delete group created in test, so as to not have an impact on real db
            DatabaseService().deleteGroup(testGroupID!!)
        }

    @Test
    fun createGroup_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request, as body of request not in correct structure/form so cannot be serialised
            val response =
                client.post("/social/group") {
                    contentType(ContentType.Application.Json)
                    setBody(" BAD JSON BODY ")
                }

            // Expect BadRequest status code, as exception is met in backend when body fails to be serialised
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // ============================
    // READ GROUP ENDPOINT TESTS
    // ============================

    @Test
    fun getSingleGroup_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request, as we use correct endpoint
            val response = client.get("/social/group/${groupID!!}")

            // Expect OK status code as groupID exists, so response given
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun getSingleGroup_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request, as 999999999999 is in correct userID form, but logically doesn't exist in db
            val response = client.get("/social/group/999999999999")

            // Expect BadRequest status code, as target group doesn't exist
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun getSingleGroup_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request, as String can't be serialised to an Integer
            val response = client.get("/social/group/abcdef")

            // Expect BadRequest status code, as exception met upon serialisation attempt of string into integer
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun getAllUsersGroups_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request, as we give valid userID as parameter to search groups for
            val response = client.get("/social/groups/${userID!!}")

            // Expect OK status code, as userID user is in a group (defined in test setup)
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun getAllUsersGroups_noUserGroups() =
        testApplication {
            application { module() }

            // Valid HTTP request, as we give a valid userID to search groups for
            val response = client.get("/social/groups/${addToGroupUserID!!}")

            // Expect NotFound status code, as we know addToGroupUserID user isn't in a group
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun getAllUsersGroups_invalidUserID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request, as userID in correct form, but logically doesn't exist in db
            val response = client.get("/social/groups/999999999999")

            // Expect BadRequest status code, as user account cannot be identified due to userID not correlating to an account
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun getAllUsersGroups_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request as string cannot be serialised into an integer
            val response = client.get("/social/groups/abcdef")

            // Expect BadRequest status code, as exception met when trying to serialise string into integer
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun getAllGroupMembers_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request as groupID given as parameter is valid and can be searched with
            val response = client.get("/social/group/members/${groupID!!}")

            // Expect OK status code, as groupID exists as a group, and contains >= 1 member
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun getAllGroupMembers_noMembers() =
        testApplication {
            application { module() }

            // Create temporary group, and remove creator (the only default member) for test
            val testGroupID = DatabaseService().createGroup(TrainingGroup(createdBy = userID!!, name = "NO MEMBERS TEST"))
            DatabaseService().leaveGroup(testGroupID!!, userID!!)

            // Valid HTTP request as groupID given as parameter is valid, but no members are in corresponding group
            val response = client.get("/social/group/members/${testGroupID!!}")

            // Expect NotFound status code, as group exists yet no members can be found as a part of the group
            assertEquals(HttpStatusCode.NotFound, response.status)

            // Delete the temporary group made earlier in test
            DatabaseService().deleteGroup(testGroupID!!)
        }

    @Test
    fun getAllGroupMembers_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request, as groupID is in correct form
            val response = client.get("/social/group/members/999999999999")

            // Expect BadRequest as 999999999999 does not correspond to a group, so is rejected
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun getAllGroupMembers_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request, as groupID is a string which cannot be serialised into an integer
            val response = client.get("/social/group/members/abcdef")

            // Expect BadRequest as we hit an exception after failing to serialise String into Integer
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // ============================
    // UPDATE GROUP ENDPOINT TESTS
    // ============================

    @Test
    fun updateGroup_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request, as we provide a valid groupID and all required fields in the HTTP request body
            val response =
                client.put("/social/group/${groupID!!}") {
                    contentType(ContentType.Application.Json) // Declare that body of HTTP request is in JSON (for serialisation)
                    setBody(
                        """
                        {
                            "name": "Test Group Updated Name",
                            "description": "Test Group Updated Description",
                            "goal": "Test Group Updated Goal"
                        }
                        """.trimIndent(),
                    ) // Required field (name) is given in HTTP body, in correct string data type
                }

            // Expect OK status code, as we provide all values in correct format, and groupID correlates to group (allowing group data update)
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun updateGroup_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request, as all required values (name and groupID) passed as correct data types
            val response =
                client.put("/social/group/999999999999") {
                    contentType(ContentType.Application.Json) // Declare body of request is in JSON (for serialisation)
                    setBody(
                        """
                        {
                            "name": "Invalid GroupID Name Update",
                            "description": "Invalid GroupID Description Update"
                            "goal": "Invalid GroupID Goal Update"
                        }
                        """.trimIndent(),
                    ) // Required field (name) in body provided as correct data type (String)
                }

            // Expect BadRequest status code as cannot identify group based of ID (as 999999999999 not valid)
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun updateGroup_missingOptionalFields() =
        testApplication {
            application { module() }

            // Valid HTTP request, as we provide all required fields (name and groupID)
            val response =
                client.put("/social/group/${groupID!!}") { // Valid groupID passed as parameter
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "name": "Updated Workout Name"
                        }
                        """.trimIndent(),
                    ) // Only passed required field in body (name) and not optional (description and goal)
                }

            // Expect OK status code, as group can be identified and updated with only groupID and name
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun updateGroup_missingRequiredField() =
        testApplication {
            application { module() }

            // Invalid HTTP request, as no value passed for the name (a required field in body)
            val response =
                client.put("/social/group/${groupID!!}") { // Group can be identified with valid groupID parameter
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "description": "Optional field (description)",
                            "goal": "Optional field (goal)"
                        }
                        """.trimIndent(),
                    ) // Only optional fields (description and goal) given in body
                }

            // Expect BadRequest status code, as request cannot be executed without all required fields (i.e. name is missing)
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun updateGroup_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request as body and groupID parameter is not in correct format or data type, and cannot be serialised
            val response =
                client.put("/social/group/abcdef") { // groupID is string instead of integer so cannot be serialised
                    contentType(ContentType.Application.Json)
                    setBody(" BAD JSON BODY ") // HTTP request body not in correct format so cannot be serialised
                }

            // Expect BadRequest status code as exception met when route tries to serialise HTTP request
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun addGroupMember_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request as groupID and addToGroupUserID are valid and passed as parameters
            val response = client.put("/social/group/${groupID!!}/${addToGroupUserID!!}")

            // Expect OK status code as all required values are passed, allowing user to be added to group
            assertEquals(HttpStatusCode.OK, response.status)

            // Remove temporary user 'addToGroupUserID' from group 'groupID' for other tests
            DatabaseService().leaveGroup(groupID!!, addToGroupUserID!!)
        }

    @Test
    fun addGroupMember_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as groupID is passed as integer so can be serialised still
            val response = client.put("/social/group/99999999999/${addToGroupUserID!!}")

            // Expect BadRequest status code as groupID cannot identify a group to add user to so is rejected
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun addGroupMember_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request, as both groupID and userID are both passed as strings so cannot be serialised into integers
            val response = client.put("/social/group/abc/def") // Bad JSON used for parameters

            // Expect BadRequest status code, as exception met when trying to serialise groupID and userID from string to integer
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // ============================
    // DELETE GROUP ENDPOINT TESTS
    // ============================

    @Test
    fun removeUserFromGroup_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request as groupID and userID both passed as parameters as correct data type
            val response = client.delete("/social/group/${groupID!!}/${userID!!}")

            // Expect OK status code, as user exists in group and so can be deleted (i.e. removed from group)
            assertEquals(HttpStatusCode.OK, response.status)

            // Add user back to group, for use in any remaining tests
            DatabaseService().joinGroup(groupID!!, userID!!)
        }

    @Test
    fun removeUserFromGroup_userNotInGroup() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as userID and groupID both passed as parametes as correct data type
            val response = client.delete("/social/group/${groupID!!}/${addToGroupUserID!!}")

            // Expect NotFound status code, as addToGroupUserID doesn't correspond with a user in group
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun removeUserFromGroup_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as userID and groupID are both passed as Integer paramters (correct data type)
            val response =
                client.delete(
                    "/social/group/9999999999999/${addToGroupUserID!!}",
                ) // Assume groupID 9999999999999 is not valid group

            // Expect BadRequest status code, as groupID 9999999999999 doesn't correspond with a group
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun removeUserFromGroup_invalidUserID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as userID is still passed as an integer (correct data type)
            val response = client.delete("/social/group/${groupID!!}/99999999999999999")

            // Expect BadRequest status code, as userID 999999999999999999 doesn't correspond to a valid user
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun removeUserFromGroup_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request as both userID and groupID are passed as strings (incorrect data type)
            val response = client.delete("/social/group/abc/def")

            // Expect BadRequest status code, as hit an exception as string cannot be serialised into integer due to incorrect JSON
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun deleteGroup_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request as groupID is given as parameter of correct data type (Integer)
            val response = client.delete("/social/group/${groupID!!}")

            // Expect OK status code as backend is given all parameters (groupID) needed to execute request (delete group)
            assertEquals(HttpStatusCode.OK, response.status)

            // Recreate deleted training group for use in other tests
            groupID =
                DatabaseService().createGroup(
                    TrainingGroup(
                        createdBy = userID!!,
                        name = "Test Group",
                        description = "Test Description",
                        goal = "Test Goal",
                    ),
                )
        }

    @Test
    fun deleteGroup_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as groupID is still of integer type so can be serialised
            val response = client.delete("/social/group/99999999999999") // Expect 999999999999999 not a valid groupID

            // Expect BadRequest status code as groupID doesn't correspond to a group and hence cant be deleted
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun deleteGroup_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request as parameters (groupID) of request is in incorrect JSON format so cant be serialised
            val response = client.delete("/social/group/abcdef")

            // Expect BadRequest status code, as exception is met when trying to serialise a string into integer data type
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // ==================== GROUP SESSION CRUD TESTS ======================

    // ==============================
    // CREATE SESSION ENDPOINT TESTS
    // ==============================

    @Test
    fun createGroupSession_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request as all required and optional fields are given in body, in correct format and data type
            val response =
                client.post("/social/session") {
                    contentType(ContentType.Application.Json) // Declare that body of request has content type of JSON (for serialisation)
                    setBody(
                        """
                        {
                            "groupID": ${groupID!!},
                            "workoutID": ${loggedWorkoutID!!},
                            "scheduledDate": "2026-01-01"
                        }
                        """.trimIndent(),
                    )
                }

            // Expect OK status code as we provide all fields required for backend to create new group session
            assertEquals(HttpStatusCode.OK, response.status)

            // Get ID corresponding to newly created test group session
            val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val testGroupSessionID = jsonResponse["ID"]?.jsonPrimitive?.content?.toInt()

            // Delete newly made test group session, so as to now have lasting effect on real db
            DatabaseService().deleteGroupSession(testGroupSessionID!!)
        }

    @Test
    fun createGroupSession_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as all required fields (and optional) are given in correct data types, allowing for serialisation
            val response =
                client.post("/social/session") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "groupID": 99394949994,
                            "workoutID": ${loggedWorkoutID!!},
                            "scheduledDate": "2026-01-01"
                        }
                        """.trimIndent(),
                    ) // Expect groupID is not valid (not actual group in db)
                }

            // Expect BadRequest status code, as groupID does not correspond to an actual group
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun createGroupSession_invalidWorkoutID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as all required fields are still given in correct type and format
            val response =
                client.post("/social/session") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "groupID": ${groupID!!},
                            "workoutID": 99999999999999,
                            "scheduledDate": "2026-01-01"
                        }
                        """.trimIndent(),
                    ) // Expect given workoutID is not valid (not actual workout in db)
                }

            // Expect BadRequest status code, as workoutID doesn't correspond to actual workout
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun createGroupSession_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request, as body of request does not follow correct format and so can't be serialised
            val response =
                client.post("/social/session") {
                    contentType(ContentType.Application.Json)
                    setBody(" BAD JSON BODY ")
                }

            // Expect BadRequest status code, as exception is met when trying to serialise a bad JSON body
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // ============================
    // READ SESSION ENDPOINT TESTS
    // ============================

    @Test
    fun getUpcomingGroupSessions_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request as all parameters required for backend to perform operation are given in correct format
            val response = client.get("/social/sessions/upcoming/${groupID!!}")

            // Expect that we recieve a upcoming session (created in setup) so expect OK status code
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun getUpcomingGroupSessions_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as groupID parameter given in correct format
            val response = client.get("/social/sessions/upcoming/999999999999999")

            // Expect that no upcoming sessions are found as group doesn't exist, so expect BadRequest status code
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun getUpcomingGroupSessions_noUpcomingSessions() =
        testApplication {
            application { module() }

            // Create temporary new test group which has no upcoming sesssions
            val tempTestGroupID = DatabaseService().createGroup(TrainingGroup(createdBy = userID!!, name = "Test Group"))

            // Technically valid HTTP request as groupID parameter is in correct format so can be serialised to integer
            val response = client.get("/social/sessions/upcoming/${tempTestGroupID!!}")

            // Assume no upcoming sessions are found, and hence expect NotFound status code
            assertEquals(HttpStatusCode.NotFound, response.status)

            // Delete temporary test group, so as to not have impact or change the real db
            DatabaseService().deleteGroup(tempTestGroupID!!)
        }

    @Test
    fun getUpcomingGroupSessions_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request as groupID is passed as a string so cannot be serialised due to bad JSON
            val response = client.get("/social/sessions/upcoming/abc")

            // Expect BadRequest, as exception is met when trying to serialise bad JSON
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun getPastGroupSessions_valid() =
        testApplication {
            application { module() }

            // Valid HTTP request as we pass groupID as a parameter in correct format so can be serialised
            val response = client.get("/social/sessions/past/${groupID!!}")

            // Expect we found past group sessions (defined in setup) and hence expect OK status code
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun getPastGroupSessions_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request as groupID is passed as parameter in correct format, allowing serialisation
            val response = client.get("/social/sessions/past/99999999999999") // Assume 99999999999999 not valid groupID

            // groupID passed as paramter expected not valid, hence expect no past sessions found and BadRequest status code returned
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun getPastGroupSessions_noPastSessions() =
        testApplication {
            application { module() }

            // Create a new temporary group which has no associated past sessions, for test
            val tempTestGroupID = DatabaseService().createGroup(TrainingGroup(createdBy = userID!!, name = "Test Group"))

            // Valid HTTP request as we give groupID as a parameter in the correct format, allowing serialisation
            val response = client.get("/social/sessions/past/${tempTestGroupID!!}")

            // Expect no past sessions found (as group just created), hence expect NotFound status code
            assertEquals(HttpStatusCode.NotFound, response.status)

            // Delete temporary group, so as to not have lasting affect on real db
            DatabaseService().deleteGroup(tempTestGroupID!!)
        }

    @Test
    fun getPastGroupSessions_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request as data type of groupID passed as parameter is string and hence cant be serialised into integer
            val response = client.get("/social/sessions/past/abc")

            // groupID is given as a string and hence an exception is hit when trying to serialise into integer, thus expect BadRequest status code
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // ==============================
    // DELETE SESSION ENDPOINT TESTS
    // ==============================

    @Test
    fun deleteGroupSession_valid() =
        testApplication {
            application { module() }

            // Create temporary test group session for use in test
            val tempTestSessionID =
                DatabaseService().addGroupSession(
                    GroupSession(groupID = groupID!!, workoutID = loggedWorkoutID!!, scheduledDate = "2027-01-01"),
                )

            // Valid HTTP request, as we pass sessionID as a parameter of the correct data type
            val response = client.delete("/social/session/${tempTestSessionID!!}")

            // As we have given backend all required parameters for deleting the session, expect OK status code
            assertEquals(HttpStatusCode.OK, response.status)

            // Incase test fails, try deleting temporary test group session so no impact on real db
            try {
                DatabaseService().deleteGroupSession(tempTestSessionID!!)
            } catch (e: Exception) {
            }
        }

    @Test
    fun deleteGroupSession_invalidGroupID() =
        testApplication {
            application { module() }

            // Technically valid HTTP request, as groupID is still passed as parameter of correct data type
            val response = client.delete("/social/session/99999999999999") // Assume 99999999999999 is not valid sessionID

            // Expect that there is no group session under given sessionID, so backend cant find session to delete so returns BadRequest status code
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    @Test
    fun deleteGroupSession_badJSON() =
        testApplication {
            application { module() }

            // Invalid HTTP request, as groupID is given as a string which cannot be serialised into an integer
            val response = client.delete("/social/session/abc")

            // Expect BadRequest status code, as we hit an exception when we try serialising a string into a integer due to bad JSON
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // ====================== POST-TEST CLEANUP ============================

    @AfterTest
    fun cleanup() {
        // Delete test users after their use in tests
        DatabaseService().deleteUser("TEST1@TEST.COM")
        DatabaseService().deleteUser("TEST2@TEST.COM")

        // Delete test workout and both of its associated exercises
        DatabaseService().deleteAllExercisesInWorkout(loggedWorkoutID!!)
        DatabaseService().deleteWorkout(loggedWorkoutID!!)

        // Delete test training group
        DatabaseService().deleteGroup(groupID!!)

        // Delete both upcoming and past group sessions
        DatabaseService().deleteGroupSession(upcomingGroupSessionID!!)
        DatabaseService().deleteGroupSession(pastGroupSessionID!!)
    }
}

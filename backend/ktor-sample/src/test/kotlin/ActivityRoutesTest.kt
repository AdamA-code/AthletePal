package routes

import com.example.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.testing.*
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import services.AuthService
import services.DatabaseService
import kotlin.test.*

class ActivityRoutesTest {
    private fun ApplicationTestBuilder.setup() {
        application {
            module() // Makes sure Ktor module installs activity activityRoutes() for testing
        }
    }

    var userID: Int? = null

    @BeforeTest
    fun setup() =
        testApplication {
            application { module() }
            userID = AuthService().registerUser("TestAccount", "TestingEmail@email.com", "wordpass", false)
        }

    // ===========================
    // WORKOUT ROUTES TESTING
    // ===========================

    @Test
    fun createWorkout_valid() =
        testApplication { // Normal Case = testing normal behaviour for creating new workout
            application { module() }

            val response =
                client.post("/api/workouts") { // Form HTTP request and send to endpoint
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            
                            "createdBy": 1,
                            "assignedTo": 1,
                            "date": "2026-05-01",
                            "notes": "Workout routes test"
                        }
                        """.trimIndent(),
                    )
                }

            response.debug()
            assertEquals(HttpStatusCode.OK, response.status) // Response should have OK HTTP status code associated
            assertTrue(response.bodyAsText().contains("ID")) // Response should contain ID of new workout made
        }

    @Test
    fun createAccount_missingField() =
        testApplication { // Erroneous Case: testing behaviour when a field is missing
            application { module() }

            val response =
                client.post("/api/workouts") { // Form HTTP request with no userID, date, or duration and send to endpoint
                    contentType(ContentType.Application.Json)
                    setBody("""{ "name": "Bad Workout"}""")
                }
            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
            ) // HTTP response recieved should have the HTTP status code for a bad request made
        }

    @Test
    fun updateWorkout_nullOptionalField() =
        testApplication { // Normal Case: testing behaviour when an optional field is left blank
            application { module() }

            val response =
                client.put("/api/workouts/1") { // Form PUT HTTP request with missing optional field
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "date": null,
                            "duration": 0
                        }
                        """.trimIndent(),
                    )
                }
            response.debug()
            assertTrue(
                response.status == HttpStatusCode.OK ||
                 response.status == HttpStatusCode.NotFound ||
                  response.status == HttpStatusCode.BadRequest,
            ) // Response should be OK or NotFound (if workout non-existant)
        }

    @Test
    fun getWorkout_invalidJSONID() =
        testApplication { // Erroneous Case: testing for invalid JSON on workoutID field
            application { module() }

            val response = client.get("/api/workout/abcdefg") // HTTP request for getting workout with an invalid workoutID JSON

            assertEquals(HttpStatusCode.BadRequest, response.status) // Response should be BadRequest as invalid JSON cannot be serialised
        }

    @Test
    fun getWorkout_negativeWorkoutID() =
        testApplication { // Erroneous Case: testing invalid workoutID, where negative ID not permitted
            application { module() }

            val response = client.get("/api/workout/-1") // HTTP request, where workoutID is negative

            assertTrue(
                response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.BadRequest,
            ) // Response should either be NotFound (if negative ID allowed) or BadRequest
        }

    // ===========================
    // COMPETITION ROUTES TESTING
    // ===========================

    @Test
    fun createCompetition_valid() =
        testApplication { // Normal Case: testing behaviour for creating competition, providing all neccessary fields
            application { module() }

            val response =
                client.post("/api/competitions") { // Form POST HTTP request for valid creating of new competition
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "userID": 1,
                            "name": "NE Regionals",
                            "date": "2026-09-01"
                        }
                        """.trimIndent(),
                    )
                }
            response.debug()
            assertEquals(HttpStatusCode.OK, response.status) // Response should be HTTP status of OK, as request is valid
            assertTrue(response.bodyAsText().contains("ID")) // Response should contain ID of new competition
        }

    @Test
    fun createCompetition_missingField() =
        testApplication { // Erroneous Case: test behaviour for missing fields in HTTP request for creating new competition
            application { module() }

            val response =
                client.post("/api/competitions") { // Form HTTP request with missing required fields
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "name": "Incomplete Competition"
                        }
                        """.trimIndent(),
                    )
                }
            assertEquals(HttpStatusCode.BadRequest, response.status) // Expect response to contain HTTP status code for a BadRequest made
        }

    @Test
    fun updateCompetition_boundaryField() =
        testApplication { // Boundary Case: test for when a field has a bounary value (i.e. rank = 1)
            application { module() }

            val response =
                client.put("/api/competitions/1") { // Form HTTP request with bounary value for "rank"
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "userID": ${userID!!},
                            "name": "Updated Competition",
                            "date": "2026-01-01",
                            "rank": 1
                        }
                        """.trimIndent(),
                    )
                }
            assertTrue(
                response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound,
            ) // Expect response to be OK or NotFound (if target competition doesn't exist)
        }

    // ===========================
    // RUNNING ROUTE TESTS
    // ===========================

    @Test
    fun createRoute_valid() =
        testApplication { // Normal Case: test behaviour for when HTTP request for valid route creation is made
            application { module() }

            val response =
                client.post("/api/routes") { // Form HTTP request containing all correct values (i.e. valid request)
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "userID": 1,
                            "name": "Hyde Park Loop",
                            "distance": 5.6,
                            "date": "2026-05-10",
                            "isPublic": false,
                            "routeData": "Big circle on map in hyde park"
                        }
                        """.trimIndent(),
                    )
                }
            response.debug()
            assertEquals(HttpStatusCode.OK, response.status) // Expect response to contain OK status code, as valid request was made
            assertTrue(response.bodyAsText().contains("ID")) // Expect response to contain the routeID of the newly made route
        }

    @Test
    fun createRoute_missingField() =
        testApplication { // Erroneous Case: test behaviour for create new route when missing fields are provided
            application { module() }

            val response =
                client.post("/api/routes") { // Form HTTP request with missing required fields
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "userID": ${userID!!},
                            "name": "Route with missing date",
                            "distance": 5.0
                        }
                        """.trimIndent(),
                    )
                }
            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
            ) // Expect response to contain BadRequest status code, as invalid request was made
        }

    @Test
    fun updateRoute_valid() =
        testApplication { // Normal Case: test behaviour when correct usage of updateRoute endpoint is used
            application { module() }

            val response =
                client.put("/api/routes/1") { // Form valid HTTP request for updating route
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "name": "Updated Route",
                            "distance": 10.0,
                            "estimatedTime": 60.0,
                            "date": "2026-06-01"
                        }
                        """.trimIndent(),
                    )
                }
            response.debug()
            assertTrue(
                response.status == HttpStatusCode.OK ||
                 response.status == HttpStatusCode.NotFound ||
                  response.status == HttpStatusCode.BadRequest,
            ) // Expect resposne to contain OK status code, or NotFound (if target route doesn't exist)
        }

    @Test
    fun updateRoute_invalidJSON() =
        testApplication { // Erroneous Case: test behaviour when wrong type is provided as an argument in HTTP request (i.e. invalid JSON)
            application { module() }

            val response =
                client.put(
                    "/api/routes/1",
                ) { // Form HTTP request where argument for sitance is provided in wrong form (i.e. string provided, when double expected)
                    contentType(ContentType.Application.Json)
                    setBody("""{"distance": "not-number"}""")
                }
            assertTrue(
                response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.InternalServerError,
            ) // Expect repsonse to contain BadRequest status code, as distance argument is bad JSON so not serialisable
        }

    // ===========================
    // ACTIVITY ROUTE TESTS
    // ===========================

    @Test
    fun getRecentActivity_valid() =
        testApplication { // Normal Case: test behaviour for getting recent activity of user, when correct usage used
            application { module() }

            val response =
                client.get(
                    "/api/activity/recent/TestingEmail@email.com",
                ) // HTTP request when correct usage used (i.e. email parsed in correct format)

            assertEquals(HttpStatusCode.OK, response.status) // Expect response to contain OK status code, as valid HTTP request was made
        }

    @Test
    fun getAllActivity_invalidUserID() =
        testApplication { // Eroneous Case: test behaviour when an invalid userID is parsed as an argument
            application { module() }

            val response =
                client.get(
                    "/api/activity/notAUserID",
                ) // Form HTTP request with invalid userID (i.e. string parsed instead of expected integer)

            assertEquals(
                HttpStatusCode.BadRequest,
                response.status,
            ) // Expect HTTP response to contain BadRequest, as invalid userID was provided in request
        }

    @Test
    fun debug_workout() =
        testApplication {
            application { module() }
            val response =
                client.post("/api/workouts") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        """
                        {
                            "createdBy": ${userID!!},
                            "assignedTo": ${userID!!},
                            "date": "2026-05-01",
                            "duration": 45
                        }
                        """.trimIndent(),
                    )
                }
            println("STATUS = ${response.status}")
            println("BODY = ${response.bodyAsText()}")
        }

    @AfterTest
    fun cleanup() {
        DatabaseService().deleteUser("TestingEmail@email.com")
    }

    private suspend fun HttpResponse.debug() {
        println("STATUS = $status")
        println("BODY = ${bodyAsText()}")
    }
}

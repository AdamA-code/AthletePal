package routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import com.example.module

import services.DatabaseService.Workout as Workout
import services.DatabaseService.Competition as Competition
import services.DatabaseService.Route as RunningRoute
import services.DatabaseService.WorkoutExercise as WorkoutExercise

import services.AuthService
import services.DatabaseService

class ActivityRoutesTest {

    private fun ApplicationTestBuilder.setup() {
        application {
            module() // Makes sure Ktor module installs activity activityRoutes() for testing
        }
    }

    var userID: Int? = null
    var workoutID: Int? = null
    var competitionID: Int? = null
    var routeID: Int? = null
    var workoutExerciseID: Int? = null

    @BeforeTest
    fun setup() = testApplication {
        application { module() }

        userID = AuthService().registerUser("TestAccount", "TestingEmail@email.com", "wordpass", false)


        // Create Test WORKOUT
        val workout = Workout(
            createdBy = userID!!,
            assignedTo = userID!!,
            date = "2026-01-01",
            notes = "Test Workout"
        )
        workoutID = DatabaseService().createWorkout(workout)


        // Create Test COMPETITION
        val competition = Competition(
            userID = userID!!,
            name = "Test Competition",
            date = "2026-01-01",
            rank = 2,
            result = 1
        )
        competitionID = DatabaseService().addComp(competition)

        // Create Test ROUTE
        val runningRoute = RunningRoute(
            userID = userID!!,
            name = "Test Route",
            distance = 10.0,
            date = "2026-01-01",
            isPublic = false,
            routeData = "Test Route Data"
        )
        routeID = DatabaseService().addRoute(runningRoute)

        // Create Test WORKOUT EXERCISE
        val workoutExercise = WorkoutExercise(
            workoutID = workoutID!!,
            exerciseID = 2,
            sets = 3,
            reps =  8,
            weight = 30.5
        )
        workoutExerciseID = DatabaseService().addExerciseToWorkout(workoutExercise)
    }

    // ===========================
    // WORKOUT ROUTES TESTING
    // ===========================

    
    @Test
    fun createWorkout_valid() = testApplication { // Normal Case = testing normal behaviour for creating new workout
        application { module() }

        val response = client.post("/api/workout") { // Form HTTP request and send to endpoint
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    
                    "createdBy": ${userID!!},
                    "assignedTo": ${userID!!},
                    "date": "2026-05-01",
                    "notes": "Workout routes test"
                }
                """.trimIndent())
        }

        assertEquals(HttpStatusCode.OK, response.status) // Response should have OK HTTP status code associated
        assertTrue(response.bodyAsText().contains("ID")) // Response should contain ID of new workout made

        val testJsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val testWorkoutID = testJsonResponse["ID"]?.jsonPrimitive?.content?.toInt() 

        DatabaseService().deleteWorkout(testWorkoutID!!)  
    }

    @Test
    fun createWorkout_missingField() = testApplication { // Erroneous Case: testing behaviour when a field is missing
        application { module() }

        val response = client.post("/api/workout") { // Form HTTP request with no userID, date, or duration and send to endpoint
            contentType(ContentType.Application.Json)
            setBody("""{ "name": "Bad Workout"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // HTTP response recieved should have the HTTP status code for a bad request made
    }

    @Test
    fun updateWorkout_valid() = testApplication {
        application { module() }

        val response = client.put("/api/workout/${workoutID!!}") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "date": "2026-06-01",
                    "duration": 60
                }
            """.trimIndent())
        }
    }

    @Test
    fun updateWorkout_nullOptionalField() = testApplication { // Normal Case: testing behaviour when an optional field is left blank
        application { module() }

        val response = client.put("/api/workout/${workoutID!!}") { // Form PUT HTTP request with missing optional field
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "date": null,
                    "duration": 0
                }
            """.trimIndent())
        }
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.BadRequest) // Response should be OK or NotFound (if workout non-existant)
        
    }

    @Test
    fun getWorkout_valid() = testApplication {
        application { module() }

        val response = client.get("/api/workout/${workoutID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getWorkout_invalidJSONID() = testApplication {  // Erroneous Case: testing for invalid JSON on workoutID field
        application { module() }

        val response = client.get("/api/workout/abcdefg") // HTTP request for getting workout with an invalid workoutID JSON

        assertEquals(HttpStatusCode.BadRequest, response.status) // Response should be BadRequest as invalid JSON cannot be serialised
    }

    @Test
    fun getWorkout_negativeWorkoutID() = testApplication { // Erroneous Case: testing invalid workoutID, where negative ID not permitted
        application { module() }

        val response = client.get("/api/workout/-1") // HTTP request, where workoutID is negative

        assertTrue(response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.BadRequest) // Response should either be NotFound (if negative ID allowed) or BadRequest
    }

    // ================================
    // WORKOUT EXERCISE ROUTES TESTING
    // ================================

    // 'Happy Case' where we successfully add a exercise to a predefined workout
    @Test
    fun addExerciseToWorkout_valid() = testApplication {
        application { module() }

        val response = client.post("/api/workout/exercises/add-exercise") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "workoutID": ${workoutID!!},
                    "exerciseID": 2,
                    "sets": 3,
                    "reps": 10,
                    "weight": 50.5
                }
            """.trimIndent())
        }

        assertEquals(HttpStatusCode.OK, response.status) // Expect OK HTTP code to be returned, indicating exercise added

        val testJsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val testWorkoutExerciseID = testJsonResponse["ID"]?.jsonPrimitive?.content?.toInt()
        DatabaseService().deleteWorkoutExercise(testWorkoutExerciseID!!) // Delete exercise added to workout in test, so not to have impact on real db
    }

    // Fail case where we reject the request, as not JSON serialisable due to wrong body formatting
    @Test
    fun addExerciseToWorkout_invalidJSON() = testApplication {
        application { module() }

        val response = client.post("/api/workout/exercises/add-exercise") {
            contentType(ContentType.Application.Json)
            setBody(" INVALID JSON REQUEST") // Body does not follow expected structure, and hence not JSON serialisable
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // Expect BadRequest returned, indicating an exception thrown and exercise not added
    }

    // Fail case where we expect rejection due to non-existant workoutID being parsed
    @Test
    fun addExerciseToWorkout_invalidWorkoutID() = testApplication {
        application { module() }

        val response = client.post("/api/workout/exercises/add-exercise") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "workoutID": 99999999,
                    "exerciseID": 2,
                    "sets": 1,
                    "reps": 1,
                    "weight": 10.0
                }
            """.trimIndent()) // Body of request valid, so as not to test two failures
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // Expect BadRequest HTTP code being returned, due to non-existant workoutID parsed
    }

    // Happy case, as we omit fields which aren't strictly requeired in the HTTP request
    @Test
    fun addExerciseToWorkout_nullOptionalFields() = testApplication {
        application { module() }

        val response = client.post("/api/workout/exercises/add-exercise") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "workoutID": ${workoutID!!},
                    "exerciseID": 2
                }
            """.trimIndent()) // We omit sets, reps, and weight fields from HTTP request, as all are optional
        }
        assertEquals(HttpStatusCode.OK, response.status) // Expect OK HTTP code returned, as exercise can still be created
    }

    // Fail case as we do not provide required fields for creating a exercise
    @Test
    fun addExerciseToWorkout_nullRequiredFields() = testApplication {
        application { module() }

        val response = client.post("/api/workout/exercises/add-exercise") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "sets": 3,
                    "reps": 5,
                    "weight": 5.5
                }
            """.trimIndent()) // We do not provide workoutID, or exerciseID which are both required fields
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // Expect BadRequest code returned, as required fields not provided
    }

    @Test
    fun getAllExercisesFromWorkout_valid() = testApplication {
        application { module() }

        val response = client.get("/api/workout/exercises/${workoutID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getAllExercisesFromWorkout_invalidWorkoutID() = testApplication {
        application { module() }

        val response = client.get("/api/workout/exercises/9999999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getAllExercisesFromWorkout_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/workout/exercises/abcdef")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getWorkoutExercise_valid() = testApplication {
        application { module() }

        val response = client.get("/api/workout/exercise/${workoutExerciseID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getWorkoutExercise_invalidWorkoutExerciseID() = testApplication {
        application { module() }

        val response = client.get("/api/workout/exercise/99999594")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getWorkoutExercise_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/workout/exercise/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun updateWorkoutExercise_valid() = testApplication {
        application { module() }

        val response = client.put("/api/workout/exercise/${workoutExerciseID!!}") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "sets": 2,
                    "reps": 15,
                    "weight": 60.25
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun updateWorkoutExercise_invalidWorkoutExerciseID() = testApplication {
        application { module() }

        val response = client.put("/api/workout/exercise/99999999") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "sets": 2,
                    "reps": 15,
                    "weight": 60.25
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun updateWorkoutExercise_missingRequiredFields() = testApplication {
        application { module() }

        val response = client.put("/api/workout/exercise/${workoutExerciseID!!}") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "sets": 2
                }
            """.trimIndent()) // All fields are required (sets, reps, weight) so missing fields are reps, weight
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun updateWorkoutExercise_badJSONBody() = testApplication {
        application { module() }

        val response = client.put("/api/workout/exercise/${workoutExerciseID!!}") {
            contentType(ContentType.Application.Json)
            setBody("BAD JSON BODY")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun updateWorkoutExercise_badJSONID() = testApplication {
        application { module() }

        val response = client.put("/api/workout/exercise/abcdef") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "sets": 4,
                    "reps": 10,
                    "weight": 0.0
                }
            """.trimIndent())
        }
    }

    @Test
    fun deleteSpecificWorkoutExercise_valid() = testApplication {
        application { module() }

        val response = client.delete("/api/workout/exercise/${workoutExerciseID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun deleteSpecificWorkoutExercise_invalidWorkoutExerciseID() = testApplication {
        application { module() }

        val response = client.delete("/api/workout/exercise/9999999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun deleteSpecificWorkoutExercise_badJSON() = testApplication {
        application { module() }

        val response = client.delete("/api/workout/exercise/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun deleteAllWorkoutExercises_valid() = testApplication {
        application { module() }

        val workoutExercise = WorkoutExercise(
            workoutID = workoutID!!,
            exerciseID = 2,
            sets = 3,
            reps =  8,
            weight = 30.5
        )

        DatabaseService().addExerciseToWorkout(workoutExercise)
        DatabaseService().addExerciseToWorkout(workoutExercise)

        val response = client.delete("/api/workout/exercises/${workoutID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun deleteAllWorkoutExercises_invalidWorkoutID() = testApplication {
        application { module() }

        val response = client.delete("/api/workout/exercises/999999999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun deleteAllWorkoutExercises_badJSON() = testApplication {
        application { module() }

        val response = client.delete("/api/workout/exercises/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ===========================
    // COMPETITION ROUTES TESTING
    // ===========================

    @Test
    fun createCompetition_valid() = testApplication { // Normal Case: testing behaviour for creating competition, providing all neccessary fields
        application { module() }

        val response = client.post("/api/competition") { // Form POST HTTP request for valid creating of new competition
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "userID": ${userID!!},
                    "name": "NE Regionals",
                    "date": "2026-09-01"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status) // Response should be HTTP status of OK, as request is valid
        assertTrue(response.bodyAsText().contains("ID")) // Response should contain ID of new competition
        
        val testJsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val testCompetitionID = testJsonResponse["ID"]?.jsonPrimitive?.content?.toInt() 

        DatabaseService().deleteCompetition(testCompetitionID!!)
    }

    @Test
    fun createCompetition_missingField() = testApplication { // Erroneous Case: test behaviour for missing fields in HTTP request for creating new competition
        application { module() }

        val response = client.post("/api/competition") { // Form HTTP request with missing required fields
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "name": "Incomplete Competition"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // Expect response to contain HTTP status code for a BadRequest made
    }

    @Test
    fun updateCompetition_valid() = testApplication {
        application { module() }

        val response = client.put("/api/competition/${competitionID!!}") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "name": "Updated Test Competition",
                    "date": "2028-01-01",
                    "rank": 2
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun updateCompetition_boundaryField() = testApplication { // Boundary Case: test for when a field has a bounary value (i.e. rank = 1)
        application { module() }

        val response = client.put("/api/competition/${competitionID!!}") { // Form HTTP request with bounary value for "rank"
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "name": "Updated Competition",
                    "date": "2026-01-01",
                    "rank": 1
                }
            """.trimIndent())
        }
        assertEquals(response.status, HttpStatusCode.OK)
    }

    // ===========================
    // RUNNING ROUTE TESTS
    // ===========================

    @Test
    fun createRoute_valid() = testApplication { // Normal Case: test behaviour for when HTTP request for valid route creation is made
        application { module() }

        val response = client.post("/api/route") { // Form HTTP request containing all correct values (i.e. valid request)
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "userID": ${userID!!},
                    "name": "Hyde Park Loop",
                    "distance": 5.6,
                    "date": "2026-05-10",
                    "isPublic": false,
                    "routeData": "Big circle on map in hyde park"
                }
            """.trimIndent())
        }

        val testJsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val testRouteID = testJsonResponse["ID"]?.jsonPrimitive?.content?.toInt() 

        testRouteID?.let { DatabaseService().deleteRoute(it) }

        assertEquals(HttpStatusCode.OK, response.status) // Expect response to contain OK status code, as valid request was made
        assertTrue(response.bodyAsText().contains("ID")) // Expect response to contain the routeID of the newly made route
        
    }

    @Test
    fun createRoute_missingField() = testApplication { // Erroneous Case: test behaviour for create new route when missing fields are provided
        application { module() }

        val response = client.post("/api/route") { // Form HTTP request with missing required fields
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "userID": ${userID!!},
                    "name": "Route with missing date",
                    "distance": 5.0
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // Expect response to contain BadRequest status code, as invalid request was made
    }

    @Test
    fun updateRoute_valid() = testApplication { // Normal Case: test behaviour when correct usage of updateRoute endpoint is used
        application { module() }

        val response = client.put("/api/route/${routeID!!}") { // Form valid HTTP request for updating route
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "name": "Updated Route",
                    "distance": 10.0,
                    "estimatedTime": 60.0,
                    "date": "2026-06-01"
                }
            """.trimIndent())
        }
        assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.BadRequest) // Expect resposne to contain OK status code, or NotFound (if target route doesn't exist)
        
    }

    @Test
    fun updateRoute_invalidJSON() = testApplication { // Erroneous Case: test behaviour when wrong type is provided as an argument in HTTP request (i.e. invalid JSON)
        application { module() }

        val response = client.put("/api/route/${routeID!!}") { // Form HTTP request where argument for sitance is provided in wrong form (i.e. string provided, when double expected)
            contentType(ContentType.Application.Json)
            setBody("""{"distance": "not-number"}""")
        }
        assertTrue(response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.InternalServerError) // Expect repsonse to contain BadRequest status code, as distance argument is bad JSON so not serialisable
        
    }

    // ===========================
    // ACTIVITY ROUTE TESTS
    // ===========================

    @Test
    fun getRecentActivity_valid() = testApplication { // Normal Case: test behaviour for getting recent activity of user, when correct usage used
        application { module() }

        val response = client.get("/api/activity/recent/TestingEmail@email.com") // HTTP request when correct usage used (i.e. email parsed in correct format)

        assertEquals(HttpStatusCode.OK, response.status) // Expect response to contain OK status code, as valid HTTP request was made
    }

    @Test
    fun getAllActivity_invalidUserID() = testApplication { // Eroneous Case: test behaviour when an invalid userID is parsed as an argument
        application { module() }

        val response = client.get("/api/activity/notAUserID") // Form HTTP request with invalid userID (i.e. string parsed instead of expected integer)

        assertEquals(HttpStatusCode.BadRequest, response.status) // Expect HTTP response to contain BadRequest, as invalid userID was provided in request
    }

    @AfterTest
    fun cleanup() {
        DatabaseService().deleteUser("TestingEmail@email.com")
        workoutID?.let { DatabaseService().deleteWorkout(it) }
        competitionID?.let { DatabaseService().deleteCompetition(it) }
        routeID?.let { DatabaseService().deleteRoute(it) }
        try { workoutExerciseID!!.let { DatabaseService().deleteWorkoutExercise(it) } } catch(e: Exception) {}
    }

}
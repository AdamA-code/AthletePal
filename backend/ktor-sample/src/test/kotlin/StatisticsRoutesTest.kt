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

import services.AuthService
import services.DatabaseService

class StatisticsRoutesTest {

    private fun ApplicationTestBuilder.setup() {
        application { module() }
    }

    @BeforeTest
    var userID: Int? = null
    var statGetID: Int? = null

    fun setup() = testApplication {
        application { module() }
        userID = AuthService().registerUser("StatisticsTestAccount", "StatsTestEmail@email.com",  "wordpass", false)
        
        val resp = client.post("/api/statistics") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "userID": ${userID!!},
                    "exerciseID": 2,
                    "scoreValue": 35.0,
                    "date": "2026-01-01"
                }
            """.trimIndent())
        }

        val jsonResp = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        statGetID = jsonResp["ID"]?.jsonPrimitive?.content?.toInt()

    }

    // ==========================================
    // STAT WRITE ENDPOINTS TESTS
    // ==========================================

    @Test
    fun addNewStat_valid() = testApplication {
        application { module() }
        val response = client.post("/api/statistics"){
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "userID": ${userID!!},
                    "exerciseID": 1,
                    "scoreValue": 20.0,
                    "date": "2026-04-20"
                }
            """.trimIndent())
        }

        val jsonResponse = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val statID = jsonResponse["ID"]?.jsonPrimitive?.content?.toInt()

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun addNewStat_invalidUserID() = testApplication {
        application { module() }
        val response = client.post("/api/statistics"){
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "userID": 9999,
                    "exerciseID": 1,
                    "scoreValue": 20.0,
                    "date": "2026-04-20"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }
    
    @Test
    fun addNewStat_invalidJSON() = testApplication {
        application { module() }
        val response = client.post("/api/statistics"){
            contentType(ContentType.Application.Json)
            setBody("Invalid JSON")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun updateStat_valid() = testApplication {
        application { module() }
        val response = client.put("/api/statistics/${statID!!}") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "scoreValue": 30.0,
                    "date": "2026-04-21"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun updateStat_invalidStatID() = testApplication {
        application { module() }
        val response = client.put("/api/statistics/9999") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "scoreValue": 30.0,
                    "date": "2026-04-21"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun updateStat_missingValue() = testApplication {
        application { module() }
        val response = client.put("/api/statistics/${statID!!}") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "date": "2026-04-21"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun updateStat_invalidJSON() = testApplication {
        application { module() }
        val response = client.put("/api/statistics/${statID!!}") {
            contentType(ContentType.Application.Json)
            setBody("Invalid JSON")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun deleteStat_invalidStatID() = testApplication {
        application { module() }
        val response = client.delete("/api/statistics/99999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun deleteStat_invalidJSON() = testApplication {
        application { module() }
        val response = client.delete("/api/statistics/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun deleteStat_valid() = testApplication {
        application { module() }
        val response = client.delete("/api/statistics/${statID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    // ==========================================
    // STAT READ ENDPOINTS TESTS
    // ==========================================

    @Test
    fun getBestStat_valid() = testApplication {
        application { module() }
        val response = client.get("/api/statistic/exercise/${userID!!}/2")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getBestStat_invalidUserID() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/exercise/9999999/2")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
    
    @Test
    fun getBestStat_invalidExerciseID() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/exercise/${userID!!}/9999999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getBestStat_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/exercise/abcd/efgh")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getStat_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/${statGetID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getStat_invalidStatID() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/9999999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getStat_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getUserProgression_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/workout-progression/${userID!!}")

        assertEquals(HttpStatusCode.OK, response.status || HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getUserProgression_invalidUserID() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/workout-progression/9999999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
    
    @Test
    fun getUserProgression_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/workout-progression/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getWorkoutsCompleted_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/workouts-completed/${userID!!}")

        assertEquals(HttpStatusCode.OK, response.status || HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getWorkoutsCompleted_invalidUserID() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/workouts-completed/999999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getWorkoutsCompleted_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/workouts-completed/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getHoursTrained_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/hours-trained/${userID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getHoursTrained_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/hours-trained/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getWorkoutStreak_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/workout-streak/${userID!!}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getWorkoutStreak_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/workout-streak/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getWeeklyHistory_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/training-history/${userID!!}")

        assertEquals(HttpStatusCode.OK, response.status || HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getWeeklyHistory_invalidUserID() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/training-history/9999999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getWeeklyHistory_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistic/user/training-history/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getExerciseBreakdown_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/user/exercise-breakdown/${userID!!}")

        assertTrue(HttpStatusCode.Ok, response.status || HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getExerciseBreakdown_invalidUserID() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/user/exercise-breakdown/99999999")

        assertTrue(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getExerciseBreakdown_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/user/exercise-breakdown/abc")

        assertTrue(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getPersonalBest_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/user/personal-bests/${userID!!}")

        assertTrue(HttpStatusCode.OK, response.status || HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getPersonalBest_invalidUserID() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/user/personal-bests/9999999")

        assertTrue(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getPersonalBest_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/user/personal-bests/abc")

        assertTrue(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun getAllUserExerciseStats_valid() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/exercise/${userID!!}/2")

        assertTrue(HttpStatusCode.OK, response.status)
    }

    @Test
    fun getAllUserExerciseStats_invalidUserID() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/exercise/9999999/2")

        assertTrue(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getAllUserExerciseStats_invalidExerciseID() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/exercise/${userID!!}/999999")

        assertTrue(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun getAllUserExerciseStats_badJSON() = testApplication {
        application { module() }

        val response = client.get("/api/statistics/exercise/abc/def")

        assertTrue(HttpStatusCode.BadRequest, response.status)
    }

    // =================================================
    // POST-TEST CLEANUP (deleting test user and stats)
    // =================================================

    @AfterTest
    fun cleanup() {
        DatabaseService().deleteUser("StatsTestEmail@email.com")
        DatabaseService().deleteStatistic(statGetID)

        try {
            DatabaseService().deleteStatistic(statID)
        }
        catch (e: Exception) {
            println("Test-made stat already deleted!")
        }

    }

}
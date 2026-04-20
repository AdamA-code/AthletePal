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

    }

    @Test
    fun getBestStat_invalidUserID() = testApplication {

    }
    
    @Test
    fun getBestStat_invalidExerciseID() = testApplication {

    }

    @Test
    fun getBestStat_badJSON() = testApplication {

    }

    @Test
    fun getStat_valid() = testApplication {

    }

    @Test
    fun getStat_invalidStatID() = testApplication {

    }

    @Test
    fun getStat_badJSON() = testApplication {

    }

    @Test
    fun getUserProgression_valid() = testApplication {

    }

    @Test
    fun getUserProgression_invalidUserID() = testApplication {

    }
    
    @Test
    fun getUserProgression_badJSON() = testApplication {

    }

    @Test
    fun getWorkoutsCompleted_valid() = testApplication {

    }

    @Test
    fun getWorkoutsCompleted_invalidUserID() = testApplication {

    }

    @Test
    fun getWorkoutsCompleted_badJSON() = testApplication {

    }

    @Test
    fun getHoursTrained_valid() = testApplication {

    }

    @Test
    fun getHourstrained_invalidUserID() = testApplication {

    }

    @Test
    fun getHoursTrained_badJSON() = testApplication {

    }

    @Test
    fun getWorkoutStreak_valid() = testApplication {

    }

    @Test
    fun getWorkoutStreak_invalidUserID() = testApplication {

    }

    @Test
    fun getWorkoutStreak_badJSON() = testApplication {

    }

    @Test
    fun getWeeklyHistory_valid() = testApplication {

    }

    @Test
    fun getWeeklyHistory_invalidUserID() = testApplication {

    }

    @Test
    fun getWeeklyHistory_badJSON() = testApplication {

    }

    @Test
    fun getExerciseBreakdown_valid() = testApplication {

    }

    @Test
    fun getExerciseBreakdown_invalidUserID() = testApplication {

    }

    @Test
    fun getExerciseBreakdown_badJSON() = testApplication {

    }

    @Test
    fun getPersonalBest



    // =================================================
    // POST-TEST CLEANUP (deleting test user and stats)
    // =================================================

    @AfterTest
    fun cleanup() {
        DatabaseService().deleteUser("StatsTestEmail@email.com")
        DatabaseService().deleteStatistic(statGetID)

    }

}
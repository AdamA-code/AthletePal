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
import io.mockk.mockk

import services.AuthService
import services.DatabaseService

class AuthRoutesTest {

    // ========================================
    // Pre-Test Setup
    // ========================================

    val authService = mockk<AuthService>() // Creates a mock of AuthService for testing

    private fun ApplicationTestBuilder.setup() {
        application {
            module()
        }
    }

    var userID: Int? = null

    @BeforeTest
    fun setup() = testApplication {
        application { module() }
        userID = AuthService().registerUser("TestAccount", "TestingEmail@email.com", "wordpass", false)
    }

    // =======================
    // CREATE ACCOUNT TESTS
    // =======================

    @Test
    // Test function for 'happy case' where acccount successfully created
    fun createAccount_Success() = testApplication { 
        application {
            module()
        }

        val response = client.post("/auth/create-account"){ // Create valid HTTP request for account creation
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "name": "TestUser",
                    "email": "testuser@test.com",
                    "password": "wordpass",
                    "accountType": true
                }
            """.trimIndent())
        }

        assertEquals(HttpStatusCode.OK, response.status) // Assume response has OK status code
        assertTrue(response.bodyAsText().contains("ID")) // Assume response contains success message
    }

    @Test
    // Test function for trying to create account which already exists
    fun createAccount_alreadyExists() = testApplication {
        application() {
            module()
        }

        val response = client.post("/auth/create-account"){ // Attempt adding a duplicate of the account
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "name": "TestAccount",
                    "email": "TestingEmail@email.com",
                    "password": "wordpass",
                    "accountType": false
                }
            """.trimIndent())
        }

        assertEquals(HttpStatusCode.NotAcceptable, response.status) // Request is technically valid so check HTTP status is OK
        assertTrue(response.bodyAsText().contains("User already exists!")) // Check that logically request is rejected by server

    }

    @Test
    // Test function for a HTTP request not given in correct JSON formatting
    fun createAccount_invalidJSON() = testApplication{
        application {
            module()
        }

        val response = client.post("/auth/create-account"){ // HTTP request for account creation using bad JSON on accountType
            contentType(ContentType.Application.Json)
            setBody("Invalid JSON")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // Assume HTTP error is given, so response is 200/500
    }

    @Test
    // Test function for create account attempt with missing values
    fun createAccount_missingRequiredFields() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/create-account") { // HTTP request for account creation (missing name, accountType fields)
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "test@test.com",
                    "password": "wordpass"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // Presume an error is thrown, so expect response status greater or equal to 400
    }

    // =======================
    // LOGIN TESTS
    // =======================

    @Test
    // Test function for 'happy case' where login done successfully
    fun accountLogin_Success() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/login") { // Attempt to login to dummy account we just created, using correct credentials
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "TestingEmail@email.com",
                    "password": "wordpass"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status) // Valid HTTP request, so should be equal to HTTPStatusCode.OK
        assertTrue(response.bodyAsText().contains("Login Successful")) // We know account exists, as just created, so expect successful login message
    }

    @Test
    // Test function for incorrect password given for a given account
    fun accountLogin_invalidPassword() = testApplication {
        application {
            module()
        }
        
        val response = client.post("/auth/login") { // Create login HTTP request for dummy account, using wrong password
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "TestingEmail@email.com"
                    "password": "WrongPassword"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.NotFound, response.status) // HTTP request is syntactically correct so expect OK status code
        assertTrue(response.bodyAsText().contains("Invalid Credentials")) //Logically credentials dont match, so expect relevent error to be given 
    }

    @Test
    // Test fucntion for trying to login to an account which doesn't exist
    fun accountLogin_nonexistentUser() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/login") { // Create HTTP request for login to an account which isnt in database
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "fakeuser@test.com",
                    "password": "wordpass"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.NotFound, response.status) // Syntax of HTTP request is correct so return OK status code
        assertTrue(response.bodyAsText().contains("Invalid Credentials")) // Logically there is no matching user in databse, so expect relevent error
    }

    @Test
    // Test function for login request being made using bad JSON
    fun accountLogin_invalidJson() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/login") { // Create HTTP request for login using incorrect JSON for password
            contentType(ContentType.Application.Json)
            setBody("Invalid JSON")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // HTTP request has incorrect syntax as required to be JSON serialisable
    }

    @Test
    // Test function for a login request with insufficient fields for succesful login
    fun accountLogin_missingFields() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/login") { // Create a HTTP login request contianing only an email but no password
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "testuser@test.com"
             }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status) // Expect HTTP response to show an error, as more values expected by client
    }

    // ========================================
    // Post-Test Clean-Up (Delete Test User)
    // ========================================

    @AfterTest
    fun cleanup() {
        DatabaseService().deleteUser("TestingEmail@email.com")
        DatabaseService().deleteUser("testuser@test.com")
    }

}
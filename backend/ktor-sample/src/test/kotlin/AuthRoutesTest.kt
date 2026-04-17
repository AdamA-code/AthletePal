import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.*
import com.example.module
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication
import io.mockk.mockk

import services.AuthService

class AuthRoutesTest {

    val authService = mockk<AuthService>() // Creates a mock of AuthService for testing

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
                    "email": "testuser${System.nanoTime()}@test.com"
                    "password": "wordpass",
                    "accountType": true
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status) // Assume response has OK status code
        assertTrue(response.bodyAsText().contains("Account created!")) // Assume response contains success message
    }

    @Test
    // Test function for trying to create account which already exists
    fun createAccount_alreadyExists() = testApplication {
        application() {
            module()
        }

        val body = """
            {
                "name": "DuplicateAccount",
                "email": "duplicate@test.com",
                "password": "wordpass",
                "accountType": true
            }
        """.trimIndent() // HTTP request for creaating same (duplicate account)

        client.post("/auth/create-account"){ // Initially add the account (doesn't exist prior, so can be added)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val response = client.post("/auth/create-account"){ // Attempt adding a duplicate of the account
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.OK, response.status) // Request is technically valid so check HTTP status is OK
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
            setBody("""
                {
                    "name": "BadJson",
                    "email": "test@test.com",
                    "password": "wordpass",
                    "accountType": True
                }
            """.trimIndent())
        }
        assertTrue(response.status.value == 200 || response.status.value == 500) // Assume HTTP error is given, so response is 200/500
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
        assertTrue(response.status.value >= 400) // Presume an error is thrown, so expect response status greater or equal to 400
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

        client.post("/auth/create-account") { // HTTP request to create a dummy account for login attempting
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "name": "LoginTestUser",
                    "email": "login@test.com",
                    "password": "wordpass",
                    "accountType": true
                }
            """.trimIndent())
        }

        val response = client.post("/auth/login") { // Attempt to login to dummy account we just created, using correct credentials
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "login@test.com",
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
        
        client.post("/auth/create-account") { // Create a dummy account which we will try to login with incorrect password
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "name": "user",
                    "email": "wrongpassword@test.com",
                    "password": "CorrectPassword",
                    "accountType": true
                }
            """.trimIndent())
        }

        val response = client.post("/auth/login") { // Create login HTTP request for dummy account, using wrong password
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "wrongpassword@test.com",
                    "password": "WrongPassword"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status) // HTTP request is syntactically correct so expect OK status code
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
        assertEquals(HttpStatusCode.OK, response.status) // Syntax of HTTP request is correct so return OK status code
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
            setBody("""
                {
                    "email": "test@test.com",
                    "password" : 123456
                }
            """.trimIndent())
        }
        assertTrue(response.status.value == 200 || response.status.value == 500) // HTTP request has incorrect syntax as required to be JSON serialisable
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
                    "email": "test@test.com"
                }
            """.trimIndent())
        }
        assertTrue(response.status.value >= 400) // Expect HTTP response to show an error, as more values expected by client
    }

}
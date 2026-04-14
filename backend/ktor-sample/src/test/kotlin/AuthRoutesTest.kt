import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.*
import com.example.module
import io.ktor.server.application.Application
import io.ktor.server.testing.testApplication

class AuthRoutesTest {

    // =======================
    // CREATE ACCOUNT TESTS
    // =======================

    @Test
    fun createAccount_Success() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/create-account"){
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
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Account created!"))
    }

    @Test
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
        """.trimIndent()

        client.post("/auth/create-account"){
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val response = client.post("/auth/create-account"){
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("User already exists!"))

    }

    @Test
    fun createAccount_invalidJSON() = testApplication{
        application {
            module()
        }

        val response = client.post("/auth/create-account"){
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
        assertTrue(response.status.value >= 400)
    }

    @Test
    fun createAccount_missingRequiredFields() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/create-account") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "test@test.com",
                    "password": "wordpass"
                }
            """.trimIndent())
        }
        assertTrue(response.status.value >= 400)
    }

    // =======================
    // LOGIN TESTS
    // =======================

    @Test
    fun accountLogin_Success() = testApplication {
        application {
            module()
        }

        client.post("/auth/create-account") {
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

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "login@test.com",
                    "password": "wordpass"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Login Successful"))
    }

    @Test
    fun accountLogin_invalidPassword() = testApplication {
        application {
            module()
        }
        
        client.post("/auth/create-account") {
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

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "wrongpassword@test.com",
                    "password": "WrongPassword"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Invalid Credentials"))
    }

    @Test
    fun accountLogin_nonexistentUser() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "fakeuser@test.com",
                    "password": "wordpass"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Invalid Credentials"))
    }

    @Test
    fun accountLogin_invalidJson() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "test@test.com",
                    "password" : 123456
                }
            """.trimIndent())
        }
        assertTrue(response.status.value >= 400)
    }

    @Test
    fun accountLogin_missingFields() = testApplication {
        application {
            module()
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "email": "test@test.com"
                }
            """.trimIndent())
        }
        assertTrue(response.status.value >= 400)
    }

}
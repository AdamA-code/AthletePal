import com.example.module
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.application
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import kotlinx.serialization.json.Json
import services.AuthService
import services.DatabaseService
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun setup() =
        testApplication {
            application { module() }
            // Create a test instance of a user in db for account-related tests, using mock of AuthService
            userID = AuthService().registerUser("TestAccount", "TestingEmail@email.com", "wordpass", false)
        }

    // =======================
    // CREATE ACCOUNT TESTS
    // =======================

    // Test function for 'happy case' where acccount successfully created
    fun createAccount_valid() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified route, providing details for account creation
            val response =
                client.post("/auth/create-account") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Valid HTTP request body, as all fields are passed in body (in correct format and data types)
                    setBody(
                        """
                        {
                            "name": "TestUser",
                            "email": "testuser@test.com",
                            "password": "wordpass",
                            "accountType": true
                        }
                        """.trimIndent(),
                    )
                }

            // Assume response has OK status code
            assertEquals(HttpStatusCode.OK, response.status)
            // Assume response contains success message
            assertTrue(response.bodyAsText().contains("ID"))

            // Delete test account 'testuser@test.com' after creation in test
            DatabaseService().deleteUser("testuser@test.com")
        }

    // Test function for trying to create account which already exists
    @Test
    fun createAccount_alreadyExists() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified route, providing details for account creation
            val response =
                client.post("/auth/create-account") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Syntactically valid HTTP request body, as all required fields passed (logically incorrect as 'email' already in use)
                    setBody(
                        """
                        {
                            "name": "TestAccount",
                            "email": "TestingEmail@email.com",
                            "password": "wordpass",
                            "accountType": false
                        }
                        """.trimIndent(),
                    )
                }

            // Expect NotAcceptable status code as account already exists using email, so request cannot be fulfilled
            assertEquals(HttpStatusCode.NotAcceptable, response.status)
            // Expect relevent error message returned, representing status code given also in response
            assertTrue(response.bodyAsText().contains("User already exists!"))
        }

    // Test function for a HTTP request not given in correct JSON formatting
    @Test
    fun createAccount_invalidJSON() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified route, providing details for account creation
            val response =
                client.post("/auth/create-account") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Invalid HTTP request body, as does not match correct JSON format so will fail serialisation
                    setBody("Invalid JSON")
                }
            // Expect BadRequest status code, as exception hit when trying to serialise bad JSON body (due to incorrect formatting)
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // Test function for create account attempt with missing values
    @Test
    fun createAccount_missingRequiredFields() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified route, providing details for account creation
            val response =
                client.post("/auth/create-account") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Invalid HTTP body, as we do not provide values which are required, hence serialisation fails
                    setBody(
                        """
                        {
                            "email": "test@test.com",
                            "password": "wordpass"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect BadRequest status code, as we hit an exception when trying to serialise bad JSON body of HTTP request
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // =======================
    // LOGIN ROUTE TESTS
    // =======================

    // Test function for 'happy case' where login done successfully
    @Test
    fun accountLogin_Success() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified login route, providing login details for verification
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Valid HTTP request body, as all required fields (email and password) are provided in correct data types
                    setBody(
                        """
                        {
                            "email": "TestingEmail@email.com",
                            "password": "wordpass"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect OK status code returned, as we provide correct details (in correct format) to allow for successful login verification
            assertEquals(HttpStatusCode.OK, response.status)
            // Expect return of a relevent acceptance message, corresponding to OK status code
            assertTrue(response.bodyAsText().contains("Login Successful"))
        }

    // Test function for incorrect password given for a given account
    fun accountLogin_invalidPassword() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified login route, providing login details for verification
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Syntactically correct HTTP request, as all fields provided in correct data types (however logically incorrect password given)
                    setBody(
                        """
                        {
                            "email": "TestingEmail@email.com"
                            "password": "WrongPassword"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect NotFound status code, as password does not match targeted account (specified by email)
            assertEquals(HttpStatusCode.NotFound, response.status)
            // Expect a relevent response message, explaining reasoning for corresponding status code returned
            assertTrue(response.bodyAsText().contains("Invalid Credentials"))
        }

    // Test fucntion for trying to login to an account which doesn't exist
    @Test
    fun accountLogin_nonexistentUser() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified login route, providing login details for verification
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Syntactically valid HTTP request, as all fields correctly provided (logically incorrect as target email doesn't exist)
                    setBody(
                        """
                        {
                            "email": "fakeuser@test.com",
                            "password": "wordpass"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect NotFound status code returned, as no user under provided email exists in db
            assertEquals(HttpStatusCode.NotFound, response.status)
            // Expect return of response message, explaining why NotFound status code is returned
            assertTrue(response.bodyAsText().contains("Invalid Credentials"))
        }

    // Test function for login request being made using bad JSON
    @Test
    fun accountLogin_invalidJson() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified login route, providing login details for verification
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Invalid malformed HTTP request body, as does not match the format of login request, thus fails serialisation
                    setBody("Invalid JSON")
                }
            // Expect BadRequest status code, as exception is met when trying to serialise HTTP request's body
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // Test function for a login request with insufficient fields for succesful login
    @Test
    fun accountLogin_missingFields() =
        testApplication {
            application {
                module()
            }

            // Make POST request to specified login route, providing login details for verification
            val response =
                client.post("/auth/login") {
                    contentType(ContentType.Application.Json) // Declare content type of HTTP request to be JSON
                    // Invalid HTTP request body, as we only provide one of the two required fields for a login, thus fails serialisation
                    setBody(
                        """
                           {
                               "email": "testuser@test.com"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect BadRequest status code, as we cannot serialise HTTP request body due to missing 'password' field
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // ========================================
    // ACCOUNT UPDATE ROUTES TESTS
    // ========================================

    // Test for changing non-essential account information (i.e. height, weight, name, etc)
    @Test
    fun accountUpdateTests_valid() =
        testApplication {
            application { module() }

            // Account update route is a PUT route at '.../update/account'
            val response =
                client.put("/update/account") {
                    contentType(ContentType.Application.Json) // Declare JSON body type
                    // Body has all required fields, in correct format and data types
                    setBody(
                        """
                        {
                            "email": "TestingEmail@email.com",
                            "gender": "Male",
                            "age": 22,
                            "height": 175.5,
                            "weight": 68.0
                        }
                        """.trimIndent(),
                    )
                }
            // Expect OK HTTP status code returned as we give all values for a successful account update
            assertEquals(HttpStatusCode.OK, response.status)
        }

    // Test for account update when specified account doesn't exist (i.e. email is invalid)
    @Test
    fun accountUpdateTests_nonexistantUser() =
        testApplication {
            application { module() }

            // Account update route is a PUT route at '.../update/account'
            val response =
                client.put("/update/account") {
                    contentType(ContentType.Application.Json) // Declare JSON body type
                    // Body is syntactically correct, but email does not exist in db to update account
                    setBody(
                        """
                        {
                            "email": "FalseEmail@mailservice.com",
                            "gender": "Male",
                            "age": 22,
                            "height": 175.5,
                            "weight": 68.0
                        }
                        """.trimIndent(),
                    )
                }
            // Expect NotFound status code, as email cannot be found to an account in db so account not updated
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    // Test for account updating when HTTP request only has required fields for updating
    @Test
    fun accountUpdateTests_missingOptionalFields() =
        testApplication {
            application { module() }

            // Account update route is a PUT route at '.../update/account'
            val response =
                client.put("/update/account") {
                    contentType(ContentType.Application.Json) // Declare JSON body type
                    // Acceptable JSON body, as the only required field for account update is 'email'
                    setBody(
                        """
                        {
                            "email": "TestingEmail@email.com"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect OK status code, as we update account specified by email with null (default) values
            assertEquals(HttpStatusCode.OK, response.status)
        }

    // Test for account updating when HTTP request has all optional fields, but not required fields
    @Test
    fun accountUpdateTests_missingRequiredFields() =
        testApplication {
            application { module() }

            // Account update route is a PUT route at '.../update/account'
            val response =
                client.put("/update/account") {
                    contentType(ContentType.Application.Json) // Declare JSON body type
                    // Invalid HTTP request, as we require an entry for 'email' field as it isn't optional
                    setBody(
                        """
                        {
                            "gender": "Male",
                            "age": 22,
                            "height": 175.5,
                            "weight": 68.0
                        }
                        """.trimIndent(),
                    )
                }
            // Expect BadRequest status code, as we hit an exception when trying to extract 'email' from request
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // Test for account updates when HTTP request has fields given as incorrect data types
    @Test
    fun accountUpdateTests_incorrectDataTypes() =
        testApplication {
            application { module() }

            // Account update route is a PUT route at '.../update/account'
            val response =
                client.put("/update/account") {
                    contentType(ContentType.Application.Json) // Declare JSON body type
                    // Incorrect HTTP request body, as values are given in incorrect data types
                    setBody(
                        """
                        {
                            "email": TestingEmail@email.com,
                            "gender": Male,
                            "age": 22.0,
                            "height": 175,
                            "weight": 68
                        }
                        """.trimIndent(),
                    )
                }
            // Expect BadRequest as serialisation fails due to incorrect data types, hitting an exception
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // Test for account updates when HTTP request is malformed so cannot be serialised
    @Test
    fun accountUpdateTests_badJSON() =
        testApplication {
            application { module() }

            // Account update route is a PUT route at '.../update/account'
            val response =
                client.put("/update/account") {
                    contentType(ContentType.Application.Json) // Declare JSON body type
                    // Incorrect HTTP request body as it completely doesn't follow the JSON body formatting
                    setBody("BAD JSON BODY")
                }
            // Expect BadRequest status code as serialisation fails due to bad formatting, and hitting an exception
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // Test for all values given, and correct (i.e. oldPassword is correct to db) and in correct format and data types (strings)
    @Test
    fun passwordChange_valid() =
        testApplication {
            application { module() }

            // Password change route is PUT directed to '/update/password/{email}' where email is given as a parameter
            val response =
                client.put("/update/password/TestingEmail@email.com") {
                    contentType(ContentType.Application.Json) // Declare body of JSON content type
                    // Valid HTTP body, as all fields in body (and parameter) are syntactically and logically correct
                    setBody(
                        """
                        {
                            "oldPassword": "wordpass",
                            "newPassword": "password"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect OK status code returned, as all values are given correctly and are logically sound for operation
            assertEquals(HttpStatusCode.OK, response.status)
        }

    // Test for all values given, however oldPassword field doesn't match that stored in the db
    @Test
    fun passwordChange_incorrectOldPassword() =
        testApplication {
            application { module() }

            // Password change route is PUT directed to '/update/password/{email}' where email is given as a parameter
            val response =
                client.put("/update/password/TestingEmail@email.com") {
                    contentType(ContentType.Application.Json) // Declare body of JSON content type
                    //  Syntactically correct HTTP body as all values given in correct format, yet oldPassword doesn't match db
                    setBody(
                        """
                        {
                            "oldPasword": "someIncorrectOldPassword",
                            "newPassword": "attemptedNewPassword"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect BadRequest status code, as oldPassword causes a logical error (due to not matching db entry)
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // Test for no value given for newPassword, however oldPassword is given and correct
    @Test
    fun passwordChange_noNewPassword() =
        testApplication {
            application { module() }

            // Password change route is PUT directed to '/update/password/{email}' where email is given as a parameter
            val response =
                client.put("/update/password/TestingEmail@email.com") {
                    contentType(ContentType.Application.Json) // Declare body of JSON content type
                    // Invalid HTTP body, as no value given for newPassword (a required field)
                    setBody(
                        """
                        {
                            "oldPassword": "password"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect BadRequest status code, as we are not given a field for newPassword, so request cannot be fulfilled
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // Test for no value given for oldPassword, however one is given for newPassword
    @Test
    fun passwordChange_noOldPassword() =
        testApplication {
            application { module() }

            // Password change route is PUT directed to '/update/password/{email}' where email is given as a parameter
            val response =
                client.put("/update/password/TestingEmail@email.com") {
                    contentType(ContentType.Application.Json) // Declare body of JSON content type
                    // Invalid HTTP request body as no value given for oldPassword field (which is required)
                    setBody(
                        """
                        {
                            "newPassword": "someNewPassword"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect BadRequest status code, as request cannot be fulfilled without all required fields (oldPassword missing)
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    // Test for an invalid email given in parameters, however oldPassword and newPassword given and correct
    @Test
    fun passwordChange_invalidEmail() =
        testApplication {
            application { module() }

            // Password change route is PUT directed to '/update/password/{email}' where email is given as a parameter
            val response =
                client.put("/update/password/FalseEmail@mailservice.com") {
                    contentType(ContentType.Application.Json) // Declare body of JSON content type
                    // Syntactically valid HTTP request, but email given in route parameters is logically incorrect
                    setBody(
                        """
                        {
                            "oldPassword": "someOldPassword",
                            "newPassword": "someNewPassword"
                        }
                        """.trimIndent(),
                    )
                }
            // Expect NotFound status code, as cannot find account associated with email (doesn't exist)
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    // Test for a malformed HTTP request body, in bad JSON, which fails serialisation
    @Test
    fun passwordChange_badJSON() =
        testApplication {
            application { module() }

            // Password change route is PUT directed to '/update/password/{email}' where email is given as a parameter
            val response =
                client.put("/update/password/TestEmail@email.com") {
                    contentType(ContentType.Application.Json) // Declare body of JSON content type
                    // Invalid HTTP request body as doesn't match required format, so will fail serialisation
                    setBody("BAD JSON BODY")
                }
            // Expect BadRequest status code, as we hit an exception when we try serialising bad JSON body
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

    /*
    fun forgottenPassword_valid() = testApplication {

    }
     */

    // Test for a valid case, where a valid email is given in route parameters for successful deletion
    @Test
    fun deleteAccount_valid() =
        testApplication {
            application { module() }

            // Create temporary account in database to test successful deletion in current test
            userID = AuthService().registerUser("TestAccount", "test654316@email.com", "wordpass", false)

            // Make HTTP request to account delete endpoint, giving temp account's email (which is valid)
            val response = client.delete("/update/account/test654316@email.com")

            // Expect OK status code, as we know account exists and email matches it, so account can be deleted from db
            assertEquals(HttpStatusCode.OK, response.status)
        }

    // Test case for where the email given in route parameter, does not match an active account for deletion from db
    @Test
    fun deleteAccount_invalidEmail() =
        testApplication {
            application { module() }

            // Make HTTP request to account delete endpoint, giving it a false email (which is invalid for db)
            val response = client.delete("/update/account/FalseEmail@mailservice.com")

            // Expect NotFound status code, as email parameter cannot be found in databse so can't be deleted
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    // ========================================
    // Post-Test Clean-Up (Delete Test User)
    // ========================================

    @AfterTest
    fun cleanup() {
        // Delete test account 'TestingEmail@email.com' after use in tests
        DatabaseService().deleteUser("TestingEmail@email.com")
    }
}

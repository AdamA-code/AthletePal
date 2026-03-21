package test

import java.sql.SQLException
import services.AuthService
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class AuthTest {

    private val auth = AuthService()

    @Test
    fun testSignupAndLogin() {
        // simulate user signup
        try {
            val success = auth.registerUser(
                name = "Finn",
                email = "Finn@outlook.com",
                password = "mypassword123",
                accountType = true
            )
            println("User created: $success")
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        // simulate login
        val loginSuccess = auth.validateLogin(
            email = "Finn@outlook.com",
            password = "mypassword123"
        )
        assertTrue(loginSuccess, "Login should succeed")

        // simulate failed login
        val loginFail = auth.validateLogin(
            email = "Finn@outlook.com",
            password = "wrongpassword"
        )
        assertTrue(!loginFail, "Login with wrong password should fail")
    }
}
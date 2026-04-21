package test

// import services.
import services.AuthService
import services.DatabaseService
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertTrue

class AuthTest {
    private val auth = AuthService()
    val ds = DatabaseService()

    @Test
    fun testSignupAndLogin() {
        // simulate user signup
        try {
            val success =
                auth.registerUser(
                    name = "Finn",
                    email = "Finn@outlook.com",
                    password = "mypassword123",
                    accountType = true,
                )
            println("User created: $success")
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        // simulate login
        val loginSuccess =
            auth.validateLogin(
                email = "Finn@outlook.com",
                password = "mypassword123",
            )
        assertTrue(loginSuccess, "Login should succeed")

        // simulate failed login
        val loginFail =
            auth.validateLogin(
                email = "Finn@outlook.com",
                password = "wrongpassword",
            )
        assertTrue(!loginFail, "Login with wrong password should fail")

        // Remove from DB
        assertTrue(ds.deleteUser("Finn@outlook.com"))
    }
}

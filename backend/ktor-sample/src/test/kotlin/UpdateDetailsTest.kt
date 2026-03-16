package test

import services.updateAccount
import services.AuthService
import kotlin.test.Test
import kotlin.test.assertTrue

class updateAccountTest {

    @Test
    fun testUpdateAccount() {

        val auth = AuthService()

        auth.registerUser("Finn", "Finn@outlook.com", "pass123", true)

        val updated = services.updateAccount(
            "Finn@outlook.com",
            "Male",
            19,
            180.5,
            75.2
        )

        assertTrue(updated)

        val user = services.findUser("Finn@outlook.com")

        println(user)
    }
}
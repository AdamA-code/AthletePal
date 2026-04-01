package test

import services.AuthService
import services.DatabaseService
import kotlin.test.Test
import kotlin.test.assertTrue

class updateAccountTest {

    @Test
    fun testUpdateAccount() {

        val databaseService = DatabaseService()
        val auth = AuthService()

        auth.registerUser("Finn", "Finn@outlook.com", "pass123", true)

        val updated = databaseService.updateAccount(
            "Finn@outlook.com",
            "Male",
            19,
            180.5,
            75.2
        )

        assertTrue(updated)

        val user = databaseService.findUser("Finn@outlook.com")

        println(user)
    }
}
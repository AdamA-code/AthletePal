package services

class AuthService {

    fun validateLogin(username: String, password: String): Boolean {
        // Testing logic below, replace with proper logic for authentication later
        val correctUsername = "admin"
        val correctPassword = "12345"

        return username == correctUsername && password == correctPassword

    }

}
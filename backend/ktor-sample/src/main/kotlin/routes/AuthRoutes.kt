package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.* // Import functionalityy to read JSON requests
import io.ktor.server.response.* // Import functionality to send responses to frontend

import models.LoginRequest // Import the model for a login request (i.e. formatting of HTTP request)
import services.AuthService // Import service (logic) for validating a login

fun Route.authRoutes() {
    val authService = AuthService() // Initialise AuthService in routes

    route("/auth") { // If HTTP request in the '/auth' route... 
        post("/login") { // If HTTP request is a POST of '/login'...
            val loginRequest = call.receive<LoginRequest>() // Break down the request into usable data, as outlined in LoginRequest

            val isValid = authService.validateLogin(loginRequest.username, loginRequest.password) // If authService deems the password acceptable

        if (isValid) {
            call.respond(mapOf("message" to "Login Successful")) // Response for valid login
        } else {
            call.respond(mapOf("message" to "Invalid Credentials")) // Response for invalid login
        }

        }
    }

}
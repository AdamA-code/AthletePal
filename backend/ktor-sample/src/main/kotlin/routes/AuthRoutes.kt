package routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.* // Import functionalityy to read JSON requests
import io.ktor.server.response.* // Import functionality to send responses to frontend

import models.LoginRequest // Import the model for a login request (i.e. formatting of HTTP request)
import models.CreateAccountRequest as CreateAccountRequest
import models.User as User // Import the model for account creation data
import models.ForgotPasswordRequest

import services.AuthService // Import service (logic) for validating a login
import services.DatabaseService // Import service for storing to database

fun Route.authRoutes() {
    val authService = AuthService() // Initialise AuthService in routes
    val databaseService = DatabaseService() // Initialise databaseService

    route("/auth") { // If HTTP request in the '/auth' route... 
        post("/login") { // If HTTP request is a POST of '/login'...
            val loginRequest = call.receive<LoginRequest>() // Break down the request into usable data, as outlined in LoginRequest

            val isValid = authService.validateLogin(loginRequest.email, loginRequest.password) // If authService deems the password acceptable

            if (isValid) {
                call.respond(mapOf("message" to "Login Successful")) // Response for valid login
            } else {
                call.respond(mapOf("message" to "Invalid Credentials")) // Response for invalid login
            }

        }

        post("/create-account") { // If HTTP request is post of account creation
            val createAccountRequest = call.receive<CreateAccountRequest>() // Retrieve data from POST, and store in a class 'createAccountRequest'

            val accountCreated = authService.registerUser(createAccountRequest.name, createAccountRequest.email, createAccountRequest.password, createAccountRequest.accountType) // Call function for account creation in authService, retrieve boolean confirmation

            if (accountCreated > 0) {
                call.respond(mapOf("message" to "Account created!")) // Success message if account successfully created
            } else {
                call.respond(mapOf("message" to "User already exists!")) // Fail message if account not created (as user already exists)
            }
        }


         
        /*post("/forgot-password") {
            val targetUser = call.receive<ForgotPasswordRequest>() // Retrieve data from request

            val verificationSent: Boolean = databaseService.PasswordReset(targetUser.email) // Call password reset function
            
            if (verificationSent) {
                call.respond(mapOf("message" to "E-Mail sent!"))
            } else {
                call.respond(mapOf("message" to "Account does not exist!"))
            }
        } 
*/
    }
}
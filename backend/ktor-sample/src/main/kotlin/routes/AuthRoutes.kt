package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import models.LoginRequest // Import the model for a login request (i.e. formatting of HTTP request)
import models.UpdateAccountRequest
import services.AuthService // Import service (logic) for validating a login
import services.DatabaseService // Import service for storing to database
import models.CreateAccountRequest as CreateAccountRequest

fun Route.authRoutes() {
    val authService = AuthService() // Initialise AuthService in routes
    val db = DatabaseService() // Initialise databaseService

    route("/auth") { // If HTTP request in the '/auth' route...
        post("/login") { // If HTTP request is a POST of '/login'...
            try {
                val loginRequest = call.receive<LoginRequest>() // Break down the request into usable data, as outlined in LoginRequest

                val isValid =
                    authService.validateLogin(
                        loginRequest.email,
                        loginRequest.password,
                    ) // If authService deems the password acceptable

                if (isValid) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Login Successful")) // Response for valid login
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Invalid Credentials")) // Response for invalid login
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        post("/create-account") { // If HTTP request is post of account creation
            try {
                // Retrieve data from POST, and store in a class 'createAccountRequest'
                val createAccountRequest = call.receive<CreateAccountRequest>()

                val accountCreated =
                    authService.registerUser(
                        createAccountRequest.name,
                        createAccountRequest.email,
                        createAccountRequest.password,
                        createAccountRequest.accountType,
                    ) // Call function for account creation in authService, retrieve boolean confirmation

                if (accountCreated > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("ID" to accountCreated)) // Success message if account successfully created
                } else {
                    call.respond(
                        HttpStatusCode.NotAcceptable,
                        mapOf("message" to "User already exists!"),
                    ) // Fail message if account not created (as user already exists)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }
    }

    route("/update") {
        // Update account information of a given user
        put("/account") {
            try {
                val body = call.receive<UpdateAccountRequest>()

                val updatedInfo =
                    db.updateAccount(
                        body.email,
                        body.gender,
                        body.age,
                        body.height,
                        body.weight,
                    )

                if (updatedInfo == true) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Account successfully updated"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Account not found"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        delete("/account/{email}") {
            try {
                val email = call.parameters["email"]!!
                val deleted = db.deleteUser(email)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User successfully deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "User not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }
    }
}

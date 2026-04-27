package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.* // Import functionalityy to read JSON requests
import io.ktor.server.response.* // Import functionality to send responses to frontend
import io.ktor.server.routing.*
import models.LoginRequest // Import the model for a login request (i.e. formatting of HTTP request)
import models.UpdateAccountRequest
import services.AuthService // Import service (logic) for validating a login
import services.DatabaseService // Import service for storing to database
import models.CreateAccountRequest as CreateAccountRequest
import models.PasswordUpdateRequest as PasswordUpdateRequest
import models.ForgotPasswordRequest as ForgotPasswordRequest

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
                    call.respond(HttpStatusCode.OK, mapOf(
                        "message" to "Login Successful",
                        "userId" to (user?.userID ?: 0),
                        "name" to (user?.name ?: "")
                        )) // Response for valid login
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Invalid Credentials")) // Response for invalid login
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        post("/create-account") { // If HTTP request is post of account creation
            try {
                val createAccountRequest =
                    call.receive<CreateAccountRequest>() // Retrieve data from POST, and store in a class 'createAccountRequest'

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
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Route for changing password (when current password is known)
        put("/password/{email}") {
            try {
                val email: String = call.parameters["email"]!!
                val body = call.receive<PasswordUpdateRequest>()

                val updated: Boolean = db.updatePassword(email, body.oldPassword, body.newPassword)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Password successfully updated"))
                }
                else if (!updated) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Password not valid for user email"))
                }
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request"))
            }
        }

        // Route for reseting a password (when current is not known, i.e. forgot password)
        get("/forgot-password/get-reset-email/{email}") {
            try {
                val email: String = call.parameters["email"]!!

                val emailSent: Boolean = db.sendResetEmail(email)

                if (emailSent) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Check inbox"))
                }
                else if (!emailSent) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Request could not be fulfilled"))
                }

            }
            catch (e: Exception) {
                call.respond("message" to "Invalid Request")
            }
        }

        put("/forgot-password/enter-token") {
            try {
                val body = call.receive<ForgotPasswordRequest>()

                val updated: Boolean = db.resetPasswordWithToken(body.token, body.newPassword)

                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Password updated"))
                }
                else if (!updated) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Password couldn't be updated"))
                }
            }
            catch (e: Exception) {
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

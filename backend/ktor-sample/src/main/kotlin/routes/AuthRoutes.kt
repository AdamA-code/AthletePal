package routes

import io.ktor.http.HttpStatusCode // Import HTTP status codes for HTTP responses to frontend
import io.ktor.server.request.receive // Import functionality to receive HTTP requests
import io.ktor.server.response.respond // Import functionality to respond to HTTP requests
import io.ktor.server.routing.Route // Import routing
import io.ktor.server.routing.delete // Import delete HTTP requests routes
import io.ktor.server.routing.get // Import get HTTP requests routes
import io.ktor.server.routing.post // Import post HTTP requests routes
import io.ktor.server.routing.put // Import put HTTP requests routes
import io.ktor.server.routing.route
import models.LoginRequest // Import the model for a login request (i.e. formatting of HTTP request)
import models.UpdateAccountRequest // Import the model for a request to update account info
import services.AuthService // Import service (logic) for validating a login
import services.DatabaseService // Import service for storing to database
import models.CreateAccountRequest // Import model for request to create an account

/*
Defines endpoints for authentication and account-related HTTP routes for the webapp.
This includes:
    - User login
    - Account creation
    - Updating account information
    - Resetting a user's password (when forgotten)
    - Changing a user's password (when known)
    - Deleting a user's account
    
Said operations are 'grouped' under the larger routes:
    - '/auth' for authentication and account creation operations
    - '/update' for existing account modification
*/

fun Route.authRoutes() {
    val authService = AuthService() // Initialise AuthService in routes
    val db = DatabaseService() // Initialise databaseService

    /*
    Authentication Routes ('/auth')
    Handles login and account creation requests
    */
    route("/auth") { // If HTTP request in the '/auth' route...

        /*
        '/login' operation is responsible for handling login requests

        Operation expects a HTTP request (in the format of LoginRequest.kt model).
        Operation returns:
            - OK (200) HTTP status code, and relevent message, if credentials are valid
            - NotFound (404) HTTP status code, and relevent message, if credentials are invalid
            - BadRequest (400) HTTP status code, for an invalid HTTP request
        */
        post("/login") { // If HTTP request is a POST of '/login'...
            try {
                val loginRequest = call.receive<LoginRequest>() // Break down the request into usable data, as outlined in LoginRequest

                val isValid =
                    authService.validateLogin(
                        loginRequest.email,
                        loginRequest.password,
                    ) // If authService verifies email and password exist in db

                if (isValid) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Login Successful")) // Response for valid login
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Invalid Credentials")) // Response for invalid login
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request")) // Response for a bad HTTP request made
            }
        }

        /*
        '/create-account' is responsible for handling account creation requests

        Operation expects a HTTP request in the format of CreateAccountRequest model (i.e. contains account details).
        Operation returns:
            - OK (200) with new user's userID is account creation is successful
            - NotAcceptable (406) and relevent error message if user already exists in db
            - BadRequest (400) and relevent error message if provided a malformed HTTP request
        */
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
                    call.respond(
                        HttpStatusCode.NotAcceptable,
                        mapOf("message" to "User already exists!"),
                    ) // Fail message if account not created (as user already exists)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid Request")) // Response if a bad HTTP request is made
            }
        }
    }

    /*
    Update Account ("/update")
    Contains operations related to the updating of account information
    */
    route("/update") {
        
        /*
        Updates user account infromation (excluding password, which is in a seperate endpoint)

        Expects a HTTP request (in the format and order dictated by UpdateAccountRequest model) which contains user fields.
        Fields provided may be null, dependant on what is being update (i.e. if a field is made null, it will be removed)

        The operation returns:
            - OK (200) and a relevent message, if the account is successfully updated
            - NotFound (404) and a relevent message, if the target account doesn't exist
            - BadRequest (400) and a relevent message, if a malformed HTTP request is received
        */
        put("/account") {
            try {
                val body = call.receive<UpdateAccountRequest>() // Receive data payload, and unpack into body based on model

                val updatedInfo =
                    db.updateAccount(
                        body.email,
                        body.gender,
                        body.age,
                        body.height,
                        body.weight,
                    ) // Pass given values, including null fields, to function in service to update account info

                if (updatedInfo == true) {
                    // Response message for successful update to account information
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Account successfully updated"))
                } else {
                    // Response message for no update made (as account not found)
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
            } catch (e: Exception) {
                // Response message for an invalid HTTP request made to backend server
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }

        /*
        Operation deletes a user account based on the email given as a parameter

        Operation expects an email given as a parameter in the path, where 'email' is the email associated
        with an account the user wants to delete.

        Operation returns:
            - OK (200) and a relevant message, if the account deletion is successful
            - NotFound (404) and a relevent message, if the target user doesn't exist
            - BadRequest (400) and a relevent message, if a malformed HTTP request is given
        */
        delete("/account/{email}") {
            try {
                val email = call.parameters["email"]!! // Extract email parameter from route
                val deleted = db.deleteUser(email) // Attempt to delete target user from database

                if (deleted) {
                    // Response message for success (i.e. user is deleted from database)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User successfully deleted"))
                } else {
                    // Response message for unsuccessful request (i.e. user couldn't be deleted from db)
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "User not found"))
                }
            } catch (e: Exception) {
                // Response message for a bad HTTP request made, which backend server cannot handle
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid request"))
            }
        }
    }
}


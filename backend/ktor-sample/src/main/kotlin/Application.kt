package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

// Import routing functions for authorisation requests (i.e. login and create account functions)
import routes.authRoutes
// Import routing functions for retrival of user activity (e.g. for dashboard ect)
import routes.activityRoutes

fun main() {
    embeddedServer(Netty, port = 3000) { // Server with Netty engine on port 3000
        module() // Call func. 'Application.module()'
    }.start(wait = true) // Start server
}


fun Application.module() { // Function for config. of Ktor app.
    install(CORS) { // Install CORS plugin
        anyHost() // Allow requests from any port number
    }
    install(ContentNegotiation) { // Install content negociation plugin
        json() // Automatic JSON handling
    }

    routing{
        get("/"){
            call.respond("")
        }
        get("/health") { // When GET HTTP request made for server health...
            call.respond(mapOf("Status" to "Running")) // Respond with affirmative good health response
        }

        authRoutes() // Add authRoutes to Ktor routing table

        activityRoutes()

    }
}
package com.example

import io.ktor.serialization.kotlinx.json.json // Imports serialisation package for JSON data serialisation
import io.ktor.server.application.Application // Imports server application for server startup
import io.ktor.server.application.install // Installs server add-on installation package
import io.ktor.server.engine.embeddedServer // Imports the server engine for running an embedded server
import io.ktor.server.netty.Netty // Installs server I/O framework
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation // Imports ContentNegotiation for better API I/O flexability
import io.ktor.server.plugins.cors.routing.CORS // Imports ability to allow other servers to make API requests to this server
import io.ktor.server.response.respond // Imports ability to respond to HTTP requests
import io.ktor.server.routing.get // Imports ability to route to GET HTTP requests
import io.ktor.server.routing.routing // Imports routing to server
import routes.activityRoutes // Import routing functions for retrival of user activity (e.g. for dashboard ect)
import routes.authRoutes // Import routing functions for authorisation requests (i.e. login and create account functions)
import routes.socialRoutes // Import routing functions for CRUD operations for training groups and group sessions
import routes.statisticsRoutes // Import routing functions for retrival of statistics, and reporting new statistics

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

    routing {
        get("/") { // When blank request made to no endpoint
            call.respond("") // Respond with nothing
        }
        get("/health") { // When GET HTTP request made for server health...
            call.respond(mapOf("Status" to "Running")) // Respond with affirmative good health response
        }

        authRoutes() // Add authRoutes to Ktor routing table

        activityRoutes() // Add activityRoutes to Ktor routing table

        statisticsRoutes() // Add statisticsRoutes to Ktor routing table

        socialRoutes() // Add socialRoutes to Ktor routing table
    }
}


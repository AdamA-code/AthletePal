package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*


fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureRouting()
}

fun main() {
    embeddedServer(Netty, port = 3000) { // Server with Netty engine on port 3000
        routing {
            get("/") { // On get method, with no path ('/')...
                call.respondText("Server Running") // Send confirmation message (shows server running)
            }
        }
    }.start(wait = true) // Start server
}
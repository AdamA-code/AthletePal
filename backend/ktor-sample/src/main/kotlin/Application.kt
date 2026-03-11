package com.example

import io.ktor.server.application.*

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureRouting()
}

fun main() {
    embeddedServer(Netty, port = 3000) { // Server with Netty engine on port 3000
        routing {
            get("/") {
                call.respondText("Server Running") // Send confirmation message when server running
            }
        }
    }.start(wait = True) // Start server
}
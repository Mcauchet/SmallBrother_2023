package com.example.plugins

import com.example.models.ServerSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*

fun Application.configureSession() {
    install(Sessions) {
        cookie<ServerSession>("server_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 300
        }
    }
}

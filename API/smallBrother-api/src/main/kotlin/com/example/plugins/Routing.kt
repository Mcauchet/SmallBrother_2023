package com.example.plugins

import com.example.routes.aideDataRouting
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import java.io.File

fun Application.configureRouting() {
    routing {
        aideDataRouting()
    }
}

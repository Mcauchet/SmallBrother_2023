package com.example.plugins

import com.example.routes.adminRouting
import com.example.routes.aideDataRouting
import io.ktor.server.routing.*
import io.ktor.server.application.*

/**
 * Manages all the routing by calling routing functions defined in routes directory
 *
 * @author Maxime Caucheteur
 * @version 1 (Updated on 26-12-22)
 */
fun Application.configureRouting() {
    routing {
        aideDataRouting()
        adminRouting()
    }
}

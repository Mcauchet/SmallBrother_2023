package com.example.plugins

import com.example.routes.adminRouting
import com.example.routes.aideDataRouting
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import java.io.File

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
        static("/root/smallbrother/") {
            staticRootFolder = File("resources")
            default("templates/index.ftl")
            static("assets") {
                files("css")
            }
            static("images") {
                file("ktor_logo.png")
            }
        }
    }
}

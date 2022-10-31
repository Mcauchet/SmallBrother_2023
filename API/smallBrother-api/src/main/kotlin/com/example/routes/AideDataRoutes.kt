package com.example.routes

import com.example.models.AideData
import com.example.models.aideDataStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

//Currently synchronous
fun Route.aideDataRouting() {
    route("/aideData") {
        get {
            if(aideDataStorage.isNotEmpty()) {
                call.respond(aideDataStorage)
            } else {
                call.respondText("No Aide Data found", status = HttpStatusCode.OK)
            }
        }
        get("{key?}") {
            val key = call.parameters["key"] ?: return@get call.respondText(
                "Missing key",
                status = HttpStatusCode.BadRequest
            )
            val aideData = aideDataStorage.find { it.key == key } ?: return@get call.respondText(
                "No aide data with key $key",
                status = HttpStatusCode.NotFound
            )
            call.respond(aideData)
        }
        post {
            val aideData = call.receive<AideData>()
            aideDataStorage.add(aideData)
            call.respondText("AideData stored correctly", status = HttpStatusCode.Created)
        }
        delete("{key?}") {
            val key = call.parameters["key"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (aideDataStorage.removeIf {it.key == key}) {
                call.respondText("Aide Data removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
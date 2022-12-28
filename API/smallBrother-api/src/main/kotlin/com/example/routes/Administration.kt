package com.example.routes

import com.example.dao.dao
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.io.File

/**
 * manages the routing for the admin panel
 *
 * @author Maxime Caucheteur
 * @version 1 (Updated on 26-12-22)
 */
fun Route.adminRouting() {
    route("/admin") {
        get {
            call.respond(FreeMarkerContent("index.ftl", mapOf("aideDatas" to dao.allAideData())))
        }
        get("{uri}") {
            //get specific data in db
            val uri = call.parameters.getOrFail<String>("uri")
            call.respond(FreeMarkerContent("show.ftl", mapOf("aideData" to dao.getAideData(uri))))
        }
        post("{uri}") {
            //delete selected entry
            val uri = call.parameters.getOrFail("uri")
            val formParameters = call.receiveParameters()
            if(formParameters.getOrFail("_action") == "Delete") {
                dao.deleteAideData(uri)
                val file = File("upload/$uri")
                file.delete()
                call.respondRedirect("/admin")
            }
        }
        post("/clean") {
            val formParameters = call.receiveParameters()
            if(formParameters.getOrFail("_action") == "Clean") {
                val existingEntries = dao.allAideData()
                val existingUris: MutableList<String> = mutableListOf()
                for (entry in existingEntries) {
                    existingUris += entry.uri
                }
                val dir = object {}.javaClass.getResource("upload")?.file?.let { it1 -> File(it1) }
                dir?.walk()?.forEach { file ->
                    if(file.name !in existingUris) {
                        file.delete()
                    }
                }
            }
            call.respondRedirect("/admin")
        }
    }
}
package com.example.routes

import com.example.dao.dao
import com.example.models.AideData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/***
 * manages all the routes to access aide datas
 */
fun Route.aideDataRouting() {
    route("/aideData") {

        get {
            val aideDatas: List<AideData> = dao.allAideData()
            if(aideDatas.isEmpty()) call.respondText("Database Empty", status = HttpStatusCode.NoContent)
            else call.respond(aideDatas)
        }

        get("/{key?}") {
            val key = call.parameters["key"]
                ?: return@get call.respondText("Aide Data key not valid", status = HttpStatusCode.NotFound)
            val aideData: AideData? = dao.getAideData(key)
            if (aideData != null) {
                call.respond(aideData)
            } else {
                call.respondText("Aide Data not in database", status = HttpStatusCode.NotFound)
            }
        }

        post {
            val aideData = call.receive<AideData>()
            dao.addAideData(aideData)
            call.respondText("AideData stored correctly", status = HttpStatusCode.Created)
        }

        post("/delete/{key?}") {
            val key = call.parameters["key"]
                ?: return@post call.respondText("Aide Data key not valid", status = HttpStatusCode.NotFound)
            dao.deleteAideData(key)
            call.respondText("AideData deleted successfully", status = HttpStatusCode.Accepted)
        }

        post("delete/") {
            dao.deleteAideDatas()
            call.respondText("Database reset", status = HttpStatusCode.Accepted)
        }

    }
}
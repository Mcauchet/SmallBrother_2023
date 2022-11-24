package com.example.routes

import com.example.dao.dao
import com.example.models.AideData
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

/***
 * manages the upload and download of aide's files
 *
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 24-11-2022)
 */
fun Route.aideDataRouting() {
    route("/upload") {
        var fileDescription = ""
        var fileName = ""
        post {
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }
                    is PartData.FileItem -> {
                        fileName = part.originalFileName as String
                        val fileBytes = part.streamProvider().readBytes()
                        File("upload/$fileName").writeBytes(fileBytes)
                    }
                    else -> {}
                }
                part.dispose()
            }
            call.respondText("$fileDescription is uploaded to 'upload/$fileName'")
        }
    }

    route("/download") {
        get("/{key}"){
            //TODO (download files for Aidant)
            val key = call.parameters["key"]
            val file = File("upload/$key.zip")
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName, "$key.zip"
                ).toString()
            )
            call.respondFile(file)
        }
    }

    //TODO delete this if no use found
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
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

const val MAX_SIZE = 2000000 //2MB
const val NAME_SIZE = 25

/***
 * manages the upload and download of aide's files
 *
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 14-01-2023)
 */
fun Route.aideDataRouting() {
    route("/upload") {
        var fileDescription = ""
        var fileName = ""
        post {
            val multipartData = call.receiveMultipart()
            val contentType = call.request.headers["Content-Type"] // todo check this
                ?: return@post call.respondText("Content-Type not found", status = HttpStatusCode.NotFound)
            if(!contentType.contains("application/zip"))
                return@post call.respondText("Format not valid", status = HttpStatusCode.Unauthorized)

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> fileDescription = part.value
                    is PartData.FileItem -> {
                        val extension = part.originalFileName?.substringAfterLast(".")
                        fileName = part.originalFileName as String
                        val fileBytes = part.streamProvider().readBytes()
                        if (extension == "zip" && fileBytes.size < MAX_SIZE && fileName.length == NAME_SIZE) { // todo check this
                            File("upload/$fileName").writeBytes(fileBytes)
                        } else {
                            call.respondText("Only small zip file accepted", status = HttpStatusCode.NotAcceptable)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }
            call.respondText("$fileDescription is uploaded to 'upload/$fileName'")
        }
        post("/aes") {
            if (call.request.headers["Content-Type"] != "application/json") {
                call.respondText("Format not valid", status = HttpStatusCode.Unauthorized) // todo check this
            }
            val aideData = call.receive<AideData>()
            dao.addAideData(aideData)
            call.respondText("AideData stored correctly", status = HttpStatusCode.Created)
        }
    }

    route("/aes") {
        get("/{key}") {
            val key = call.parameters["key"]
                ?: return@get call.respondText("uri not valid", status = HttpStatusCode.NotFound)
            val aideData = dao.getAideData(key)
            if (aideData != null) call.respond(aideData.aesKey)
            else call.respondText("AES key not found", status = HttpStatusCode.NotFound)
        }
    }

    route("/sign") {
        get("/{key}") {
            val key = call.parameters["key"]
                ?: return@get call.respondText("uri not valid", status = HttpStatusCode.NotFound)
            val aideData = dao.getAideData(key)
            if(aideData != null) call.respond(aideData.signature)
            else call.respondText("Signature not found", status = HttpStatusCode.NotFound)
        }
    }

    route("/download") {
        get("/{key}"){
            val key = call.parameters["key"]
            val file = File("upload/$key")
            val aideData = dao.getAideData("$key")
            if (aideData != null) {
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName, aideData.uri
                    ).toString()
                )
            }
            call.respondFile(file)
        }
    }

    //TODO delete this if no use found
    /*route("/aideData") {

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

    }*/
}
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

const val MAX_SIZE = 10000000 //10MB limit for files
const val NAME_SIZE = 25
const val EXT_SIZE = 4

/**
 * Manages the upload and download of aide's files
 *
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 09-05-2023)
 */
fun Route.aideDataRouting() {
    route("/upload") {
        if(!File("/upload/").exists()) File("/upload/").mkdir()
        var fileDescription = ""
        var fileName = ""
        post {
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> fileDescription = part.value
                    is PartData.FileItem -> {
                        if(part.headers["Content-Type"] != "application/zip")
                            call.respondText("Format not valid", status = HttpStatusCode.Unauthorized)
                        val extension = part.originalFileName?.substringAfterLast(".")
                        fileName = part.originalFileName as String
                        val fileBytes = part.streamProvider().readBytes()
                        if (extension == "zip" && fileBytes.size < MAX_SIZE && fileName.length == NAME_SIZE+EXT_SIZE) {
                            File("/upload/$fileName").writeBytes(fileBytes)
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
        post("/aideData") {
            if (call.request.headers["Content-Type"] != "application/json") {
                call.respondText("Format not valid", status = HttpStatusCode.Unauthorized)
            }
            val aideData = call.receive<AideData>()
            dao.addAideData(aideData)
            call.respondText("AideData stored correctly", status = HttpStatusCode.Created)
        }
    }

    route("/download") {
        get("/{key}"){
            val key = call.parameters["key"]
            val file = File("/upload/$key")
            val aideData = dao.getAideData("$key")
            if (aideData != null) {
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName,
                        aideData.uri
                    ).toString()
                )
            }
            call.respondFile(file)
        }
    }

    route("/aideData") {
        get("/{key}") {
            val key = call.parameters["key"]
                ?: return@get call.respondText("uri not valid", status = HttpStatusCode.NotAcceptable)
            val aideData = dao.getAideData(key)
            if(aideData != null) call.respond(aideData)
            else call.respondText("Data not found", status = HttpStatusCode.NotFound)
        }
    }
}
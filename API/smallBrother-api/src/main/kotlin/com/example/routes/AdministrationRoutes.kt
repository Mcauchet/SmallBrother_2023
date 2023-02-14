package com.example.routes

import com.example.dao.dao
import com.example.models.Admin
import com.example.models.ServerSession
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import org.mindrot.jbcrypt.BCrypt
import java.io.File

/**
 * Manages the routing for the admin panel
 *
 * @author Maxime Caucheteur
 * @version 1 (Updated on 16-01-2023)
 */
fun Route.adminRouting() {
    authenticate("auth-session") {
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
                        if(file.name !in existingUris) file.delete()
                    }
                }
                call.respondRedirect("/admin")
            }
            get("/editAdmin") {
                call.respond(FreeMarkerContent("editAdmin.ftl", mapOf("admins" to dao.allAdmin())))
            }
            post("/editAdmin") {
                val formParameters = call.receiveParameters()
                val email = formParameters.getOrFail("email")
                val previousPwd = formParameters.getOrFail("previousPassword")
                val newPassword = formParameters.getOrFail("newPassword")
                val confirmPassword = formParameters.getOrFail("confirmPassword")
                if(newPassword != confirmPassword) {
                    call.respond(FreeMarkerContent("editAdmin.ftl", mapOf("passwordsMatch" to false)))
                }
                val phone = formParameters.getOrFail("phone")
                val dbPwd = dao.getAdmin(email)?.encPwd
                val newAdmin = Admin(email, BCrypt.hashpw(newPassword, BCrypt.gensalt(12)), phone)
                if(dbPwd != null && BCrypt.checkpw(previousPwd, dbPwd)) {
                    dao.addAdmin(newAdmin)
                    call.respondRedirect("/admin")
                } else {
                    call.respond(FreeMarkerContent("editAdmin.ftl", mapOf("confirm" to false)))
                }
            }
        }
        get("/") {
            call.respondRedirect("/admin")
        }
        get("/logout") {
            call.sessions.clear<ServerSession>()
            call.respondRedirect("/login")
        }
    }

    route("/login") {
        get {
            call.respond(FreeMarkerContent("login.ftl", mapOf("admins" to dao.allAdmin())))
        }
        post {
            val formParameters = call.receiveParameters()
            val email = formParameters.getOrFail("email")
            val pwd = formParameters.getOrFail("password")
            val dbPwd = dao.getAdmin(email)?.encPwd
            if(dbPwd != null && BCrypt.checkpw(pwd, dbPwd)) {
                call.sessions.set(ServerSession(email))
                call.respondRedirect("/admin")
            } else {
                call.respond(FreeMarkerContent("login.ftl", mapOf("check" to false)))
            }
        }
    }
}
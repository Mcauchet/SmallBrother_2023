package com.example.plugins

import com.example.dao.dao
import com.example.models.ServerSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import org.mindrot.jbcrypt.BCrypt

fun Application.configureAuthentication() {
    install(Authentication) {
        form("auth-form") {
            userParamName = "email"
            passwordParamName = "password"
            validate { credentials ->
                val dbPwd = dao.getAdmin(credentials.name)?.encPwd
                if (dbPwd != null && BCrypt.checkpw(credentials.password, dbPwd)) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Credentials are not valid")
            }
        }
        session<ServerSession>("auth-session") {
            validate { session ->
                if (session.email == "adminSB@hotmail.com") session
                else null
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }
}
package com.example

import com.example.dao.*
import com.example.models.ServerSession
import com.example.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.event.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function.
// This annotation prevents the IDE from marking it as unused.
fun Application.module() {
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
    install(Sessions) {
        cookie<ServerSession>("server_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 300
        }
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val contentType = call.request.headers["Content-Type"]
            "Status: $status, HttpMethod: $httpMethod, Content-Type: $contentType"
        }
    }
    DatabaseFactory.init(environment.config)
    configureTemplating()
    configureSerialization()
    configureRouting()
}

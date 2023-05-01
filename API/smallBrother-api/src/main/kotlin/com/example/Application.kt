package com.example

import com.example.dao.*
import com.example.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.event.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function.
// This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureAuthentication()
    configureSession()
    configureCallLogging()
    DatabaseFactory.init()
    configureTemplating()
    configureSerialization()
    configureRouting()
}

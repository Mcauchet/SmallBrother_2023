package com.example.plugins

import freemarker.cache.*
import freemarker.core.HTMLOutputFormat
import io.ktor.server.application.*
import io.ktor.server.freemarker.*

fun Application.configureTemplating() {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")

        //prevents XSS attacks https://ktor.io/docs/creating-interactive-website.html#freemarker_config
        outputFormat = HTMLOutputFormat.INSTANCE
    }
}

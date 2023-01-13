package com.example.models

import io.ktor.server.auth.*

data class ServerSession(val email: String): Principal

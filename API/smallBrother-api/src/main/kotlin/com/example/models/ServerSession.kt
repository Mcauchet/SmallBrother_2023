package com.example.models

import io.ktor.server.auth.*

/**
 * Used by the Session plugin for the connection cookie
 * @property email the email of the admin used to log in
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 14-02-2023)
 */
data class ServerSession(val email: String): Principal

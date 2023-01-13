package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*

data class Admin(
    @Serializable
    val email: String,
    @Serializable
    val encPwd: String,
    @Serializable
    val phoneNumber: String,
)


object Admins: Table() {
    val email = varchar("email", 100)
    val encPwd = varchar("pwd", 100)
    val phoneNumber = varchar("phone", 13)

    override val primaryKey = PrimaryKey(email)
}
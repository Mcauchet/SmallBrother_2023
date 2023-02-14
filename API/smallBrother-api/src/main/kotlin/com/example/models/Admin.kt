package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*

/**
 * Represents the admin account to log in the admin panel
 * @property email the email address of the admin
 * @property encPwd the encrypted password of the admin (using Bcrypt)
 * @property phoneNumber the phone number of the admin (empty by default)
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 14-02-2023)
 */
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
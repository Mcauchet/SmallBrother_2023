package com.example.security

import org.mindrot.jbcrypt.BCrypt

fun checkCredentials(password: String, dbPwd: String): Boolean {
    return BCrypt.checkpw(password, dbPwd)
}

fun encrypt(password: String, rounds: Int): String {
    return BCrypt.hashpw(password, BCrypt.gensalt(rounds))
}
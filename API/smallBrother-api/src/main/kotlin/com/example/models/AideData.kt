package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*

@Serializable
data class AideData(
    @Serializable
    val uri: String,
    @Serializable
    val AESKey: String,
)

object AideDatas : Table() {
    private val id = integer("id").autoIncrement()
    val uri = varchar("uri", 128)
    val AESKey = varchar("AESKEY", 2048)

    override val primaryKey = PrimaryKey(id)
}



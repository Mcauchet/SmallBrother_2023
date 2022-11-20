package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*

//TODO edit the data format once we know what we send, etc.
@Serializable
data class AideData(
    @Serializable
    val img1: String,
    @Serializable
    val img2: String,
    @Serializable
    val audio: String,
    @Serializable
    val motion: Boolean,
    @Serializable
    val battery: Int,
    @Serializable
    val key: String,
)

object AideDatas : Table() {
    private val id = integer("id").autoIncrement()
    val img1 = varchar("image1", 128)
    val img2 = varchar("image2", 128)
    val audio = varchar("audio", 128)
    val motion = bool("motion").default(false)
    val battery = integer("battery").default(50)
    val key = varchar("key", 128)

    override val primaryKey = PrimaryKey(id)
}



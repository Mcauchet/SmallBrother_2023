package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime

/***
 * Represents the pair URI/AESKey uploaded to the server by the aidé
 *
 * @property uri the name of the file to retrieve
 * @property aesKey the encrypted AESKey given to the Aidant to decrypt the data
 *
 * @author Maxime Caucheteur
 * @version 1 (Updated on 13-01-2023)
 */
@Serializable
data class AideData(
    @Serializable
    val uri: String,
    @Serializable
    val aesKey: String,
    @Serializable
    val signature: String,
)

object AideDatas : Table() {
    val uri = varchar("uri", 128)
    val aesKey = varchar("aesKEY", 2048)
    val signature = varchar("signature", 2048)
    val createdAt = datetime("date_created")

    override val primaryKey = PrimaryKey(uri)
}



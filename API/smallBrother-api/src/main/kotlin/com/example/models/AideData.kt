package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*

/***
 * Represents the pair URI/AESKey uploaded to the server by the aidé
 *
 * @property uri the name of the file to retrieve
 * @property aesKey the encrypted AESKey given to the Aidant to decrypt the data
 *
 * @author Maxime Caucheteur
 * @version 1 (Updated on 01-12-2022)
 */
@Serializable
data class AideData(
    @Serializable
    val uri: String,
    @Serializable
    val aesKey: String,
)

object AideDatas : Table() {
    private val id = integer("id").autoIncrement()
    val uri = varchar("uri", 128)
    val aesKey = varchar("aesKEY", 2048)

    override val primaryKey = PrimaryKey(id)
}



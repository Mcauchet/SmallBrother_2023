package com.projet.sluca.smallbrother.models

import kotlinx.serialization.Serializable

/**
 * Represents the information linked to a context capture file
 * @property uri the uri of the file on the server
 * @property aesKey the encrypted aes key used to decrypt the file
 * @property signature the signature of the file to assess the origin of the file
 * @property iv the initialization vector used for aes encryption/decryption
 * @author Maxime Caucheteur
 * @version 1.2 (Updated on 13-03-2023)
 */
@Serializable
data class AideData(
    @Serializable
    val uri: String,
    @Serializable
    val aesKey: String,
    @Serializable
    val signature: String,
    @Serializable
    val iv: String,
)
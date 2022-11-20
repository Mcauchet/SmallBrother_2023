package com.projet.sluca.smallbrother.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO change parameters to ByteArray (pbly) because JSON doesn't support Image, ...
@Serializable
data class AideData(
    @SerialName("img1")
    val img1: String,
    @SerialName("img2")
    val img2: String,
    @SerialName("audio")
    val audio: String,
    @SerialName("motion")
    val motion: Boolean,
    @SerialName("battery")
    val battery: Int,
    @SerialName("key")
    val key: String,
)

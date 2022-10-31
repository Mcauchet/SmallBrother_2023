package com.projet.sluca.smallbrother

import android.media.Image
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO change parameters to ByteArray (pbly) because JSON doesn't support Image, ...
@Serializable
data class AideData(
    @SerialName("img1")
    val img1: Image,
    @SerialName("img2")
    val img2: Image,
    @SerialName("motion")
    val motion: Boolean,
    @SerialName("battery")
    val battery: Int,
    @SerialName("key")
    val key: String,
)

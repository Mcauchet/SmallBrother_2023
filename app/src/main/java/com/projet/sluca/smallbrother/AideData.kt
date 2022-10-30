package com.projet.sluca.smallbrother

import android.media.AudioRecord
import android.media.Image

//TODO change parameters to ByteArray (pbly) because JSON doesn't support Image, ...
data class AideData(
    val img1: Image,
    val img2: Image,
    val record: AudioRecord, //vérifier ça
    val motion: Boolean,
    val urlGoogleMap: String?,
    val battery: Int,
)

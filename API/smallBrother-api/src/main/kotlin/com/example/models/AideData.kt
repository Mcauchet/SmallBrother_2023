package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class AideData(
    val img1: String,
    val img2: String,
    val motion: Boolean,
    val battery: Int,
)

val aideDataStorage = mutableListOf<AideData>()
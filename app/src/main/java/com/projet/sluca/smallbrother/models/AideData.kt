package com.projet.sluca.smallbrother.models

import kotlinx.serialization.Serializable

@Serializable
data class AideData(
    @Serializable
    val uri: String,
    @Serializable
    val AESKey: String,
)
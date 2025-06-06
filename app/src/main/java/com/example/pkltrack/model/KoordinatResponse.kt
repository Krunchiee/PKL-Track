package com.example.pkltrack.model

data class KoordinatResponse(
    val success: Boolean,
    val message: String,
    val data: KoordinatData
)

data class KoordinatData(
    val lat: String,
    val lng: String
)
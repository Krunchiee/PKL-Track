package com.example.pkltrack.model

data class ServerTimeResponse(
    val success: Boolean,
    val message: String,
    val data: ServerTimeData
)

data class ServerTimeData(
    val server_time: String,
    val timestamp: Long,
    val timezone: String
)
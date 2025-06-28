package com.example.pkltrack.model

data class NotificationResponse(
    val success: Boolean,
    val message: String,
    val data: List<NotificationItem>
)
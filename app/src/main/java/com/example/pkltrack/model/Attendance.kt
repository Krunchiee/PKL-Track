package com.example.pkltrack.model

data class Attendance(
    val date: String,
    val clockIn: String,
    val clockOut: String,
    val isLate: Boolean
)
package com.example.databricks.model

import java.time.Instant

data class Device(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val status: String,
    val location: String,
    val lastHeartbeat: Instant
)

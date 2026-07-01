package com.example.databricks.model

data class DeviceRequest(
    val deviceName: String,
    val deviceType: String,
    val status: String,
    val location: String
)

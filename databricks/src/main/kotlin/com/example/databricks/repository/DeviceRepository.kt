package com.example.databricks.repository

import com.example.databricks.model.Device

interface DeviceRepository {
    fun findAll(): List<Device>
    fun findById(deviceId: String): Device?
    fun save(device: Device): Device
    fun update(deviceId: String, device: Device): Device?
    fun delete(deviceId: String): Boolean
}

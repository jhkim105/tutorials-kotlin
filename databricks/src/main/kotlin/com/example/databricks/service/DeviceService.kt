package com.example.databricks.service

import com.example.databricks.model.Device
import com.example.databricks.model.DeviceRequest
import com.example.databricks.repository.DeviceRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository
) {
    fun getAllDevices(): List<Device> {
        return deviceRepository.findAll()
    }

    fun getDeviceById(id: String): Device {
        return deviceRepository.findById(id) 
            ?: throw IllegalArgumentException("Device not found with id: $id")
    }

    fun createDevice(request: DeviceRequest): Device {
        val device = Device(
            deviceId = UUID.randomUUID().toString(),
            deviceName = request.deviceName,
            deviceType = request.deviceType,
            status = request.status,
            location = request.location,
            lastHeartbeat = Instant.now()
        )
        return deviceRepository.save(device)
    }

    fun updateDevice(id: String, request: DeviceRequest): Device {
        val existing = getDeviceById(id)
        val updated = existing.copy(
            deviceName = request.deviceName,
            deviceType = request.deviceType,
            status = request.status,
            location = request.location,
            lastHeartbeat = Instant.now()
        )
        return deviceRepository.update(id, updated)
            ?: throw IllegalStateException("Failed to update device with id: $id")
    }

    fun deleteDevice(id: String) {
        getDeviceById(id) // Verify existence
        val deleted = deviceRepository.delete(id)
        if (!deleted) {
            throw IllegalStateException("Failed to delete device with id: $id")
        }
    }
}

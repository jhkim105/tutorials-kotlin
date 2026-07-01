package com.example.databricks.controller

import com.example.databricks.model.Device
import com.example.databricks.model.DeviceRequest
import com.example.databricks.service.DeviceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val deviceService: DeviceService
) {

    @GetMapping
    fun getAllDevices(): List<Device> {
        return deviceService.getAllDevices()
    }

    @GetMapping("/{id}")
    fun getDeviceById(@PathVariable id: String): Device {
        return deviceService.getDeviceById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDevice(@RequestBody request: DeviceRequest): Device {
        return deviceService.createDevice(request)
    }

    @PutMapping("/{id}")
    fun updateDevice(
        @PathVariable id: String,
        @RequestBody request: DeviceRequest
    ): Device {
        return deviceService.updateDevice(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevice(@PathVariable id: String) {
        deviceService.deleteDevice(id)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleNotFound(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (ex.message ?: "Not Found")))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleState(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to (ex.message ?: "Bad Request")))
    }
}

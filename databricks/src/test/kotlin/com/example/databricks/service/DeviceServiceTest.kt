package com.example.databricks.service

import com.example.databricks.model.Device
import com.example.databricks.model.DeviceRequest
import com.example.databricks.repository.DeviceRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class DeviceServiceTest {

    @MockK
    lateinit var deviceRepository: DeviceRepository

    @InjectMockKs
    lateinit var deviceService: DeviceService

    @Test
    fun `getAllDevices should return list from repository`() {
        // Given
        val devices = listOf(
            Device("id-1", "Device 1", "TEMPERATURE", "ONLINE", "Room A", Instant.now())
        )
        every { deviceRepository.findAll() } returns devices

        // When
        val result = deviceService.getAllDevices()

        // Then
        assertEquals(1, result.size)
        assertEquals("Device 1", result[0].deviceName)
        verify(exactly = 1) { deviceRepository.findAll() }
    }

    @Test
    fun `createDevice should save and return new device`() {
        // Given
        val request = DeviceRequest("Device New", "FLOW", "OFFLINE", "Pipe 1")
        every { deviceRepository.save(any()) } answers { firstArg() }

        // When
        val result = deviceService.createDevice(request)

        // Then
        assertNotNull(result.deviceId)
        assertEquals("Device New", result.deviceName)
        assertEquals("FLOW", result.deviceType)
        verify(exactly = 1) { deviceRepository.save(any()) }
    }

    @Test
    fun `deleteDevice should throw if device not found`() {
        // Given
        every { deviceRepository.findById(any()) } returns null

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            deviceService.deleteDevice("invalid-id")
        }
    }
}

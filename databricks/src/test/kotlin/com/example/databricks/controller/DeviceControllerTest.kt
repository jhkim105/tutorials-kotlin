package com.example.databricks.controller

import com.example.databricks.model.Device
import com.example.databricks.model.DeviceRequest
import com.example.databricks.service.DeviceService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant

@WebMvcTest(DeviceController::class)
class DeviceControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var deviceService: DeviceService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `GET all devices should return list`() {
        // Given
        val devices = listOf(
            Device("id-1", "Device 1", "TEMPERATURE", "ONLINE", "Room A", Instant.now())
        )
        every { deviceService.getAllDevices() } returns devices

        // When & Then
        mockMvc.perform(get("/api/devices"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].deviceName").value("Device 1"))

        verify(exactly = 1) { deviceService.getAllDevices() }
    }

    @Test
    fun `POST create device should return 201 created`() {
        // Given
        val request = DeviceRequest("Device New", "FLOW", "OFFLINE", "Pipe 1")
        val created = Device("id-2", "Device New", "FLOW", "OFFLINE", "Pipe 1", Instant.now())
        every { deviceService.createDevice(any()) } returns created

        // When & Then
        mockMvc.perform(post("/api/devices")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.deviceId").value("id-2"))
            .andExpect(jsonPath("$.deviceName").value("Device New"))

        verify(exactly = 1) { deviceService.createDevice(any()) }
    }
}

package com.example.databricks.repository

import com.example.databricks.model.Device
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Repository
@Profile("databricks")
class DatabricksDeviceRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : DeviceRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        Device(
            deviceId = rs.getString("device_id"),
            deviceName = rs.getString("device_name"),
            deviceType = rs.getString("device_type"),
            status = rs.getString("status"),
            location = rs.getString("location"),
            lastHeartbeat = rs.getTimestamp("last_heartbeat")?.toInstant() ?: Instant.now()
        )
    }

    override fun findAll(): List<Device> {
        logger.info { "Fetching all devices from Databricks SQL Warehouse" }
        val sql = "SELECT device_id, device_name, device_type, status, location, last_heartbeat FROM iot_devices ORDER BY last_heartbeat DESC"
        return jdbcTemplate.query(sql, emptyMap<String, Any>(), rowMapper)
    }

    override fun findById(deviceId: String): Device? {
        logger.info { "Fetching device by ID $deviceId from Databricks SQL Warehouse" }
        val sql = "SELECT device_id, device_name, device_type, status, location, last_heartbeat FROM iot_devices WHERE device_id = :deviceId"
        val params = mapOf("deviceId" to deviceId)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    override fun save(device: Device): Device {
        logger.info { "Saving device ${device.deviceId} to Databricks SQL Warehouse" }
        val sql = """
            INSERT INTO iot_devices (device_id, device_name, device_type, status, location, last_heartbeat) 
            VALUES (:deviceId, :deviceName, :deviceType, :status, :location, CAST(:lastHeartbeat AS TIMESTAMP))
        """.trimIndent()
        val params = mapOf(
            "deviceId" to device.deviceId,
            "deviceName" to device.deviceName,
            "deviceType" to device.deviceType,
            "status" to device.status,
            "location" to device.location,
            "lastHeartbeat" to Timestamp.from(device.lastHeartbeat).toString()
        )
        jdbcTemplate.update(sql, params)
        return device
    }

    override fun update(deviceId: String, device: Device): Device? {
        logger.info { "Updating device $deviceId in Databricks SQL Warehouse" }
        val sql = """
            UPDATE iot_devices 
            SET device_name = :deviceName, device_type = :deviceType, status = :status, location = :location, last_heartbeat = CAST(:lastHeartbeat AS TIMESTAMP) 
            WHERE device_id = :deviceId
        """.trimIndent()
        val params = mapOf(
            "deviceId" to deviceId,
            "deviceName" to device.deviceName,
            "deviceType" to device.deviceType,
            "status" to device.status,
            "location" to device.location,
            "lastHeartbeat" to Timestamp.from(device.lastHeartbeat).toString()
        )
        val updatedRows = jdbcTemplate.update(sql, params)
        return if (updatedRows > 0) device else null
    }

    override fun delete(deviceId: String): Boolean {
        logger.info { "Deleting device $deviceId from Databricks SQL Warehouse" }
        val sql = "DELETE FROM iot_devices WHERE device_id = :deviceId"
        val params = mapOf("deviceId" to deviceId)
        return jdbcTemplate.update(sql, params) > 0
    }
}

package com.example.databricks.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Configuration
class DatabaseConfig(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val env: Environment
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val activeProfiles = env.activeProfiles.toList()
        logger.info { "Active Spring Profiles: $activeProfiles" }

        if (activeProfiles.contains("databricks")) {
            logger.info { "Initializing Databricks Database Schema if not exists" }
            try {
                // Databricks Delta Lake SQL table initialization
                val createTableSql = """
                    CREATE TABLE IF NOT EXISTS iot_devices (
                        device_id STRING,
                        device_name STRING,
                        device_type STRING,
                        status STRING,
                        location STRING,
                        last_heartbeat TIMESTAMP
                    ) USING DELTA
                """.trimIndent()
                jdbcTemplate.jdbcTemplate.execute(createTableSql)
                logger.info { "Databricks Delta table check/creation completed successfully" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to initialize table in Databricks. Verify privileges and connection." }
            }
        } else {
            // Local profile initialization
            logger.info { "Initializing local H2 Database Schema" }
            try {
                val createTableSql = """
                    CREATE TABLE IF NOT EXISTS iot_devices (
                        device_id VARCHAR(50) PRIMARY KEY,
                        device_name VARCHAR(100),
                        device_type VARCHAR(50),
                        status VARCHAR(20),
                        location VARCHAR(100),
                        last_heartbeat TIMESTAMP
                    )
                """.trimIndent()
                jdbcTemplate.jdbcTemplate.execute(createTableSql)

                // Check if empty and add mock data
                val count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_devices", emptyMap<String, Any>(), Int::class.java) ?: 0
                if (count == 0) {
                    logger.info { "Inserting default IoT devices mock data into H2" }
                    val insertSql = """
                        INSERT INTO iot_devices (device_id, device_name, device_type, status, location, last_heartbeat) 
                        VALUES (:id, :name, :type, :status, :location, :heartbeat)
                    """.trimIndent()

                    val mockDevices = listOf(
                        mapOf("id" to UUID.randomUUID().toString(), "name" to "Smart-Sensor-Temp-A1", "type" to "TEMPERATURE", "status" to "ONLINE", "location" to "Server Room 1", "heartbeat" to Timestamp.from(Instant.now().minusSeconds(300))),
                        mapOf("id" to UUID.randomUUID().toString(), "name" to "Flow-Meter-B2", "type" to "FLOW", "status" to "MAINTENANCE", "location" to "Water Intake Pipe", "heartbeat" to Timestamp.from(Instant.now().minusSeconds(1200))),
                        mapOf("id" to UUID.randomUUID().toString(), "name" to "Pressure-Valve-C3", "type" to "PRESSURE", "status" to "ONLINE", "location" to "Gas Main Pipeline", "heartbeat" to Timestamp.from(Instant.now().minusSeconds(60))),
                        mapOf("id" to UUID.randomUUID().toString(), "name" to "Hygrometer-D4", "type" to "HUMIDITY", "status" to "OFFLINE", "location" to "Warehouse Hallway C", "heartbeat" to Timestamp.from(Instant.now().minusSeconds(86400)))
                    )

                    mockDevices.forEach { params ->
                        jdbcTemplate.update(insertSql, params)
                    }
                    logger.info { "Mock data initialization complete" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to initialize local H2 database" }
            }
        }
    }
}

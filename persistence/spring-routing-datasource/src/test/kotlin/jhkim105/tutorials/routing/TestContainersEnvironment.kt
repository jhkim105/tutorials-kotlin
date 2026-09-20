package jhkim105.tutorials.routing

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.MySQLContainer
import java.sql.DriverManager

object TestContainersEnvironment {
    private const val DB_IMAGE = "mysql:8.0.33"

    val mysqlContainer: MySQLContainer<*> by lazy {
        MySQLContainer(DB_IMAGE)
            .withDatabaseName("master_db")
            .withUsername("testuser")
            .withPassword("testpass")
            .apply {
                start()
                initDatabasesAndSchemas(jdbcUrl, username, password)
            }
    }

    val masterUrl: String by lazy { mysqlContainer.jdbcUrl }
    val slave1Url: String by lazy { masterUrl.replace("master_db", "slave1_db") }
    val slave2Url: String by lazy { masterUrl.replace("master_db", "slave2_db") }

    private fun initDatabasesAndSchemas(url: String, user: String, pass: String) {
        DriverManager.getConnection(url, user, pass).use { conn ->
            conn.createStatement().use { stmt ->
                // Slave 데이터베이스 생성
                stmt.execute("CREATE DATABASE IF NOT EXISTS slave1_db")
                stmt.execute("CREATE DATABASE IF NOT EXISTS slave2_db")

                // 각 DB에 테이블 스키마 생성
                listOf("master_db", "slave1_db", "slave2_db").forEach { dbName ->
                    stmt.execute("USE $dbName")
                    stmt.execute(
                        """
                        CREATE TABLE IF NOT EXISTS members (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL
                        )
                        """.trimIndent()
                    )
                }
            }
        }
    }

    fun registerProperties(registry: DynamicPropertyRegistry) {
        registry.add("datasource.master.url") { masterUrl }
        registry.add("datasource.master.username") { mysqlContainer.username }
        registry.add("datasource.master.password") { mysqlContainer.password }

        registry.add("datasource.slaves.slave1.url") { slave1Url }
        registry.add("datasource.slaves.slave1.username") { mysqlContainer.username }
        registry.add("datasource.slaves.slave1.password") { mysqlContainer.password }

        registry.add("datasource.slaves.slave2.url") { slave2Url }
        registry.add("datasource.slaves.slave2.username") { mysqlContainer.username }
        registry.add("datasource.slaves.slave2.password") { mysqlContainer.password }
    }
}

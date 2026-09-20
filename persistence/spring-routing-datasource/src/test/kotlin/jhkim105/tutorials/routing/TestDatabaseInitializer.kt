package jhkim105.tutorials.routing

import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestDatabaseInitializer(
    @Qualifier("masterDataSource") private val masterDataSource: HikariDataSource,
    @Qualifier("slaveDataSources") private val slaveDataSources: Map<String, HikariDataSource>,
) {

    @PostConstruct
    fun initSchemas() {
        val createTableSql = """
            CREATE TABLE IF NOT EXISTS members (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
            )
        """.trimIndent()

        masterDataSource.connection.use { conn ->
            conn.createStatement().use { stmt -> stmt.execute(createTableSql) }
        }

        slaveDataSources.values.forEach { slaveDataSource ->
            slaveDataSource.connection.use { conn ->
                conn.createStatement().use { stmt -> stmt.execute(createTableSql) }
            }
        }
    }
}

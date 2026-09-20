package jhkim105.tutorials.routing.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "datasource")
data class DataSourceProperties(
    val master: DataSourcePoolProperties = DataSourcePoolProperties(),
    val slaves: Map<String, DataSourcePoolProperties> = emptyMap(),
)

data class DataSourcePoolProperties(
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val driverClassName: String = "com.mysql.cj.jdbc.Driver",
    val poolName: String = "HikariPool",
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 2,
    val connectionTimeout: Long = 30000,
    val idleTimeout: Long = 600000,
    val maxLifetime: Long = 1800000,
)

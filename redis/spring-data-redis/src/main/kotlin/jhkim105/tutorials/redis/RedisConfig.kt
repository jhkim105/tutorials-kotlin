package jhkim105.tutorials.redis

import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

//@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val clusterNodes = listOf(
            "localhost:7000",
//            "localhost:7001",
//            "localhost:7002",
//            "localhost:7003",
//            "localhost:7004",
//            "localhost:7005"
        )
        val redisClusterConfiguration = RedisClusterConfiguration(clusterNodes)

        val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enablePeriodicRefresh(Duration.ofSeconds(10))
            .enableAllAdaptiveRefreshTriggers()
            .build()

        val clientOptions = ClusterClientOptions.builder()
            .topologyRefreshOptions(topologyRefreshOptions)
            .build()

        val clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(2))
            .clientOptions(clientOptions)
            .build()

        return LettuceConnectionFactory(redisClusterConfiguration, clientConfig)
    }
}

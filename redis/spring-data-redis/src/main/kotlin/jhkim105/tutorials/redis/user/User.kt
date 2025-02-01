package jhkim105.tutorials.redis.user

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.Instant

@RedisHash("User")
class User(
    @Id
    var id: String? = null,
    val username: String,
    val role: Role,
    val createdAt: Instant = Instant.now(),
    @TimeToLive
    val ttl: Long
)

enum class Role {
    ADMIN, USER
}
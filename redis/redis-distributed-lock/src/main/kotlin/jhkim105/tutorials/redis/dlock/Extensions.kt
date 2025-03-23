package jhkim105.tutorials.redis.dlock

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

fun <T> RedissonClient.withLock(
    key: String,
    waitTime: Long = 100,
    leaseTime: Long = 200,
    action: () -> T
): T {
    val lock: RLock = this.getLock(key)
    return runCatching {
        if (lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS)) {
            try {
                action()
            } finally {
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                }
            }
        } else {
            throw IllegalStateException("Could not acquire lock for key: $key")
        }
    }.getOrElse { throw IllegalStateException("Lock execution failed", it) }
}

fun <T> RedissonClient.withLock(
    key: String,
    action: () -> T
): T {
    val lock: RLock = this.getLock(key)
    lock.lock()
    val result = action()
    lock.unlock()
    return result
}
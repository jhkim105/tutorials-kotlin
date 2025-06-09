package jhkim105.tutorials.redis

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.listener.MessageListener
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@Component
class Subscriber : MessageListener<String> {
    override fun onMessage(channel: CharSequence, message: String) {
        val key = "lock-${message.hashCode()}"  // 메시지 기반 고유 키
        val lock = lockCache.get(key) { ReentrantLock() }

        log.info { "Thread=${Thread.currentThread().name}, lock=$lock" }
        if (lock.tryLock()) {
            try {
                log.info { "🔒 Received message: $message, channel: $channel" }
                Thread.sleep(1000)
            } finally {
                lock.unlock()
            }
        } else {
            log.warn { "❗Lock already held, skipping message: $message" }
        }
    }

    companion object {
        private val log = KotlinLogging.logger { }
        // TTL 기반 락 캐시: 10분간 미사용 시 자동 제거, 최대 10,000개
        private val lockCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build<String, ReentrantLock>()
    }

}
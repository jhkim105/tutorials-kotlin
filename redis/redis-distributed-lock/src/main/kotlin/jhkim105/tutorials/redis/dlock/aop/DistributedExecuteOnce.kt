package jhkim105.tutorials.redis.dlock.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedExecuteOnce(val key: String, val leaseTime: Long = 2000)

@Aspect
@Component
class DistributedExecuteOnceAspect(private val redissonClient: RedissonClient) {

    @Around("@annotation(distributedExecuteOnce)")
    fun around(joinPoint: ProceedingJoinPoint, distributedExecuteOnce: DistributedExecuteOnce): Any? {
        val key = KeyExtractor.extractKey(joinPoint, distributedExecuteOnce.key) ?: return joinPoint.proceed()
        val atomicLong = redissonClient.getAtomicLong(key)
        if (atomicLong.compareAndSet(0, 1)) {
            atomicLong.expire(Duration.ofSeconds(distributedExecuteOnce.leaseTime))
            return joinPoint.proceed()
        }
        log.debug("Already locked. key: $key")
        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(DistributedExecuteOnce::class.java)
    }
}
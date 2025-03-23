package jhkim105.tutorials.redis.dlock.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(val key: String, val waitTime: Long = 3000, val leaseTime: Long = 1000)

@Aspect
@Component
class DistributedLockAspect(private val redissonClient: RedissonClient) {

    @Around("@annotation(distributedLock)")
    fun around(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any? {
        val key = KeyExtractor.extractKey(joinPoint, distributedLock.key) ?:  return joinPoint.proceed()

        val rLock: RLock = redissonClient.getLock(key)
        return runCatching {
            if (!rLock.tryLock(distributedLock.waitTime, distributedLock.leaseTime, TimeUnit.MILLISECONDS)) {
                throw IllegalStateException("Could not acquire lock for key: $key")
            }
            joinPoint.proceed()  //
        }.onFailure {
            throw IllegalStateException("Lock execution failed. key: $key", it)
        }.also {
            if (rLock.isHeldByCurrentThread) {
                rLock.unlock()  // 락 해제
            }
        }.getOrThrow()
    }

}
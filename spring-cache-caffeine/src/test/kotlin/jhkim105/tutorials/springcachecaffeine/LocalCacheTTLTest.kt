package jhkim105.tutorials.springcachecaffeine

import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import java.util.concurrent.TimeUnit

@SpringBootTest
class LocalCacheTTLTest {

    @Test
    fun testDifferentCacheNamesWithDifferentTTLs() {
        val cache1Name = CacheConfig.CacheNames.CACHE_1 // 5 seconds TTL
        val localCacheName = CacheConfig.CacheNames.LOCAL_CACHE // 10 seconds TTL
        val testKey = "ttlTestKey"
        
        // Test cache1 (5 seconds TTL)
        val cache1Result1 = LocalCacheAccessor.localCache(cache1Name, testKey) {
            println("[DEBUG_LOG] Function executed for cache1 - first call")
            "cache1-result-${System.currentTimeMillis()}"
        }
        
        val cache1Result2 = LocalCacheAccessor.localCache(cache1Name, testKey) {
            println("[DEBUG_LOG] Function executed for cache1 - second call")
            "cache1-result-${System.currentTimeMillis()}"
        }
        
        // Should be cached (same result)
        assertThat(cache1Result1).isEqualTo(cache1Result2)
        println("[DEBUG_LOG] Cache1 caching verified: $cache1Result1")
        
        // Test localCache (10 seconds TTL)
        val localCacheResult1 = LocalCacheAccessor.localCache(localCacheName, testKey) {
            println("[DEBUG_LOG] Function executed for localCache - first call")
            "localCache-result-${System.currentTimeMillis()}"
        }
        
        val localCacheResult2 = LocalCacheAccessor.localCache(localCacheName, testKey) {
            println("[DEBUG_LOG] Function executed for localCache - second call")
            "localCache-result-${System.currentTimeMillis()}"
        }
        
        // Should be cached (same result)
        assertThat(localCacheResult1).isEqualTo(localCacheResult2)
        println("[DEBUG_LOG] LocalCache caching verified: $localCacheResult1")
        
        // Different cache names should have different values even with same key
        assertThat(cache1Result1).isNotEqualTo(localCacheResult1)
        println("[DEBUG_LOG] Different cache names produce different results as expected")
    }
    
    @Test
    fun testCache1TTLExpiration() {
        val cacheName = CacheConfig.CacheNames.CACHE_1 // 5 seconds TTL
        val testKey = "expirationTestKey"
        
        // First call
        val result1 = LocalCacheAccessor.localCache(cacheName, testKey) {
            println("[DEBUG_LOG] Function executed - first call for TTL test")
            "result-${System.currentTimeMillis()}"
        }
        
        // Second call immediately - should be cached
        val result2 = LocalCacheAccessor.localCache(cacheName, testKey) {
            println("[DEBUG_LOG] Function executed - second call for TTL test")
            "result-${System.currentTimeMillis()}"
        }
        
        assertThat(result1).isEqualTo(result2)
        println("[DEBUG_LOG] Immediate second call returned cached value: $result1")
        
        // Wait for cache to expire (cache1 has 5 seconds TTL)
        println("[DEBUG_LOG] Waiting 6 seconds for cache expiration...")
        TimeUnit.SECONDS.sleep(6)
        
        // Third call after expiration - should execute function again
        val result3 = LocalCacheAccessor.localCache(cacheName, testKey) {
            println("[DEBUG_LOG] Function executed - third call after expiration")
            "result-${System.currentTimeMillis()}"
        }
        
        // Should be different from the first result (cache expired)
        assertThat(result1).isNotEqualTo(result3)
        println("[DEBUG_LOG] After expiration, new value generated: $result3")
    }
}
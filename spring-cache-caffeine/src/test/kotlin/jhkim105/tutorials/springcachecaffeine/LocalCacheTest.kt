package jhkim105.tutorials.springcachecaffeine

import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
class LocalCacheTest {

    @Test
    fun localCacheTest() {
        val results = mutableSetOf<String>()
        val cacheName = CacheConfig.CacheNames.LOCAL_CACHE
        val cacheKey = "testKey"
        
        // First call - should execute function and cache result
        val result1 = LocalCacheAccessor.getOrPut(cacheName, cacheKey) {
            println("[DEBUG_LOG] Function executed - first call")
            "result-${System.currentTimeMillis()}"
        }
        results.add(result1)
        
        // Second call - should return cached result (same value)
        val result2 = LocalCacheAccessor.getOrPut(cacheName, cacheKey) {
            println("[DEBUG_LOG] Function executed - second call")
            "result-${System.currentTimeMillis()}"
        }
        results.add(result2)
        
        // Third call - should return cached result (same value)
        val result3 = LocalCacheAccessor.getOrPut(cacheName, cacheKey) {
            println("[DEBUG_LOG] Function executed - third call")
            "result-${System.currentTimeMillis()}"
        }
        results.add(result3)
        
        // All results should be the same (cached)
        assertThat(results).hasSize(1)
        assertThat(result1).isEqualTo(result2).isEqualTo(result3)
        
        println("[DEBUG_LOG] Test completed successfully. Cached value: $result1")
    }
    
    @Test
    fun localCacheWithDifferentKeysTest() {
        val cacheName = CacheConfig.CacheNames.LOCAL_CACHE
        
        val result1 = LocalCacheAccessor.getOrPut(cacheName, "key1") {
            println("[DEBUG_LOG] Function executed for key1")
            "value1"
        }
        
        val result2 = LocalCacheAccessor.getOrPut(cacheName, "key2") {
            println("[DEBUG_LOG] Function executed for key2")
            "value2"
        }
        
        // Different keys should have different values
        assertThat(result1).isEqualTo("value1")
        assertThat(result2).isEqualTo("value2")
        assertThat(result1).isNotEqualTo(result2)
        
        println("[DEBUG_LOG] Different keys test completed successfully")
    }
}
package jhkim105.tutorials.kotlin.vt

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

private val virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()

class AsyncScope {
    private val _futures = mutableListOf<Future<*>>()
    private val cancelled = AtomicBoolean(false)

    val futures: List<Future<*>> get() = _futures

    fun <T> async(action: () -> T): Future<T> {
        val future = virtualExecutor.submit(Callable(action))
        _futures += future
        return future
    }

    fun asyncUnit(action: () -> Unit): Future<Unit> = async(action)

    fun joinAll() {
        _futures.forEach { it.get() }
    }

    fun joinAllCatching(): List<Result<Any?>> {
        return _futures.map { future ->
            runCatching { future.get() }
        }
    }

    fun cancelAll() {
        if (cancelled.compareAndSet(false, true)) {
            _futures.forEach { it.cancel(true) }
        }
    }

    fun <T> awaitAll(): List<Result<T>> {
        return _futures.map {
            @Suppress("UNCHECKED_CAST")
            runCatching { it.get() as T }
        }
    }

    fun awaitAllUnit(): List<Result<Unit>> = awaitAll()
}

fun <T> awaitAll(block: AsyncScope.() -> Unit): List<Result<T>> {
    val scope = AsyncScope()
    scope.block()
    return scope.awaitAll()
}

fun awaitAllUnit(block: AsyncScope.() -> Unit): List<Result<Unit>> {
    val scope = AsyncScope()
    scope.block()
    return scope.awaitAllUnit()
}

fun joinAll(block: AsyncScope.() -> Unit) {
    val scope = AsyncScope()
    scope.block()
    scope.joinAll()
}

fun joinAllCatching(block: AsyncScope.() -> Unit): List<Result<Any?>> {
    val scope = AsyncScope()
    scope.block()
    return scope.joinAllCatching()
}

fun <T> awaitAllWithTimeout(timeoutMillis: Long, block: AsyncScope.() -> Unit): List<Result<T>> {
    val scope = AsyncScope()
    val executor = Executors.newSingleThreadExecutor()

    val future = executor.submit<List<Result<T>>> {
        scope.block()
        scope.awaitAll()
    }

    return try {
        future.get(timeoutMillis, TimeUnit.MILLISECONDS)
    } catch (e: TimeoutException) {
        scope.cancelAll()
        List(scope.futures.size) { Result.failure<T>(e) }
    } finally {
        executor.shutdownNow()
    }
}
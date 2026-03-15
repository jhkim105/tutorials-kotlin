@file:OptIn(ExperimentalCoroutinesApi::class)

package jhkim105.coroutines.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File


fun main() = runBlocking {
    println("▶ main thread=${Thread.currentThread().name}")

    val data = parseData()        // CPU 풀에서 실행됨
    saveToFile(data)              // IO 풀에서 실행됨

    println("▶ 완료 thread=${Thread.currentThread().name}")
}

suspend fun parseData(): String {
    // CPU 바운드 작업 (Default)
    return withContext(Dispatchers.Default) {
        println("▶ [parseData] thread=${Thread.currentThread().name}")
        // 무거운 연산 시뮬레이션
        (1..3_000_000).sum().toString()
    }
}

suspend fun saveToFile(data: String) {
    // IO 바운드 작업 (IO)
    withContext(Dispatchers.IO) {
        println("▶ [saveToFile] thread=${Thread.currentThread().name}, data=$data")
        File("output.txt").writeText("저장된 데이터 길이=${data.length}")
    }
}


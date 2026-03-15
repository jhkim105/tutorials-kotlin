package jhkim105.coroutines.coroutines

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

fun main() = runBlocking {
    println("작업 시작")

    try {
        withTimeout(2000L) { // 2초 안에 끝나야 함
            repeat(10) { i ->
                println("Step $i")
                delay(500L) // 0.5초 대기
            }
        }
        println("작업 완료")
    } catch (e: TimeoutCancellationException) {
        println("작업이 타임아웃으로 취소되었습니다.")
    }

    println("프로그램 종료")
}
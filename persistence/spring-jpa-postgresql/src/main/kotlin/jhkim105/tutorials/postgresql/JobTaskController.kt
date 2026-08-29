package jhkim105.tutorials.postgresql

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

data class ClaimTaskResponse(
    val success: Boolean,
    val message: String,
    val task: JobTask?
)

@RestController
@RequestMapping("/api/jobs")
class JobTaskController(
    private val jobTaskRepository: JobTaskRepository,
    private val jobTaskService: JobTaskService
) {

    /**
     * 1. 테스트용 대기 작업 생성 (Seed)
     */
    @PostMapping("/seed")
    @Transactional
    fun seedTasks(
        @RequestParam(defaultValue = "20") count: Int
    ): ResponseEntity<Map<String, Any>> {
        val tasks = (1..count).map { i ->
            JobTask(
                taskName = "JobTask-$i",
                status = TaskStatus.PENDING
            )
        }
        val saved = jobTaskRepository.saveAll(tasks)
        log.info { "Seeded ${saved.size} pending tasks" }
        return ResponseEntity.ok(
            mapOf(
                "createdCount" to saved.size,
                "totalPendingCount" to jobTaskRepository.count()
            )
        )
    }

    /**
     * 2. SKIP LOCKED를 활용한 동시 작업 선점 API
     * 여러 워커(클라이언트)가 동시에 요청해도 락 대기 없이 서로 다른 작업을 즉시 반환받습니다.
     */
    @PostMapping("/claim")
    fun claimTask(
        @RequestParam(defaultValue = "worker-1") workerName: String
    ): ResponseEntity<ClaimTaskResponse> {
        val task = jobTaskService.claimNextTask(workerName)
        return if (task != null) {
            log.info { "[$workerName] successfully claimed task ID: ${task.id}" }
            ResponseEntity.ok(
                ClaimTaskResponse(
                    success = true,
                    message = "Task successfully claimed by $workerName",
                    task = task
                )
            )
        } else {
            log.info { "[$workerName] No pending tasks available to claim" }
            ResponseEntity.ok(
                ClaimTaskResponse(
                    success = false,
                    message = "No pending task available",
                    task = null
                )
            )
        }
    }

    /**
     * 3. 현재 작업 상태 요약 조회
     */
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Map<String, Any>> {
        val allTasks = jobTaskRepository.findAll()
        val countByStatus = allTasks.groupBy { it.status }.mapValues { it.value.size }
        return ResponseEntity.ok(
            mapOf(
                "totalCount" to allTasks.size,
                "statusSummary" to countByStatus
            )
        )
    }
}

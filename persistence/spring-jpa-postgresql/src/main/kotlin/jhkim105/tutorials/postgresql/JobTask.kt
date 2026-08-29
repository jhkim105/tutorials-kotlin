package jhkim105.tutorials.postgresql

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

enum class TaskStatus {
    PENDING,
    PROCESSING,
    COMPLETED
}

@Entity
@Table(name = "job_tasks")
class JobTask(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_task_seq")
    @SequenceGenerator(name = "job_task_seq", sequenceName = "job_task_sequence", allocationSize = 50)
    val id: Long? = null,

    @Column(nullable = false)
    val taskName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TaskStatus = TaskStatus.PENDING
)

interface JobTaskRepository : JpaRepository<JobTask, Long> {

    /**
     * PostgreSQL 'FOR UPDATE SKIP LOCKED' enables high-concurrency worker polling.
     * Multiple workers can fetch un-locked pending tasks simultaneously without waiting or deadlocks.
     */
    @Query(
        value = """
            SELECT * FROM job_tasks
            WHERE status = 'PENDING'
            ORDER BY id ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    fun findNextPendingTaskForUpdateSkipLocked(): JobTask?
}

@Service
class JobTaskService(
    private val jobTaskRepository: JobTaskRepository
) {

    @Transactional
    fun claimNextTask(workerName: String): JobTask? {
        val task = jobTaskRepository.findNextPendingTaskForUpdateSkipLocked() ?: return null
        task.status = TaskStatus.PROCESSING
        return jobTaskRepository.save(task)
    }
}

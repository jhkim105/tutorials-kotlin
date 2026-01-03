package com.example.scheduler.adapters.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskJpaRepository : JpaRepository<TaskEntity, String> {
    fun findByTaskId(taskId: String): TaskEntity?
    fun findAllByOrderByTaskIdAsc(): List<TaskEntity>
}

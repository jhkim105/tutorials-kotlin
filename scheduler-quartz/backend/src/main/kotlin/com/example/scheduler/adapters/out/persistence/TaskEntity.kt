package com.example.scheduler.adapters.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "tasks")
data class TaskEntity(
    @Id
    @Column(name = "task_id", length = 100)
    val taskId: String,

    @Column(name = "name", nullable = false, length = 200)
    val name: String,

    @Column(name = "description", nullable = false, length = 500)
    val description: String
)

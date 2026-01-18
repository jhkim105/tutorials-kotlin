package com.example.scheduler.config

import com.example.scheduler.adapters.out.persistence.ExecutionPersistenceAdapter
import com.example.scheduler.adapters.out.persistence.ExecutionJpaRepository
import com.example.scheduler.adapters.out.persistence.SchedulePersistenceAdapter
import com.example.scheduler.adapters.out.persistence.ScheduleJpaRepository
import com.example.scheduler.core.application.port.`in`.ExecutionQueryUseCase
import com.example.scheduler.core.application.port.`in`.ExecutionUseCase
import com.example.scheduler.core.application.port.`in`.ScheduleExecutionUseCase
import com.example.scheduler.core.application.port.`in`.ScheduleUseCase
import com.example.scheduler.core.application.port.`in`.TaskQueryUseCase
import com.example.scheduler.core.application.port.out.ExecutionRepositoryPort
import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.application.port.out.SchedulerPort
import com.example.scheduler.core.application.port.out.TaskRegistryPort
import com.example.scheduler.core.application.service.ExecutionQueryService
import com.example.scheduler.core.application.service.ExecutionService
import com.example.scheduler.core.application.service.ScheduleCalculator
import com.example.scheduler.core.application.service.ScheduleExecutionService
import com.example.scheduler.core.application.service.ScheduleService
import com.example.scheduler.core.application.service.TaskQueryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId
import java.util.UUID

@Configuration
class AppConfig {
    @Bean
    fun clock(): Clock = Clock.system(ZoneId.of("Asia/Seoul"))

    @Bean
    fun instanceId(): String = UUID.randomUUID().toString()

    @Bean
    fun scheduleCalculator(): ScheduleCalculator = ScheduleCalculator()

    @Bean
    fun scheduleRepository(
        scheduleJpaRepository: ScheduleJpaRepository,
        instanceId: String
    ): ScheduleRepositoryPort {
        return SchedulePersistenceAdapter(scheduleJpaRepository, instanceId)
    }

    @Bean
    fun executionRepository(
        executionJpaRepository: ExecutionJpaRepository,
        instanceId: String
    ): ExecutionRepositoryPort {
        return ExecutionPersistenceAdapter(executionJpaRepository, instanceId)
    }

    @Bean
    fun scheduleUseCase(
        scheduleRepository: ScheduleRepositoryPort,
        taskRegistry: TaskRegistryPort,
        schedulerPort: SchedulerPort,
        clock: Clock,
        calculator: ScheduleCalculator
    ): ScheduleUseCase {
        return ScheduleService(scheduleRepository, taskRegistry, schedulerPort, clock, calculator)
    }

    @Bean
    fun executionUseCase(
        executionRepository: ExecutionRepositoryPort,
        taskRegistry: TaskRegistryPort,
        clock: Clock
    ): ExecutionUseCase {
        return ExecutionService(executionRepository, taskRegistry, clock)
    }

    @Bean
    fun scheduleExecutionUseCase(
        scheduleRepository: ScheduleRepositoryPort,
        executionRepository: ExecutionRepositoryPort,
        taskRegistry: TaskRegistryPort,
        calculator: ScheduleCalculator,
        clock: Clock,
        @Value("\${scheduler.lock.execution-seconds:300}") executionLockSeconds: Long,
        @Value("\${scheduler.lock.schedule-seconds:30}") scheduleLockSeconds: Long,
        @Value("\${scheduler.batch-size:20}") batchSize: Int
    ): ScheduleExecutionUseCase {
        return ScheduleExecutionService(
            scheduleRepository,
            executionRepository,
            taskRegistry,
            calculator,
            clock,
            executionLockSeconds,
            scheduleLockSeconds,
            batchSize
        )
    }

    @Bean
    fun executionQueryUseCase(executionRepository: ExecutionRepositoryPort): ExecutionQueryUseCase {
        return ExecutionQueryService(executionRepository)
    }

    @Bean
    fun taskQueryUseCase(taskRegistry: TaskRegistryPort): TaskQueryUseCase {
        return TaskQueryService(taskRegistry)
    }
}

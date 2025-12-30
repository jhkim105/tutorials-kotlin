package com.example.scheduler.infra.scheduler

import com.example.scheduler.application.port.ExecutionUseCase
import com.example.scheduler.application.port.ScheduleRefresher
import com.example.scheduler.application.port.ScheduleRepositoryPort
import com.example.scheduler.domain.model.DeliverySchedule
import com.example.scheduler.domain.model.ScheduleType
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Component
class TaskScheduleManager(
    private val taskScheduler: TaskScheduler,
    private val executionUseCase: ExecutionUseCase,
    private val scheduleRepository: ScheduleRepositoryPort,
    @Value("\${app.scheduler.poll-interval-ms:10000}") private val pollIntervalMs: Long
) : ScheduleRefresher {
    private val log = KotlinLogging.logger {}
    private val scheduledTasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()
    private var lastCheckedAt: Instant = Instant.now()

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        registerAllEnabled()
    }

    fun registerAllEnabled() {
        scheduleRepository.findAllByEnabledTrue().forEach { registerSchedule(it) }
    }

    override fun refreshSchedule(schedule: DeliverySchedule) {
        val scheduleId = schedule.id ?: return
        cancelSchedule(scheduleId)
        if (schedule.enabled) {
            registerSchedule(schedule)
        }
    }

    fun cancelSchedule(scheduleId: Long) {
        scheduledTasks.remove(scheduleId)?.cancel(false)
    }

    @Scheduled(fixedDelayString = "\${app.scheduler.poll-interval-ms:10000}")
    fun pollUpdatedSchedules() {
        val now = Instant.now()
        val updated = scheduleRepository.findAllByUpdatedAtAfter(lastCheckedAt)
        if (updated.isNotEmpty()) {
            log.info("Detected schedule updates: {}", updated.size)
        }
        updated.forEach { refreshSchedule(it) }
        lastCheckedAt = now
    }

    private fun registerSchedule(schedule: DeliverySchedule) {
        val scheduleId = schedule.id ?: return
        val future = when (schedule.scheduleType) {
            ScheduleType.CRON -> registerCron(schedule)
            ScheduleType.ONCE -> registerOnce(schedule)
        }

        if (future != null) {
            scheduledTasks[scheduleId] = future
        }
    }

    private fun registerCron(schedule: DeliverySchedule): ScheduledFuture<*>? {
        val cron = schedule.cronExpression ?: return null
        val zone = schedule.timezone?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
        val trigger = CronTrigger(cron, zone)

        return taskScheduler.schedule(
            {
                executionUseCase.execute(schedule.id!!, Instant.now())
            },
            trigger
        )
    }

    private fun registerOnce(schedule: DeliverySchedule): ScheduledFuture<*>? {
        val runAt = schedule.runAt ?: return null
        if (runAt.isBefore(Instant.now())) {
            return null
        }

        return taskScheduler.schedule(
            {
                executionUseCase.execute(schedule.id!!, runAt)
                cancelSchedule(schedule.id!!)
            },
            runAt
        )
    }
}

package com.example.scheduler.adapters.`in`.scheduler

import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.application.port.out.SchedulerPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class DynamicScheduleLoader(
        private val scheduleRepository: ScheduleRepositoryPort,
        private val schedulerPort: SchedulerPort
) {

    @EventListener(ApplicationReadyEvent::class)
    fun loadSchedules() {
        log.info { "Loading active schedules..." }
        val schedules = scheduleRepository.findAll().filter { it.enabled }
        schedules.forEach { schedule ->
            try {
                schedulerPort.schedule(schedule)
            } catch (ex: Exception) {
                log.error(ex) { "Failed to load schedule ${schedule.id}" }
            }
        }
        log.info { "Loaded ${schedules.size} schedules." }
    }
}

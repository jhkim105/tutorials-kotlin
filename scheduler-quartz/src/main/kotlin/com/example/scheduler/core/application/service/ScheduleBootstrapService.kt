package com.example.scheduler.core.application.service

import com.example.scheduler.core.application.port.`in`.ScheduleBootstrapUseCase
import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.application.port.out.SchedulerPort
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class ScheduleBootstrapService(
    private val scheduleRepository: ScheduleRepositoryPort,
    private val schedulerPort: SchedulerPort
) : ScheduleBootstrapUseCase {

    override fun syncExistingSchedules() {
        val schedules = scheduleRepository.findAll().filter { it.enabled }
        schedules.forEach { schedule ->
            try {
                schedulerPort.schedule(schedule)
            } catch (ex: Exception) {
                log.error(ex) { "Failed to schedule ${schedule.id}" }
            }
        }
    }
}

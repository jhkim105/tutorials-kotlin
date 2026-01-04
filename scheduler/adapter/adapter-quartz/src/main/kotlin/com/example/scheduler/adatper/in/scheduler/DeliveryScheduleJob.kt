package com.example.scheduler.adatper.`in`.scheduler

import com.example.scheduler.application.port.ExecutionUseCase
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DeliveryScheduleJob(
    private val executionUseCase: ExecutionUseCase
) : Job {
    private val log = KotlinLogging.logger {}

    override fun execute(context: JobExecutionContext) {
        val scheduleId = context.mergedJobDataMap.getLongValue(QuartzScheduleManager.JOB_DATA_SCHEDULE_ID)
        val fireTime = context.fireTime?.toInstant() ?: Instant.now()
        log.info("Quartz firing scheduleId={}, fireTime={}", scheduleId, fireTime)
        executionUseCase.execute(scheduleId, fireTime)
    }
}

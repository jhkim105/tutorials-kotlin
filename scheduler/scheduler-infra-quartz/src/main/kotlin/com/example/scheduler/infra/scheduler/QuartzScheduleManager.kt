package com.example.scheduler.infra.scheduler

import com.example.scheduler.application.port.ScheduleRefresher
import com.example.scheduler.application.port.ScheduleRepositoryPort
import com.example.scheduler.domain.model.DeliverySchedule
import com.example.scheduler.domain.model.ScheduleType
import mu.KotlinLogging
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.util.Date

@Component
class QuartzScheduleManager(
    private val scheduler: Scheduler,
    private val scheduleRepository: ScheduleRepositoryPort
) : ScheduleRefresher {
    private val log = KotlinLogging.logger {}

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        registerAllEnabled()
    }

    fun registerAllEnabled() {
        scheduleRepository.findAllByEnabledTrue().forEach { refreshSchedule(it) }
    }

    override fun refreshSchedule(schedule: DeliverySchedule) {
        val scheduleId = schedule.id ?: return
        if (!schedule.enabled) {
            deleteJob(scheduleId)
            return
        }

        when (schedule.scheduleType) {
            ScheduleType.CRON -> upsertCron(schedule)
            ScheduleType.ONCE -> recreateOnce(schedule)
        }
    }

    private fun upsertCron(schedule: DeliverySchedule) {
        val scheduleId = schedule.id ?: return
        val jobKey = QuartzKeys.jobKey(scheduleId)
        val triggerKey = QuartzKeys.triggerKey(scheduleId)
        val trigger = buildCronTrigger(schedule, triggerKey)

        if (scheduler.checkExists(jobKey)) {
            scheduler.rescheduleJob(triggerKey, trigger)
        } else {
            val jobDetail = buildJobDetail(scheduleId)
            scheduler.scheduleJob(jobDetail, trigger)
        }
    }

    private fun recreateOnce(schedule: DeliverySchedule) {
        val scheduleId = schedule.id ?: return
        deleteJob(scheduleId)

        val runAt = schedule.runAt ?: return
        if (runAt.isBefore(Instant.now())) {
            log.info("Skipping past ONCE schedule. scheduleId={}, runAt={}", scheduleId, runAt)
            return
        }

        val jobDetail = buildJobDetail(scheduleId)
        val trigger = buildOnceTrigger(schedule)
        scheduler.scheduleJob(jobDetail, trigger)
    }

    private fun deleteJob(scheduleId: Long) {
        val jobKey = QuartzKeys.jobKey(scheduleId)
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
        }
    }

    private fun buildJobDetail(scheduleId: Long): JobDetail {
        return JobBuilder.newJob(DeliveryScheduleJob::class.java)
            .withIdentity(QuartzKeys.jobKey(scheduleId))
            .usingJobData(JOB_DATA_SCHEDULE_ID, scheduleId)
            .build()
    }

    private fun buildCronTrigger(schedule: DeliverySchedule, triggerKey: TriggerKey): Trigger {
        val cronExpression = schedule.cronExpression ?: throw IllegalArgumentException("cronExpression required")
        val zone = schedule.timezone?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
        val cronSchedule = CronScheduleBuilder.cronSchedule(cronExpression).inTimeZone(java.util.TimeZone.getTimeZone(zone))

        return TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .withSchedule(cronSchedule)
            .build()
    }

    private fun buildOnceTrigger(schedule: DeliverySchedule): Trigger {
        val runAt = schedule.runAt ?: throw IllegalArgumentException("runAt required")
        return TriggerBuilder.newTrigger()
            .withIdentity(QuartzKeys.triggerKey(schedule.id!!))
            .startAt(Date.from(runAt))
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
            .build()
    }

    companion object {
        const val JOB_DATA_SCHEDULE_ID = "scheduleId"
    }
}

private object QuartzKeys {
    private const val GROUP = "delivery"

    fun jobKey(scheduleId: Long) = org.quartz.JobKey.jobKey("schedule-$scheduleId", GROUP)

    fun triggerKey(scheduleId: Long) = org.quartz.TriggerKey.triggerKey("trigger-$scheduleId", GROUP)
}

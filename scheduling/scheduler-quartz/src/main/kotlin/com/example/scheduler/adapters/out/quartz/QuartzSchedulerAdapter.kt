package com.example.scheduler.adapters.out.quartz

import com.example.scheduler.core.application.port.out.SchedulerPort
import com.example.scheduler.core.domain.model.Schedule
import com.example.scheduler.core.domain.model.ScheduleType
import com.example.scheduler.adapters.out.quartz.QuartzIdentifiers
import io.github.oshai.kotlinlogging.KotlinLogging
import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.ObjectAlreadyExistsException
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

private val log = KotlinLogging.logger {}

@Component
class QuartzSchedulerAdapter(
    private val scheduler: Scheduler,
    @Value("\${scheduler.quartz.job-class:com.example.scheduler.adapters.in.scheduler.quartz.TaskExecutionJob}")
    private val jobClassName: String
) : SchedulerPort {

    private val jobClass: Class<out Job> = resolveJobClass(jobClassName)

    override fun schedule(schedule: Schedule) {
        if (!schedule.enabled) {
            return
        }
        val trigger = buildTrigger(schedule) ?: return
        val jobKey = jobKey(schedule.id)
        val triggerKey = triggerKey(schedule.id)
        if (scheduler.checkExists(jobKey)) {
            scheduler.rescheduleJob(triggerKey, trigger)
            return
        }
        val jobDetail = JobBuilder.newJob(jobClass)
            .withIdentity(jobKey)
            .usingJobData(QuartzIdentifiers.JOB_TASK_ID_KEY, schedule.taskId)
            .build()
        try {
            scheduler.scheduleJob(jobDetail, trigger)
        } catch (ex: ObjectAlreadyExistsException) {
            scheduler.rescheduleJob(triggerKey, trigger)
        }
    }

    override fun reschedule(schedule: Schedule) {
        unschedule(schedule.id)
        schedule(schedule)
    }

    override fun unschedule(scheduleId: String) {
        val jobKey = jobKey(scheduleId)
        val triggerKey = triggerKey(scheduleId)
        if (scheduler.checkExists(jobKey)) {
            scheduler.unscheduleJob(triggerKey)
            scheduler.deleteJob(jobKey)
        }
    }

    private fun buildTrigger(schedule: Schedule): Trigger? {
        val triggerBuilder = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey(schedule.id))
            .forJob(jobKey(schedule.id))

        return when (schedule.scheduleType) {
            ScheduleType.CRON -> {
                val cron = schedule.cronExpression ?: return null
                triggerBuilder
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build()
            }
            ScheduleType.ONCE -> {
                val runAt = schedule.runAt ?: return null
                val now = Instant.now()
                val startAt = Date.from(if (runAt.isBefore(now)) now else runAt)
                triggerBuilder
                    .startAt(startAt)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                    .build()
            }
        }
    }

    private fun jobKey(scheduleId: String) = org.quartz.JobKey(QuartzIdentifiers.SCHEDULE_JOB_PREFIX + scheduleId)

    private fun triggerKey(scheduleId: String) =
        org.quartz.TriggerKey(QuartzIdentifiers.SCHEDULE_TRIGGER_PREFIX + scheduleId)

    private fun resolveJobClass(className: String): Class<out Job> {
        return try {
            Class.forName(className).asSubclass(Job::class.java)
        } catch (ex: Exception) {
            log.error(ex) { "Failed to load Quartz job class: $className" }
            throw IllegalStateException("Invalid Quartz job class: $className", ex)
        }
    }
}

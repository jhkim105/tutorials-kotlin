package com.example.scheduler.adapters.out.quartz

import com.example.scheduler.core.application.port.out.ScheduleRepositoryPort
import com.example.scheduler.core.application.port.out.SchedulerPort
import com.example.scheduler.core.domain.model.Schedule
import com.example.scheduler.core.domain.model.ScheduleType
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.ObjectAlreadyExistsException
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

private val log = KotlinLogging.logger {}

@Component
class QuartzSchedulerAdapter(
    private val scheduler: Scheduler,
    private val scheduleRepository: ScheduleRepositoryPort
) : SchedulerPort {

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
        val jobDetail = JobBuilder.newJob(TaskExecutionJob::class.java)
            .withIdentity(jobKey)
            .usingJobData(JOB_TASK_ID_KEY, schedule.taskId)
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

    @PostConstruct
    fun scheduleExisting() {
        val schedules = scheduleRepository.findAll().filter { it.enabled }
        schedules.forEach { schedule ->
            try {
                schedule(schedule)
            } catch (ex: Exception) {
                log.error(ex) { "Failed to schedule ${schedule.id}" }
            }
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

    private fun jobKey(scheduleId: String) = org.quartz.JobKey(SCHEDULE_JOB_PREFIX + scheduleId)

    private fun triggerKey(scheduleId: String) = org.quartz.TriggerKey(SCHEDULE_TRIGGER_PREFIX + scheduleId)

    companion object {
        const val JOB_TASK_ID_KEY = "taskId"
        const val SCHEDULE_JOB_PREFIX = "schedule-job-"
        const val SCHEDULE_TRIGGER_PREFIX = "schedule-"
    }
}

package com.example.scheduler.adapters.`in`.scheduler

import com.example.scheduler.core.application.port.`in`.ScheduleExecutionUseCase
import com.example.scheduler.adapters.out.quartz.QuartzIdentifiers
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@DisallowConcurrentExecution
@Component
class TaskExecutionJob(
    private val scheduleExecutionUseCase: ScheduleExecutionUseCase
) : Job {

    override fun execute(context: JobExecutionContext) {
        val scheduleId = context.trigger.key.name.removePrefix(QuartzIdentifiers.SCHEDULE_TRIGGER_PREFIX)
        val taskId = context.mergedJobDataMap.getString(QuartzIdentifiers.JOB_TASK_ID_KEY)
        scheduleExecutionUseCase.executeScheduled(scheduleId, taskId)
    }
}

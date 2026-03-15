package com.example.springquartz.controller

import com.example.springquartz.job.SampleJob
import java.util.UUID
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/jobs")
class JobController(private val scheduler: Scheduler) {

    @PostMapping("/schedule")
    fun scheduleJob(@RequestParam name: String, @RequestParam intervalSeconds: Int): String {
        val jobName = if (name.isNotBlank()) name else UUID.randomUUID().toString()
        val jobDetail =
                JobBuilder.newJob(SampleJob::class.java)
                        .withIdentity(jobName, "DYNAMIC_GROUP")
                        .withDescription("Dynamically scheduled job: $jobName")
                        .build()

        val trigger =
                TriggerBuilder.newTrigger()
                        .withIdentity("$jobName-trigger", "DYNAMIC_GROUP")
                        .startNow()
                        .withSchedule(
                                SimpleScheduleBuilder.simpleSchedule()
                                        .withIntervalInSeconds(intervalSeconds)
                                        .repeatForever()
                        )
                        .build()

        scheduler.scheduleJob(jobDetail, trigger)
        return "Job '$jobName' scheduled successfully with interval $intervalSeconds seconds."
    }

    @PostMapping("/reschedule")
    fun rescheduleJob(@RequestParam name: String, @RequestParam intervalSeconds: Int): String {
        val triggerKey = TriggerKey.triggerKey("$name-trigger", "DYNAMIC_GROUP")

        // Check if trigger exists
        if (!scheduler.checkExists(triggerKey)) {
            return "Trigger for Job '$name' not found."
        }

        val newTrigger =
                TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey)
                        .startNow()
                        .withSchedule(
                                SimpleScheduleBuilder.simpleSchedule()
                                        .withIntervalInSeconds(intervalSeconds)
                                        .repeatForever()
                        )
                        .build()

        scheduler.rescheduleJob(triggerKey, newTrigger)
        return "Job '$name' rescheduled successfully with new interval $intervalSeconds seconds."
    }
}

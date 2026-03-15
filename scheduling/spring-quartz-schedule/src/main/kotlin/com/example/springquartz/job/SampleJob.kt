package com.example.springquartz.job

import com.example.springquartz.service.SampleJobService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SampleJob : Job {

    @Autowired
    private lateinit var jobService: SampleJobService

    override fun execute(context: JobExecutionContext) {
        jobService.executeSampleJob()
    }
}

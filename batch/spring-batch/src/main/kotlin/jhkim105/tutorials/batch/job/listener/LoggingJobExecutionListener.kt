package jhkim105.tutorials.batch.job.listener

import jhkim105.tutorials.batch.job.SimpleJobConfig
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.BeforeJob

class LoggingJobExecutionListener {
    companion object {
        private val log = LoggerFactory.getLogger(SimpleJobConfig::class.java)
    }

    @BeforeJob
    fun beforeJobExecution(jobExecution: JobExecution) {
        val startTime = System.currentTimeMillis()
        jobExecution.executionContext.putLong("startTime", startTime)
        log.info("beforeJob: ${jobExecution.jobInstance.jobName} started at $startTime")
    }

    @AfterJob
    fun afterJobExecution(jobExecution: JobExecution) {
        val endTime = System.currentTimeMillis()
        val startTime = jobExecution.executionContext.getLong("startTime", endTime)
        val duration = endTime - startTime
        log.info("afterJob: ${jobExecution.jobInstance.jobName} ended at $endTime")
        log.info("Job execution time: ${duration}ms")
    }
}
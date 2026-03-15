package jhkim105.tutorials.batch.job

import jhkim105.tutorials.batch.job.listener.LoggingJobExecutionListener
import org.slf4j.LoggerFactory
import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@Configuration
class SimpleJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    companion object {
        const val JOB_NAME = "simpleJob"
        private val log = LoggerFactory.getLogger(SimpleJobConfig::class.java)
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .incrementer(RunIdIncrementer())
            .listener(jobExecutionListener())
            .listener(LoggingJobExecutionListener())
            .start(step1(null))
            .build()
    }

    private fun jobExecutionListener(): JobExecutionListener {
        return object : JobExecutionListener {
            override fun beforeJob(jobExecution: JobExecution) {
                log.info("beforeJob")
            }
            override fun afterJob(jobExecution: JobExecution) {
                log.info("afterJob")
            }
        }
    }

    @Bean("${JOB_NAME}_step1")
    @JobScope
    fun step1(@Value("#{jobParameters[requestDate]}") requestDate: LocalDateTime?): Step {
        return StepBuilder("simpleStep", jobRepository)
            .listener(stepExecutionListener())
            .tasklet({ _, _ ->
                log.info("hello, world. requestDate: $requestDate")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    private fun stepExecutionListener(): StepExecutionListener {
        return object : StepExecutionListener {
            override fun beforeStep(stepExecution: StepExecution) {
                log.info("beforeStep")
            }

            override fun afterStep(stepExecution: StepExecution): ExitStatus? {
                log.info("afterStep")
                return super.afterStep(stepExecution)
            }
        }
    }


}
package jhkim105.tutorials.batch.adapter.`in`.job

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
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

@Configuration
class SimpleJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    companion object {
        const val JOB_NAME = "simple-job"
        private val log = LoggerFactory.getLogger(SimpleJobConfig::class.java)
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(step1(null))
            .build()
    }

    @Bean("${JOB_NAME}_step1")
    @JobScope
    fun step1(@Value("#{jobParameters[requestDate]}") requestDate: String?): Step {
        return StepBuilder("simpleStep", jobRepository)
            .tasklet({ _, _ ->
                log.info("hello, world. requestDate: $requestDate")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }


}
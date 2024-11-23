package jhkim105.tutorials.batch.job

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class StepNextJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    companion object {
        const val JOB_NAME = "step-next-job"
        private val log = LoggerFactory.getLogger(StepNextJobConfig::class.java)
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(step1())
            .next(step2())
            .next(step3())
            .build()
    }

    @Bean("${JOB_NAME}_step1")
    fun step1(): Step {
        return StepBuilder("${JOB_NAME}_step1", jobRepository)
            .tasklet({ _, _ ->
                log.info("step2")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean("${JOB_NAME}_step2")
    fun step2(): Step {
        return StepBuilder("${JOB_NAME}_step2", jobRepository)
            .tasklet({ _, _ ->
                log.info("step2")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean("${JOB_NAME}_step3")
    fun step3(): Step {
        return StepBuilder("${JOB_NAME}_step3", jobRepository)
            .tasklet({ _, _ ->
                log.info("step3")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }


}
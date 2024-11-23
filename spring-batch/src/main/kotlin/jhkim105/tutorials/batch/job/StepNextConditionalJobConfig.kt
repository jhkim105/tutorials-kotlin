package jhkim105.tutorials.batch.job

import jhkim105.tutorials.batch.job.StepNextConditionalJobConfig.Companion.JOB_NAME
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
@ConditionalOnProperty(name = ["job.name"], havingValue = JOB_NAME)
class StepNextConditionalJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    companion object {
        const val JOB_NAME = "step-next-conditional-job"
        private val log = LoggerFactory.getLogger(StepNextConditionalJobConfig::class.java)
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            // step1 실행 후 FAILED 일 경우 step3 실행하고 flow 종로하기
            .start(step1())
                .on("FAILED")
                .to(step3())
                .on("*")
                .end()
            // step1 실행 후 step2 -> step3 실행 후 종료
            .from(step1())
                .on("*")
                .to(step2())
                .next(step3())
                .on("*")
                .end()
            .end()
            .build();
    }

    @Bean("${JOB_NAME}_step1")
    fun step1(): Step {
        return StepBuilder("${JOB_NAME}_step1", jobRepository)
            .tasklet({ contribution, _ ->
                log.info("step2")
                contribution.exitStatus = ExitStatus.FAILED
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
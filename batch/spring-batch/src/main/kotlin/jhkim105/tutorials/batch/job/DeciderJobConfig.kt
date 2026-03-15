package jhkim105.tutorials.batch.job

import jhkim105.tutorials.batch.job.DeciderJobConfig.OddDecider.Companion.EVEN
import jhkim105.tutorials.batch.job.DeciderJobConfig.OddDecider.Companion.ODD
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import kotlin.random.Random

@Configuration
class DeciderJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    companion object {
        const val JOB_NAME = "deciderJob"
        private val log = LoggerFactory.getLogger(DeciderJobConfig::class.java)
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        // @formatter:off
        return JobBuilder(JOB_NAME, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(startStep())
            .next(decider())
            .from(decider())
                .on(OddDecider.ODD)
                .to(oddStep())
            .from(decider())
                .on(OddDecider.EVEN)
                .to(evenStep())
            .end()
            .build()
        // @formatter:on
    }

    @Bean("${JOB_NAME}_startStep")
    fun startStep(): Step {
        return StepBuilder("${JOB_NAME}_startStep", jobRepository)
            .tasklet({ _, chunkContext ->
                log.info("startStep")
                val randomNumber = Random.nextInt(1, 101) // 1부터 100까지의 랜덤 숫자 생성
                log.info("number: $randomNumber")
                if (randomNumber % 2 == 0) {
                    chunkContext.stepContext.stepExecution.jobExecution.executionContext.putString(OddDecider.STATUS, EVEN)
                } else {
                    chunkContext.stepContext.stepExecution.jobExecution.executionContext.putString(OddDecider.STATUS, ODD)
                }

                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean("${JOB_NAME}_oddStep")
    fun oddStep(): Step {
        return StepBuilder("${JOB_NAME}_oddStep", jobRepository)
            .tasklet({ _, _ ->
                log.info("oddStep")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean("${JOB_NAME}_evenStep")
    fun evenStep(): Step {
        return StepBuilder("${JOB_NAME}_evenStep", jobRepository)
            .tasklet({ _, _ ->
                log.info("evenStep")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }


    @Bean
    fun decider(): JobExecutionDecider {
        return OddDecider()
    }

    class OddDecider : JobExecutionDecider {
        companion object {
            const val STATUS = "STATUS"
            const val ODD = "ODD"
            const val EVEN = "EVEN"
        }

        override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
            val status = jobExecution.executionContext.getString(STATUS)
            return when(status) {
                ODD -> FlowExecutionStatus(ODD)
                EVEN -> FlowExecutionStatus(EVEN)
                else -> FlowExecutionStatus.FAILED
            }
        }
    }
}
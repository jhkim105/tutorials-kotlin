package jhkim105.tutorials.batch.job

import jhkim105.tutorials.batch.job.param.JobParam
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
import java.time.LocalDate

@Configuration
class JobParameterExtendJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jobParameterExtendJobParameter: JobParameterExtendJobParameter,
    private val jobParameterExtendJobParameter2: JobParameterExtendJobParameter2,
    private val jobParam: JobParam,
) {

    companion object {
        const val JOB_NAME = "jobParameterExtendJob"
        private val log = LoggerFactory.getLogger(JobParameterExtendJobConfig::class.java)
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(step1())
            .build()
    }

    @Bean("${JOB_NAME}_step1")
    @JobScope
    fun step1(): Step {
        return StepBuilder("simpleStep", jobRepository)
            .tasklet({ _, _ ->
                log.info("jobParameterExtendJobParameter: $jobParameterExtendJobParameter")
                log.info("jobParameterExtendJobParameter2: $jobParameterExtendJobParameter2")
                log.info("jobParam: $jobParam")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }


    @Bean("${JOB_NAME}jobParameter")
    @JobScope
    fun jobParameterExtendJobParameter(
        @Value("#{jobParameters[createdDate]}") createdDate: LocalDate,
        @Value("#{jobParameters[name]}") name: String
    ): JobParameterExtendJobParameter {
        return JobParameterExtendJobParameter(createdDate, name)
    }

    @Bean("${JOB_NAME}jobParameter2")
    @JobScope
    fun jobParameterExtendJobParameter2(
    ): JobParameterExtendJobParameter2 {
        return JobParameterExtendJobParameter2()
    }


}

open class JobParameterExtendJobParameter(
    private val createdDate: LocalDate,
    private val name: String,
) {
    open fun getRequestDate() = createdDate
    open fun getStatus() = name

    override fun toString(): String {
        return "JobParameterExtendJobParameter(createdDate=$createdDate, name='$name')"
    }
}


open class JobParameterExtendJobParameter2{
    @Value("#{jobParameters[createdDate]}") open val createdDate: LocalDate? = null
    @Value("#{jobParameters[name]}") open val name: String? = null
    override fun toString(): String {
        return "JobParameterExtendJobParameter2(createdDate=$createdDate, name='$name')"
    }
}



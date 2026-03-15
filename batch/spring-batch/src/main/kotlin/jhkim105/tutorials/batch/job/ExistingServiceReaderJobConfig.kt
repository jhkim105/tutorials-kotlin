package jhkim105.tutorials.batch.job

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ExistingServiceReaderJobConfig (
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    companion object {
        const val JOB_NAME = "existing-service-reader"
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(step())
            .build()
    }

    @Bean("${JOB_NAME}_step")
    fun step(): Step {
        return StepBuilder("${JOB_NAME}_step", jobRepository)
            .chunk<String, String>(1, transactionManager) // chunk size 1로 설정
            .reader(reader())
            .processor(customerProcessor())
            .writer(customerWriter())
            .build()
    }


    @Bean
    fun reader(): ItemReader<String> {
        return CustomReader(existingService())
    }

    @Bean
    fun customerProcessor(): ItemProcessor<String, String> {
        return ItemProcessor { customer ->
            customer.uppercase()
        }
    }

    @Bean
    fun customerWriter(): ItemWriter<String> {
        return ItemWriter { customers ->
            customers.forEach { println(it) }
        }
    }

    @Bean
    fun existingService(): ExistingService {
        return ExistingService()
    }

}

class CustomReader(
    private val service: ExistingService,
) : ItemReader<String> {
    private var list: List<String>? = null
    private var currentIndex = 0

    override fun read(): String? {
        if (list == null) {
            list = service.getList("User ");
        }

        if (currentIndex == list!!.size) {
            currentIndex = 0
            return null
        }

        return list?.get(currentIndex++)
    }
}

class ExistingService {
    fun getList(prefix: String): MutableList<String> {
        return mutableListOf("${prefix}_John", "${prefix}_Jane", "${prefix}_Tom", "${prefix}_Alice")
    }
}
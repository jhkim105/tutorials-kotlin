package jhkim105.tutorials.batch.adapter.`in`.job

import jhkim105.tutorials.batch.domain.port.`in`.GetPagingListUseCase
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.AbstractPagingItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class CustomPagingItemReaderJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val getPagingListUseCase: GetPagingListUseCase
) {

    companion object {
        const val JOB_NAME = "custom-paging-reader"
        private val log = LoggerFactory.getLogger(CustomPagingItemReaderJobConfig::class.java)
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(step1())
            .build()
    }

    @Bean("${JOB_NAME}_step1")
    fun step1(): Step {
        return StepBuilder("${JOB_NAME}_step1", jobRepository)
            .chunk<Int, Int>(5, transactionManager)
            .reader(CustomPagingItemReader(getPagingListUseCase, 5, 10))
            .writer { items -> println("Processed: $items") }
            .build()
    }


}

class CustomPagingItemReader(
    private val useCase: GetPagingListUseCase,
    pageSize: Int,
    private val maxPage: Int,
) : AbstractPagingItemReader<Int>() {

    init {
        this.pageSize = pageSize
    }

    override fun doReadPage() {
        if (this.page >= maxPage) {
            this.results = emptyList()
            return
        }

        val currentData = useCase.getPagingList(this.page, this.pageSize)

        this.results = currentData
    }

}
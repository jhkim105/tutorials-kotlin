package jhkim105.tutorials.batch.job

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
class JdbcBatchConfig(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,

    ) {

    companion object {
        const val JOB_NAME = "jdbcPagingJob"
        private val log = LoggerFactory.getLogger(SimpleJobConfig::class.java)
    }

    @Bean(JOB_NAME)
    fun job(): Job {
        log.info("Starting job: $JOB_NAME")
        return JobBuilder(JOB_NAME, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(step())
            .build()
    }


    @Bean("${JOB_NAME}_step")
    @JobScope
    fun step(): Step {
        return StepBuilder("${JOB_NAME}_step", jobRepository)
            .chunk<Map<String, Any>, Map<String, Any>>(100, transactionManager)
            .reader(reader())
            .writer(writer())
            .build()
    }

    @Bean("${JOB_NAME}_reader")
    @StepScope
    fun reader(): JdbcPagingItemReader<Map<String, Any>> {
        val queryProvider = SqlPagingQueryProviderFactoryBean().apply {
            setDataSource(dataSource)
            setSelectClause("SELECT id, name, amount")
            setFromClause("FROM source")
            setSortKey("id")
        }.`object`

        return JdbcPagingItemReaderBuilder<Map<String, Any>>()
            .name("jdbcPagingItemReader")
            .dataSource(dataSource)
            .queryProvider(queryProvider)
            .pageSize(2)
            .rowMapper { rs, _ ->
                mapOf(
                    "id" to rs.getInt("id"),
                    "name" to rs.getString("name"),
                    "amount" to rs.getDouble("amount")
                )
            }.build()
    }

    @Bean("${JOB_NAME}_writer")
    @StepScope
    fun writer(): JdbcBatchItemWriter<Map<String, Any>> {
        return JdbcBatchItemWriterBuilder<Map<String, Any>>()
            .dataSource(dataSource)
            .sql("INSERT INTO target (id, name, amount) VALUES (:id, :name, :amount)")
            .itemSqlParameterSourceProvider { item ->
                val paramMap = mapOf(
                    "id" to item["id"],
                    "name" to item["name"],
                    "amount" to item["amount"]
                )
                MapSqlParameterSource(paramMap)
            }
            .build()
    }





}


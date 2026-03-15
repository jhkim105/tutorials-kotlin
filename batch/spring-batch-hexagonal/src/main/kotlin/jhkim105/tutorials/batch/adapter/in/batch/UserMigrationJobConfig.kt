package jhkim105.tutorials.batch.adapter.`in`.batch

import jhkim105.tutorials.batch.application.domain.entity.User
import jhkim105.tutorials.batch.application.port.`in`.UserGetUseCase
import jhkim105.tutorials.batch.application.port.`in`.UserSaveUseCase
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class UserMigrationJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val userGetUseCase: UserGetUseCase,
    private val userSaveUseCase: UserSaveUseCase
) {


    @Bean
    fun userMigrationJob(): Job = JobBuilder("userMigrationJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(userMigrationStep())
            .build()

    private fun userMigrationStep(): Step = StepBuilder("userMigrationStep", jobRepository)
        .chunk<User, User>(10, transactionManager)
        .reader(userReader())
        .writer(userWriter())
        .build()

    private fun userReader(): ItemReader<out User> {
        val usersIterator = userGetUseCase.getUsers().iterator()

        return ItemReader {
            if (usersIterator.hasNext()) usersIterator.next() else null
        }
    }

    private fun userWriter(): ItemWriter<in User> {
        return ItemWriter<User> {
            chunk -> userSaveUseCase.saveAll(chunk.items)
        }
    }

}
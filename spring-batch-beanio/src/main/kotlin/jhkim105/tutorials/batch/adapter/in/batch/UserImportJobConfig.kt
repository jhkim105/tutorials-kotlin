package jhkim105.tutorials.batch.adapter.`in`.batch

import jhkim105.tutorials.batch.adapter.out.file.UserFileReader
import jhkim105.tutorials.batch.application.domain.entity.User
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
import org.springframework.core.io.ClassPathResource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class UserImportJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val userFileReader: UserFileReader,
    private val userSaveUseCase: UserSaveUseCase
) {


    @Bean
    fun userImportJob(): Job = JobBuilder("userImportJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(userImportStep())
            .build()

    private fun userImportStep(): Step = StepBuilder("userImportStep", jobRepository)
        .chunk<User, User>(10, transactionManager)
        .reader(userReader())
        .writer(userWriter())
        .build()

    private fun userReader(): ItemReader<out User> {
        val usersIterator = userFileReader.read(ClassPathResource("users.txt")).iterator()

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
package jhkim105.tutorials.batch.job

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate

@SpringBootTest(classes = [BatchTestConfig::class, JobParameterExtendJobConfig::class])
@SpringBatchTest
@TestPropertySource(properties = ["job.name=${JobParameterExtendJobConfig.JOB_NAME}"])
class JobParameterExtendJobConfigTest(
    @Autowired private val jobLauncherTestUtils: JobLauncherTestUtils,
    @Autowired private val jobRepositoryTestUtils: JobRepositoryTestUtils,
) {

    @BeforeEach
    fun clearJobExecutions() {
        jobRepositoryTestUtils.removeJobExecutions()
    }

    @Test
    fun test() {
        val jobParameters = JobParametersBuilder()
            .addLocalDate("createdDate", LocalDate.now())
            .addString("name", "name01")
            .toJobParameters()
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }
}
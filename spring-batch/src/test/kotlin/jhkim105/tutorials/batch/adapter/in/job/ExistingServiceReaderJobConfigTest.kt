package jhkim105.tutorials.batch.adapter.`in`.job

import jhkim105.tutorials.batch.adapter.UseCaseConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource


@SpringBootTest(classes = [BatchTestConfig::class, ExistingServiceReaderJobConfig::class])
@SpringBatchTest
@TestPropertySource(properties = ["job.name=${ExistingServiceReaderJobConfig.JOB_NAME}"])
class ExistingServiceReaderJobConfigTest {
    @Autowired
    lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    lateinit var jobRepositoryTestUtils: JobLauncherTestUtils

    @Test
    fun test() {
        val jobParameters = jobLauncherTestUtils.uniqueJobParameters
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }
}
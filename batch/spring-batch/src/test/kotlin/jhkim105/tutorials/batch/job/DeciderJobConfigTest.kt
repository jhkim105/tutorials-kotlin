package jhkim105.tutorials.batch.job

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import kotlin.test.Test

@SpringBootTest(classes = [BatchTestConfig::class, DeciderJobConfig::class])
@SpringBatchTest
@TestPropertySource(properties = ["job.name=${DeciderJobConfig.JOB_NAME}"])
@ActiveProfiles("test")
class DeciderJobConfigTest(
    @Autowired private val jobLauncherTestUtils: JobLauncherTestUtils,
    @Autowired private val jobRepositoryTestUtils: JobRepositoryTestUtils,
) {

    @BeforeEach
    fun clearJobExecutions() {
        jobRepositoryTestUtils.removeJobExecutions()
    }

    @Test
    fun test() {
        val jobParameters = jobLauncherTestUtils.uniqueJobParameters
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }
}
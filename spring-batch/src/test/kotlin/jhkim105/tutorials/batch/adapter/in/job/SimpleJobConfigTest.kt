package jhkim105.tutorials.batch.adapter.`in`.job

import jhkim105.tutorials.batch.adapter.`in`.job.SimpleJobConfig
import org.junit.jupiter.api.Assertions.*
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import kotlin.test.Test

@SpringBootTest(classes = [BatchTestConfig::class, SimpleJobConfig::class])
@SpringBatchTest
@TestPropertySource(properties = ["job.name=${SimpleJobConfig.JOB_NAME}"])
class SimpleJobConfigTest {
    @Autowired
    lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    lateinit var jobRepositoryTestUtils: JobLauncherTestUtils

    @Test
    @Throws(Exception::class)
    fun test() {
        val jobParameters = jobLauncherTestUtils.uniqueJobParameters

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }
}
package jhkim105.tutorials.batch.adapter.`in`.job

import jhkim105.tutorials.batch.adapter.`in`.job.StepNextConditionalJobConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@SpringBootTest(classes = [BatchTestConfig::class, StepNextConditionalJobConfig::class])
@SpringBatchTest
@TestPropertySource(properties = ["job.name=" + StepNextConditionalJobConfig.JOB_NAME])
class StepNextConditionalJobConfigTest {

    @Autowired
    lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    lateinit var jobRepositoryTestUtils: JobLauncherTestUtils


    @Test
    @Throws(Exception::class)
    fun test() {
        val jobParameters = jobLauncherTestUtils.uniqueJobParameters

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)
        println(jobExecution.jobInstance.jobName)

        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
    }
}
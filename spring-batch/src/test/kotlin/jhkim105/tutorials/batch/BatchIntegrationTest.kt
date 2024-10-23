package jhkim105.tutorials.batch

import org.junit.jupiter.api.Test
import org.springframework.batch.core.JobExecution
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest


@SpringBatchTest
@SpringBootTest
class BatchIntegrationTest {

    @Autowired
    lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    lateinit var jobRositoryTestUtils: JobLauncherTestUtils

    @Test
    fun test() {
        val jobExecution = jobRositoryTestUtils.launchJob()
    }
}
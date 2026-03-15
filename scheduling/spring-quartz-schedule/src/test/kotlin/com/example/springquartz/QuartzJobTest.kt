package com.example.springquartz

import com.example.springquartz.service.SampleJobService
import java.util.concurrent.TimeUnit
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class QuartzJobTest {

    @Autowired private lateinit var scheduler: Scheduler

    @MockBean private lateinit var sampleJobService: SampleJobService

    @Test
    fun `default job and trigger should be registered`() {
        // Given
        val jobKey =
                JobKey.jobKey(
                        "jobDetail"
                ) // Default bean name if not specified or specific identity
        // In SchedulerConfig, we used JobDetailFactoryBean which defaults definition to bean name
        // "jobDetail"
        // But wait, in the config I did:
        // JobBuilder.newJob()...withIdentity("Qrtz_Job_Detail") ? No, that was the snippet in
        // requirements
        // In my implementation of SchedulerConfig:
        // jobDetailFactory.setJobClass(SampleJob::class.java)
        // jobDetailFactory.setDescription("Invoke Sample Job service...")
        // It didn't explicitly set identity, so it defaults to bean name?
        // Let's check SchedulerConfig.kt implementation I wrote.

        // I wrote:
        // @Bean fun jobDetail(): JobDetailFactoryBean { ... }
        // @Bean fun trigger(job: JobDetail): SimpleTriggerFactoryBean {
        // ...Trigger...setRepeatInterval(3600000)... }

        // Spring's JobDetailFactoryBean uses the bean name as the job name if not set.
        // The bean name is 'jobDetail'.
        // Trigger bean name is 'trigger'.

        val expectedJobKey = JobKey.jobKey("Qrtz_Job_Detail")
        val expectedTriggerKey = TriggerKey.triggerKey("trigger")

        // When & Then
        assertThat(scheduler.checkExists(expectedJobKey)).isTrue()
        assertThat(scheduler.checkExists(expectedTriggerKey)).isTrue()
    }

    @Test
    fun `job should be executed and call service`() {
        // The job is scheduled to run every hour.
        // To test execution, we can manually trigger it using the scheduler.

        val jobKey = JobKey.jobKey("Qrtz_Job_Detail")

        // When
        scheduler.triggerJob(jobKey)

        // Then
        // Wait for asynchronous execution
        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            verify(sampleJobService, atLeastOnce()).executeSampleJob()
        }
    }
}

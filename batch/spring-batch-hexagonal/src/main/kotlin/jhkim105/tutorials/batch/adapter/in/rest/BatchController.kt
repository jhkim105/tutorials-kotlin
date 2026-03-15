package jhkim105.tutorials.batch.adapter.`in`.rest

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant


@RestController
@RequestMapping("/batches")
class BatchController(
    private val jobLauncher: JobLauncher,
    private val applicationContext: ApplicationContext

) {

    @PostMapping("/{jobName}")
    fun launch(@PathVariable jobName: String): ResponseEntity<String?> {
        val job =applicationContext.getBean(jobName) as Job

        val jobParameters = JobParametersBuilder()
            .addLong("time", Instant.now().toEpochMilli(),)
            .toJobParameters()

        val jobExecution: JobExecution = jobLauncher.run(job, jobParameters)
        return ResponseEntity.ok(jobExecution.toString());
    }

}
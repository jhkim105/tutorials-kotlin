package jhkim105.tutorials.batch.job.param

import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@JobScope
class JobParam(
    @Value("#{jobParameters[createdDate]}") val createdDate: LocalDate,
    @Value("#{jobParameters[name]}") private val name: String,
) {
    override fun toString(): String {
        return "JobParam(createdDate=$createdDate, name='$name')"
    }
}
package com.example.scheduler.bootstrap

import com.example.scheduler.application.port.ScheduleCommand
import com.example.scheduler.application.port.ScheduleUseCase
import com.example.scheduler.domain.model.ScheduleType
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SampleDataInitializer(
    private val scheduleUseCase: ScheduleUseCase
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (scheduleUseCase.list().isNotEmpty()) {
            return
        }

        scheduleUseCase.create(
            ScheduleCommand(
                name = "Print hello",
                scheduleType = ScheduleType.CRON,
                cronExpression = "*/10 * * * * ?",
                enabled = true,
                actionKey = "PRINT_MESSAGE",
                payload = "{\"message\": \"Hello Scheduler\"}"
            )
        )

        scheduleUseCase.create(
            ScheduleCommand(
                name = "Create file",
                scheduleType = ScheduleType.CRON,
                cronExpression = "0 */1 * * * ?",
                enabled = true,
                actionKey = "CREATE_FILE",
                payload = "{\"directory\": \"data/out\", \"prefix\": \"sample\"}"
            )
        )

        scheduleUseCase.create(
            ScheduleCommand(
                name = "Ping once",
                scheduleType = ScheduleType.ONCE,
                runAt = Instant.now().plusSeconds(30),
                enabled = true,
                actionKey = "HTTP_PING",
                payload = "{\"url\": \"https://example.com\"}"
            )
        )
    }
}

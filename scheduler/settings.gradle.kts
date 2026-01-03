rootProject.name = "scheduler"

include(
    ":scheduler-task:app:app-task",
    ":scheduler-quartz:app:app-quartz",
    ":scheduler-core",
    ":infra:scheduler-infra-persistence",
    ":infra:scheduler-infra-actions",
    ":scheduler-task:adapters:task-adapters",
    ":scheduler-quartz:adapters:quartz-adapters"
)

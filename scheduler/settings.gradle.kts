rootProject.name = "scheduler"

include(
    ":scheduler-domain",
    ":scheduler-application",
    ":scheduler-infra-persistence",
    ":scheduler-infra-actions",
    ":scheduler-infra-task",
    ":scheduler-infra-quartz",
    ":scheduler-api-task",
    ":scheduler-api-quartz"
)

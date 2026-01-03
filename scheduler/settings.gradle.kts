rootProject.name = "scheduler"

include(
    ":app:scheduler-spring-app",
    ":app:scheduler-quartz-app",
    ":core",
    ":adapter:adapter-core",
    ":adapter:adapter-spring",
    ":adapter:adapter-quartz"
)

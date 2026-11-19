rootProject.name = "sse-compare"

include(
    "common",
    "persistence-jpa",
    "persistence-r2dbc",
    "app-mvc-sseemitter",
    "app-mvc-flow",
    "app-webflux-flux",
    "app-webflux-flow",
    "loadgen",
)

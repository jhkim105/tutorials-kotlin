rootProject.name = "hexagonal-multi-module"
include(
    "domain",
    "application",
    "interface:api",
    "infrastructure:persistence",
    "app"
)
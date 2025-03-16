plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "concurrency"
include("common", "spring-webflux-coroutines", "spring-web-vt")

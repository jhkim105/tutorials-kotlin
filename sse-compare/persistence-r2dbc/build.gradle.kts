plugins {
    `java-library`
    kotlin("plugin.spring")
}

dependencies {
    api(project(":common"))
    api("org.springframework.boot:spring-boot-starter-data-r2dbc")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    runtimeOnly("io.asyncer:r2dbc-mysql:1.3.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
}

plugins {
    `java-library`
    `java-test-fixtures`
    kotlin("plugin.spring")
}

dependencies {
    api("jakarta.validation:jakarta.validation-api")
    api("io.micrometer:micrometer-core")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("io.kotest:kotest-runner-junit5:5.9.1")
    testFixturesApi("io.kotest:kotest-assertions-core:5.9.1")
    testFixturesApi("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    testFixturesImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

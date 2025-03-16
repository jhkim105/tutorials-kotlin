plugins {
    kotlin("jvm")
}

group = "jhkim105.tutorials.concurrency"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.springframework.boot:spring-boot-starter-logging:3.4.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
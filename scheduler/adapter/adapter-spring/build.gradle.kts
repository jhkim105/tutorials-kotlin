plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.9"))
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-context-support")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}

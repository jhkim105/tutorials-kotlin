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
    implementation(project(":scheduler-domain"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-web")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}

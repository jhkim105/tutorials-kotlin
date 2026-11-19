plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":persistence-jpa"))
    implementation(project(":persistence-r2dbc"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation(testFixtures(project(":common")))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

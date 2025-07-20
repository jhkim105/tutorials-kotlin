plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":interface:api"))
    implementation(project(":infrastructure:persistence"))
    implementation("org.springframework.boot:spring-boot-starter")
}

kotlin {
    jvmToolchain(21)
}

tasks.bootJar {
    archiveFileName.set("blog.jar") // 원하는 이름
}
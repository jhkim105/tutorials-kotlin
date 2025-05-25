import gradle.kotlin.dsl.accessors._e054d9723d982fdb55b1e388b8ab0cbf.implementation
import gradle.kotlin.dsl.accessors._e054d9723d982fdb55b1e388b8ab0cbf.java
import gradle.kotlin.dsl.accessors._e054d9723d982fdb55b1e388b8ab0cbf.testImplementation
import gradle.kotlin.dsl.accessors._e054d9723d982fdb55b1e388b8ab0cbf.testRuntimeOnly
import org.gradle.kotlin.dsl.*

plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
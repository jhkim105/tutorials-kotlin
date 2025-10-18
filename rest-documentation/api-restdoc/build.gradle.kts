plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.asciidoctor.jvm.convert") version "4.0.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":core"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.4.1")

    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.4.1")
}

tasks.test {
    useJUnitPlatform()
}

val snippetsDir = layout.buildDirectory.dir("generated-snippets")
tasks.test {
    outputs.dir(snippetsDir)
}

tasks.asciidoctor {
    dependsOn(tasks.test)
    inputs.dir("build/generated-snippets")
    baseDirFollowsSourceFile()
    sources {
        include("**/index.adoc")
    }
    setOutputDir(layout.buildDirectory.dir("docs/asciidoc"))
}

tasks.bootJar {
    dependsOn(tasks.named("asciidoctor"))
    from("build/docs/asciidoc") {
        into("static/docs")
    }
}
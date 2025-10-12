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

    dependsOn(tasks.test) // 테스트 후 생성된 스니펫을 사용
    inputs.dir("build/generated-snippets")
    baseDirFollowsSourceFile()
    sources {
        include("**/index.adoc")
    }
    setOutputDir(layout.buildDirectory.dir("docs/asciidoc"))
}

// ✅ HTML 복사 작업
tasks.register<Copy>("copyDocs") {
    dependsOn(tasks.asciidoctor)
    from("build/docs/asciidoc")
    into("src/main/resources/static/docs")
}

// bootJar 빌드 시 문서 포함
tasks.bootJar {
    dependsOn(tasks.asciidoctor)
    from(layout.buildDirectory.dir("docs/asciidoc")) { into("static/docs") }
}
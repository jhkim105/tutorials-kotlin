plugins {
    `kotlin-dsl`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21)) // 필요 시 변경
}

repositories {
    mavenCentral()
}
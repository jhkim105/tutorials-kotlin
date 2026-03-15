rootProject.name = "rest-documentation"

include("core", "api-springdoc", "api-restdoc", "api-restdoc2openapi")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
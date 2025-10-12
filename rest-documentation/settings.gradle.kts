rootProject.name = "rest-documentation"

include("core", "api-springdoc", "api-restdoc")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
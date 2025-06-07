import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.1"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.21"
	kotlin("plugin.spring") version "1.9.21"
	kotlin("plugin.jpa") version "1.9.21"
	kotlin("kapt") version "1.9.21"



}

group = "jhkim105.tutorials"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.hibernate.orm:hibernate-ant")
	runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation(kotlin("test"))



	// querydsl
	implementation("com.querydsl:querydsl-jpa:${dependencyManagement.importedProperties["querydsl.version"]}:jakarta")
	implementation("com.querydsl:querydsl-apt:${dependencyManagement.importedProperties["querydsl.version"]}:jakarta")
	kapt("com.querydsl:querydsl-apt:${dependencyManagement.importedProperties["querydsl.version"]}:jakarta")



}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		showStandardStreams = true
		showCauses = true
		showExceptions = true
		showStackTraces = true
		exceptionFormat = TestExceptionFormat.FULL
	}
}

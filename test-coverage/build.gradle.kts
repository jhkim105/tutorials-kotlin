plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
}

group = "jhkim105.tutorials"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport) // 테스트 후 커버리지 리포트 생성
}

tasks.jacocoTestReport {
	dependsOn(tasks.test) // 테스트가 먼저 수행되어야 함
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.test)

	violationRules {
		rule {
			limit {
				counter = "LINE"        // LINE, INSTRUCTION, BRANCH 등 선택 가능
				value = "COVEREDRATIO" // COVEREDCOUNT, MISSEDCOUNT 등도 가능
				minimum = "0.70".toBigDecimal() // 최소 80% 커버리지 요구
			}
		}
	}
}

// CI에서 `build` 수행 시 자동 포함되도록 연결
tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}
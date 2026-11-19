plugins {
    application
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("ch.qos.logback:logback-classic")
}

application {
    mainClass.set("jhkim105.ssecompare.loadgen.MainKt")
}

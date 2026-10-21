plugins {
    kotlin("jvm") version "2.2.21"
}

group = "ru.matveev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("io.ktor:ktor-client-core:3.6.0")
    testImplementation("io.ktor:ktor-client-cio:3.6.0")
    testImplementation("io.ktor:ktor-client-content-negotiation:3.6.0")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:3.6.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    application
    // id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "com.example"
version = "0.0.1"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.10")

    
}

application {
    mainClass.set("com.example.ApplicationKt")
}

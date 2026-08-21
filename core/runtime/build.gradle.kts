plugins {
    id("cryptolyzer.kotlin-jvm")
}

dependencies {
    api(project(":core:contracts"))
    implementation(project(":core:telemetry"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.platform.launcher)
}

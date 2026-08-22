plugins {
    id("cryptolyzer.kotlin-jvm")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit.jupiter)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.platform.launcher)
}

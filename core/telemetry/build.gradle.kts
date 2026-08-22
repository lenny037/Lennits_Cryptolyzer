plugins {
    id("cryptolyzer.kotlin-jvm")
}

dependencies {
    api(project(":core:contracts"))
    testImplementation(libs.junit.jupiter)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.platform.launcher)
}

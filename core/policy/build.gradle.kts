plugins {
    id("cryptolyzer.kotlin-jvm")
}

dependencies {
    api(project(":core:contracts"))
    api(project(":core:domain"))
    testImplementation(libs.junit.jupiter)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.platform.launcher)
}

plugins {
    id("cryptolyzer.kotlin-jvm")
    `java-test-fixtures`
}

dependencies {
    api(project(":core:contracts"))
    api(project(":core:domain"))
    implementation(project(":core:telemetry"))

    // The EventStore contract kit is published as a test fixture so that every implementation
    // of the port, in this module or any other, is verified against one suite.
    testFixturesApi(project(":core:contracts"))
    testFixturesApi(libs.junit.jupiter)

    testImplementation(testFixtures(project(":core:eventbus")))
    testImplementation(project(":core:telemetry"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.platform.launcher)
}

plugins {
    id("cryptolyzer.kotlin-jvm")
}

dependencies {
    api(project(":core:contracts"))
    api(project(":core:eventbus"))
    implementation(libs.sqlite.jdbc)

    // Same contract suite as the in-memory store: two implementations, one specification.
    testImplementation(testFixtures(project(":core:eventbus")))
    // RegisteredEventStore takes a Telemetry sink; the schema-compatibility tests wire a recording
    // one so an unreadable fixture row is observable rather than merely absent.
    testImplementation(project(":core:telemetry"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.platform.launcher)
}

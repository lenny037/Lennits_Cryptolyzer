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

// EventRegistryDocsTest regenerates and verifies docs/events/EVENT_REGISTRY.md, so it needs the
// repository root. Passed as a property rather than derived from the working directory: a test that
// walks up the filesystem guessing at a repository layout is a test that breaks on the first
// change to it. Providers keep this configuration-cache compatible.
tasks.test {
    systemProperty("cryptolyzer.rootDir", rootProject.layout.projectDirectory.asFile.absolutePath)
    systemProperty(
        "cryptolyzer.updateEventRegistryDoc",
        providers.systemProperty("cryptolyzer.updateEventRegistryDoc").orElse("false").get(),
    )
    // The document is an input and an output of this task in update mode, so caching it would be
    // wrong; the check itself is cheap.
    outputs.upToDateWhen { false }
}

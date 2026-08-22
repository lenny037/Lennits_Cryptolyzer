plugins {
    id("cryptolyzer.kotlin-jvm")
}

dependencies {
    api(project(":core:contracts"))
    api(project(":core:eventbus"))
    implementation(libs.sqlite.jdbc)

    // Same contract suite as the in-memory store: two implementations, one specification.
    testImplementation(testFixtures(project(":core:eventbus")))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.platform.launcher)
}

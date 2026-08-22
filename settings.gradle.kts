rootProject.name = "lennit-cryptolyzer"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

// ---------------------------------------------------------------------------
// Core is pure Kotlin/JVM on purpose: the domain, event fabric, runtime and
// policy engine must be buildable and testable without the Android SDK.
// ADR-002 records this decision.
// ---------------------------------------------------------------------------
include(":core:contracts")
include(":core:domain")
include(":core:telemetry")
include(":core:eventbus")
include(":core:persistence")
include(":core:runtime")
include(":core:policy")

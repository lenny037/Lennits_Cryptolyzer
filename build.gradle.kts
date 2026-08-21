// The Kotlin Gradle plugin arrives on the build classpath through buildSrc and is applied by the
// `cryptolyzer.kotlin-jvm` convention plugin, so it is deliberately not declared again here.

/**
 * Architecture guard, Phase 1 gate, machine-checkable.
 *
 * The core layers must never gain a dependency on a platform SDK or on a hosted
 * backend SDK. ADR-001 removes Firebase/Firestore from the core; ADR-002 keeps the
 * core pure JVM. A prose rule that nothing enforces is not a rule, so the build
 * fails if a forbidden coordinate appears in a core module's declared dependencies.
 */
val forbiddenCoreDependencies = listOf(
    "com.google.firebase",
    "com.google.gms",
    "com.google.android.gms",
    "com.google.cloud:google-cloud-firestore",
    "androidx.",
    "com.android.",
)

// Leaf modules only. `:core` itself is a grouping container with no build file, so it has no
// `check` task and no configurations to inspect.
val coreModulePaths: List<String> = subprojects.map { it.path }.filter { it.startsWith(":core:") }

val architectureGuard by tasks.registering {
    group = "verification"
    description = "Fails if any :core module depends on a platform or hosted-backend SDK."

    // Declared dependencies are collected during configuration so the task action itself
    // touches no live project state and stays configuration-cache compatible.
    val declaredCoordinates: Map<String, List<String>> = subprojects
        .filter { it.path.startsWith(":core:") }
        .associate { module ->
            module.path to module.configurations
                .filter { it.name.endsWith("Implementation") || it.name.endsWith("Api") }
                .flatMap { configuration -> configuration.dependencies.map { "${it.group}:${it.name}" } }
        }

    val forbidden = forbiddenCoreDependencies

    doLast {
        val declaredViolations = declaredCoordinates.flatMap { (modulePath, coordinates) ->
            coordinates.mapNotNull { coordinate ->
                forbidden.firstOrNull { coordinate.startsWith(it) }?.let { match ->
                    "$modulePath -> $coordinate (declared; forbidden prefix '$match')"
                }
            }
        }
        val violations = declaredViolations
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Architecture guard failed. Core modules must stay free of platform/backend SDKs:\n" +
                    violations.joinToString("\n") { "  - $it" },
            )
        }
        logger.lifecycle(
            "Architecture guard: declared dependencies of ${declaredCoordinates.size} core modules clean.",
        )
    }
}

/**
 * Transitive half of the guard, registered inside each core module.
 *
 * A declared-dependency check is not sufficient: a forbidden SDK can arrive on the classpath
 * through an innocent-looking library. The resolved runtime classpath is the honest answer. The
 * task is registered in the owning project so it only ever touches its own project's state, which
 * keeps it configuration-cache compatible.
 */
subprojects {
    if (!path.startsWith(":core:")) return@subprojects
    val forbidden = forbiddenCoreDependencies
    val modulePath = path
    plugins.withId("org.jetbrains.kotlin.jvm") {
        val classpathGuard = tasks.register("classpathGuard") {
            group = "verification"
            description = "Fails if a forbidden SDK reaches this module's resolved runtime classpath."
            val resolved = configurations.named("runtimeClasspath")
                .flatMap { it.incoming.artifacts.resolvedArtifacts }
                .map { artifacts -> artifacts.map { it.id.componentIdentifier.displayName } }
            doLast {
                val coordinates = resolved.get()
                val violations = coordinates.mapNotNull { coordinate ->
                    forbidden.firstOrNull { coordinate.startsWith(it) }?.let { match ->
                        "$modulePath -> $coordinate (resolved runtime classpath; forbidden prefix '$match')"
                    }
                }
                if (violations.isNotEmpty()) {
                    throw GradleException(
                        "Classpath guard failed for $modulePath:\n" +
                            violations.joinToString("\n") { "  - $it" },
                    )
                }
                logger.lifecycle("Classpath guard: $modulePath clean (${coordinates.size} artifacts).")
            }
        }
        tasks.named("check").configure { dependsOn(classpathGuard) }
    }
}

val verifyAll by tasks.registering {
    group = "verification"
    description = "Full local gate: architecture guard plus every module's tests."
    dependsOn(architectureGuard)
    dependsOn(coreModulePaths.map { "$it:check" })
}

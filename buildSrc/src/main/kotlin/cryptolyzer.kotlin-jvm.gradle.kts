import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Convention plugin for every pure Kotlin/JVM module in the monorepo.
 *
 * Hard rules encoded here (see docs/architecture/DEPENDENCY_RULES.md):
 *  - explicit API mode: every public declaration needs an explicit visibility and return type,
 *    so module boundaries cannot drift silently.
 *  - warnings are errors: no "we will clean it up later" debt accumulates in core.
 *  - JVM target 17: matches the Android app's desugaring target, so core artifacts are
 *    consumable by the Android module without bytecode surprises.
 *  - toolchain 21: the compiler itself runs on a pinned JDK, so the build does not depend on
 *    whichever JDK happens to be on the developer's or runner's PATH. Reproducibility is the
 *    point: target and toolchain are separate decisions and both are declared.
 */

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
}

kotlin {
    explicitApi()
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

java {
    // Bytecode level is pinned independently of the toolchain that produces it.
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.progressiveMode.set(true)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

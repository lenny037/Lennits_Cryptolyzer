package com.lennit.cryptolyzer.eventbus

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * The event registry document is generated, so it cannot drift from the declaration.
 *
 * Documentation that is maintained by hand and verified by nobody is how the repository ended up
 * with root directives contradicting its own architecture (see `docs/PLAN_CRITIQUE.md`). This test
 * is the cheap structural fix: the document either equals `EventRegistry.render()` or the build
 * fails.
 *
 * Regenerate with:
 * `./gradlew :core:eventbus:test -Dcryptolyzer.updateEventRegistryDoc=true`
 */
class EventRegistryDocsTest {

    private val repositoryRoot: Path
        get() = Path.of(
            requireNotNull(System.getProperty("cryptolyzer.rootDir")) {
                "cryptolyzer.rootDir is not set; core/eventbus/build.gradle.kts must pass it"
            },
        )

    private val documentPath: Path get() = repositoryRoot.resolve("docs/events/EVENT_REGISTRY.md")

    @Test
    fun `the committed registry document matches the declaration`() {
        val expected = EventRegistry.RELEASED.render()
        if (System.getProperty("cryptolyzer.updateEventRegistryDoc") == "true") {
            Files.createDirectories(documentPath.parent)
            Files.writeString(documentPath, expected)
            return
        }
        assertTrue(
            Files.exists(documentPath),
            "$documentPath is missing; regenerate it with " +
                "./gradlew :core:eventbus:test -Dcryptolyzer.updateEventRegistryDoc=true",
        )
        assertEquals(
            expected,
            Files.readString(documentPath),
            "docs/events/EVENT_REGISTRY.md is out of date with EventRegistry.RELEASED. " +
                "Regenerate it with " +
                "./gradlew :core:eventbus:test -Dcryptolyzer.updateEventRegistryDoc=true",
        )
    }

    @Test
    fun `the document records the fingerprint of the declaration it was generated from`() {
        assertTrue(
            EventRegistry.RELEASED.render().contains(EventRegistry.RELEASED.fingerprint()),
        )
    }
}

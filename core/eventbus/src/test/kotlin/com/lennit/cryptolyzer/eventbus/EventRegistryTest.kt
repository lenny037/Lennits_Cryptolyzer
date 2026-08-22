package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import com.lennit.cryptolyzer.eventbus.testing.RegistryFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** The declaration must refuse to describe an unreadable history in the first place. */
class EventSchemaDeclarationTest {

    private fun field(name: String, required: Boolean = true) =
        PayloadField(name, required, DataClassification.Public, "desc")

    @Test
    fun `a version above 1 without an upcast is refused, because a stored row cannot upgrade itself`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            EventSchema(
                type = EventType("test.no_upcast"),
                owner = ModuleId.Ingestion,
                status = SchemaStatus.Ratified,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "p",
                versions = listOf(
                    EventSchemaVersion(1, listOf(field("a"))),
                    EventSchemaVersion(2, listOf(field("a"), field("b"))),
                ),
            )
        }
        assertTrue(error.message!!.contains("needs an upcast"))
    }

    @Test
    fun `version numbers must be contiguous from 1, so no released shape is missing`() {
        assertThrows(IllegalArgumentException::class.java) {
            EventSchema(
                type = EventType("test.gap"),
                owner = ModuleId.Ingestion,
                status = SchemaStatus.Ratified,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "p",
                versions = listOf(
                    EventSchemaVersion(1, listOf(field("a"))),
                    EventSchemaVersion(3, listOf(field("a")), upcastFromPrevious = DropField("x")),
                ),
            )
        }
    }

    @Test
    fun `an additive-only type cannot remove a field`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            EventSchema(
                type = EventType("test.removal"),
                owner = ModuleId.Ingestion,
                status = SchemaStatus.Ratified,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "p",
                versions = listOf(
                    EventSchemaVersion(1, listOf(field("a"), field("b"))),
                    EventSchemaVersion(2, listOf(field("a")), upcastFromPrevious = DropField("b")),
                ),
            )
        }
        assertTrue(error.message!!.contains("removes"))
    }

    @Test
    fun `a later version cannot weaken a field's redaction class`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            EventSchema(
                type = EventType("test.weakened"),
                owner = ModuleId.Ingestion,
                status = SchemaStatus.Ratified,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "p",
                versions = listOf(
                    EventSchemaVersion(
                        1,
                        listOf(PayloadField("secret_note", true, DataClassification.Sensitive, "d")),
                    ),
                    EventSchemaVersion(
                        2,
                        listOf(PayloadField("secret_note", true, DataClassification.Public, "d")),
                        upcastFromPrevious = DropField("nothing"),
                    ),
                ),
            )
        }
        assertTrue(error.message!!.contains("redaction must never regress"))
    }

    @Test
    fun `a ratified schema must declare its payload`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            EventSchema(
                type = EventType("test.empty_ratified"),
                owner = ModuleId.Ingestion,
                status = SchemaStatus.Ratified,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "p",
                versions = listOf(EventSchemaVersion(1, emptyList())),
            )
        }
        assertTrue(error.message!!.contains("declares no payload fields"))
    }

    @Test
    fun `duplicate field names in one version are refused`() {
        assertThrows(IllegalArgumentException::class.java) {
            EventSchemaVersion(1, listOf(field("a"), field("a", required = false)))
        }
    }

    @Test
    fun `a registry cannot declare the same type twice`() {
        assertThrows(IllegalArgumentException::class.java) {
            EventRegistry.of(RegistryFixtures.sensorReading, RegistryFixtures.sensorReading)
        }
    }
}

/** Declarative upcast steps, checked individually before they are trusted in a chain. */
class PayloadUpcastTest {

    @Test
    fun `rename moves the value and is a no-op when the old key is absent`() {
        val rename = RenameField(from = "old", to = "new")
        assertEquals(mapOf("new" to "7"), rename.apply(mapOf("old" to "7")))
        assertEquals(mapOf("other" to "1"), rename.apply(mapOf("other" to "1")))
    }

    @Test
    fun `default never overwrites an existing value`() {
        val default = DefaultField("unit", "wei")
        assertEquals(mapOf("unit" to "gwei"), default.apply(mapOf("unit" to "gwei")))
        assertEquals(mapOf("unit" to "wei"), default.apply(emptyMap()))
    }

    @Test
    fun `steps apply in declaration order`() {
        val steps = UpcastSteps(RenameField("a", "b"), DefaultField("b", "fallback"), DropField("c"))
        assertEquals(mapOf("b" to "1"), steps.apply(mapOf("a" to "1", "c" to "2")))
        assertEquals(mapOf("b" to "fallback"), steps.apply(emptyMap()))
    }

    @Test
    fun `an empty step list is refused rather than silently doing nothing`() {
        assertThrows(IllegalArgumentException::class.java) { UpcastSteps(emptyList()) }
    }

    @Test
    fun `a rename to itself is refused`() {
        assertThrows(IllegalArgumentException::class.java) { RenameField("a", "a") }
    }
}

class EventRegistryValidationTest {

    private val registry = RegistryFixtures.registry

    @Test
    fun `an unregistered type is refused and the message says what to do`() {
        val envelope = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.NEVER_REGISTERED,
            schemaVersion = 1,
            payload = emptyMap(),
        )
        val result = registry.validate(envelope)
        val error = assertIsValidation(result)
        assertEquals("type", error.field)
        assertTrue(error.message.contains("not in the event registry"))
    }

    @Test
    fun `a version newer than the build is refused instead of guessed at`() {
        val envelope = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.SENSOR_READING,
            schemaVersion = 2,
            payload = mapOf("sensor" to "s", "reading" to "1"),
        )
        val error = assertIsValidation(registry.validate(envelope))
        assertEquals("schemaVersion", error.field)
        assertTrue(error.message.contains("Refusing to guess"))
    }

    @Test
    fun `a missing required field names the field`() {
        val error = assertIsValidation(
            registry.validatePayload(RegistryFixtures.SENSOR_READING, 1, mapOf("sensor" to "s")),
        )
        assertEquals("reading", error.field)
    }

    @Test
    fun `a blank required field is not treated as present`() {
        val error = assertIsValidation(
            registry.validatePayload(
                RegistryFixtures.SENSOR_READING,
                1,
                mapOf("sensor" to "s", "reading" to "   "),
            ),
        )
        assertEquals("reading", error.field)
    }

    @Test
    fun `an undeclared field is refused, so payload drift fails at the boundary`() {
        val error = assertIsValidation(
            registry.validatePayload(
                RegistryFixtures.SENSOR_READING,
                1,
                mapOf("sensor" to "s", "reading" to "1", "surprise" to "x"),
            ),
        )
        assertEquals("surprise", error.field)
        assertTrue(error.message.contains("Add the field to the registry"))
    }

    @Test
    fun `an optional declared field may be absent or present`() {
        val base = mapOf("sensor" to "s", "reading" to "1")
        assertTrue(registry.validatePayload(RegistryFixtures.SENSOR_READING, 1, base).isSuccess)
        assertTrue(
            registry.validatePayload(RegistryFixtures.SENSOR_READING, 1, base + ("note" to "n")).isSuccess,
        )
    }

    @Test
    fun `a draft type accepts any payload, and says so in the document`() {
        assertTrue(
            registry.validatePayload(
                RegistryFixtures.DRAFT_ONLY,
                1,
                mapOf("anything" to "goes", "for_now" to "true"),
            ).isSuccess,
        )
    }

    private fun assertIsValidation(result: Outcome<*>): PlatformError.Validation {
        val error = result.errorOrNull()
        assertTrue(error is PlatformError.Validation, "expected a validation failure, got $error")
        return error as PlatformError.Validation
    }
}

class EventRegistryUpcastTest {

    private val registry = RegistryFixtures.registry

    @Test
    fun `a v1 payload is lifted across two versions to the current shape`() {
        val result = registry.upcast(
            RegistryFixtures.WIDGET_OBSERVED,
            fromVersion = 1,
            payload = mapOf("source" to "rpc", "value_raw" to "42"),
        )
        val lifted = requireNotNull(result.getOrNull()) {
            "upcast failed: ${result.errorOrNull()?.message}"
        }
        assertEquals(3, lifted.version)
        assertEquals(listOf(2, 3), lifted.appliedVersions)
        assertEquals(mapOf("source" to "rpc", "value" to "42", "unit" to "wei"), lifted.payload)
    }

    @Test
    fun `a current-version payload is returned untouched and reports no upcast`() {
        val payload = mapOf("source" to "rpc", "value" to "42", "unit" to "gwei")
        val lifted = requireNotNull(
            registry.upcast(RegistryFixtures.WIDGET_OBSERVED, 3, payload).getOrNull(),
        )
        assertEquals(payload, lifted.payload)
        assertTrue(lifted.appliedVersions.isEmpty())
        assertTrue(!lifted.wasUpcast)
    }

    @Test
    fun `the envelope overload reports the current version and keeps identity fields`() {
        val envelope = RegistryFixtures.envelope(
            eventId = "e1",
            type = RegistryFixtures.LEDGER_ENTRY,
            schemaVersion = 1,
            payload = mapOf("amount" to "12.50"),
            producer = ModuleId.Treasury,
        )
        val lifted = requireNotNull(registry.upcast(envelope).getOrNull())
        assertEquals(2, lifted.schemaVersion)
        assertEquals(mapOf("amount" to "12.50", "memo" to ""), lifted.payload)
        assertEquals(envelope.eventId, lifted.eventId)
        assertEquals(envelope.idempotencyKey, lifted.idempotencyKey)
        assertEquals(envelope.recordedAtEpochMillis, lifted.recordedAtEpochMillis)
    }

    @Test
    fun `a stored row that never matched its own declared shape is reported, not repaired`() {
        val result = registry.upcast(
            RegistryFixtures.WIDGET_OBSERVED,
            fromVersion = 1,
            payload = mapOf("source" to "rpc"),
        )
        val error = result.errorOrNull()
        assertTrue(error is PlatformError.Validation)
        assertTrue(error!!.message.contains("does not match its own declared shape"))
    }

    @Test
    fun `a row from a newer build is refused rather than reinterpreted`() {
        val error = registry.upcast(RegistryFixtures.SENSOR_READING, 9, emptyMap()).errorOrNull()
        assertTrue(error is PlatformError.Validation)
        assertTrue(error!!.message.contains("must not be reinterpreted"))
    }

    @Test
    fun `every declared upcast chain produces a valid current payload`() {
        // The mechanical version of "we remembered to write the upcast": for each type, build a
        // minimal payload for every historical version and prove it reaches the current shape.
        for (schema in registry.allSchemas) {
            for (version in schema.versions) {
                val declared = requireNotNull(schema.version(version.version))
                val minimal = declared.fields
                    .filter { it.required }
                    .associate { it.name to "x" }
                val result = registry.upcast(schema.type, version.version, minimal)
                assertTrue(
                    result.isSuccess,
                    "${schema.type.name} v${version.version} does not reach the current shape: " +
                        result.errorOrNull()?.message,
                )
            }
        }
    }
}

/** Invariants of the shipped declaration, as opposed to the fixtures. */
class ReleasedEventRegistryTest {

    private val released = EventRegistry.RELEASED

    @Test
    fun `every event type constant in EventType is declared in the registry`() {
        // A type that exists in code but not in the registry is exactly the hole this phase closes:
        // it would be appendable, persistable, and undocumented.
        val constants = listOf(
            EventType.CHAIN_BLOCK_OBSERVED,
            EventType.CHAIN_BALANCE_OBSERVED,
            EventType.SIGNAL_PRODUCED,
            EventType.PREDICTION_PRODUCED,
            EventType.PREDICTION_EVALUATED,
            EventType.TREASURY_SNAPSHOT_TAKEN,
            EventType.POLICY_DECISION_RECORDED,
            EventType.RUNTIME_STATE_CHANGED,
        )
        val missing = constants.filter { released.schemaFor(it) == null }.map { it.name }
        assertTrue(missing.isEmpty(), "event types missing from the registry: $missing")
        assertEquals(constants.size, released.declaredTypes.size)
    }

    @Test
    fun `every declared schema names an owner, a purpose and a compatibility policy`() {
        released.allSchemas.forEach { schema ->
            assertTrue(schema.purpose.isNotBlank(), "${schema.type.name} has no purpose")
            assertTrue(schema.currentVersion >= 1)
            assertNotEquals(0, schema.owner.code.length)
        }
    }

    @Test
    fun `the fingerprint is stable across calls and changes when the contract changes`() {
        assertEquals(released.fingerprint(), released.fingerprint())
        val extended = EventRegistry.of(released.allSchemas + RegistryFixtures.sensorReading)
        assertNotEquals(released.fingerprint(), extended.fingerprint())
    }

    @Test
    fun `an undeclared type has no version`() {
        assertNull(released.currentVersionOf(RegistryFixtures.NEVER_REGISTERED))
    }
}

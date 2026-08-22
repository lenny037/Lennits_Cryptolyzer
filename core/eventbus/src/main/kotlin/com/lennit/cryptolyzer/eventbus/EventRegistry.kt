package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import java.security.MessageDigest

/** Payload lifted to the current schema version, with the path it took. */
public data class UpcastPayload(
    val version: Int,
    val payload: Map<String, String>,
    val appliedVersions: List<Int>,
) {
    public val wasUpcast: Boolean get() = appliedVersions.isNotEmpty()
}

/**
 * The single authority on which events exist, who owns them, and how their payloads may change.
 *
 * Why a registry at all, and why now: Phase 3 gave the log a `schemaVersion` column, but a version
 * number with nothing to compare it against is decoration. Before more persisted concepts arrive,
 * an event needs a declared owner, a declared shape per version, and a declared transformation
 * between versions. Otherwise the first payload rename silently turns two years of rows into
 * unreadable text, on a device whose database cannot be dropped and rebuilt.
 *
 * Three rules are enforced mechanically rather than in review:
 *  1. an unregistered event type cannot be appended through [RegisteredEventStore];
 *  2. a row written by a newer build is refused rather than guessed at;
 *  3. a ratified payload accepts exactly its declared fields, so drift fails at the boundary
 *     instead of surfacing as a null three modules downstream.
 *
 * The registry is data, not code generation: [render] prints it, so `docs/events/EVENT_REGISTRY.md`
 * cannot drift from the declaration (`EventRegistryDocsTest` fails if it does).
 */
public class EventRegistry private constructor(
    private val schemas: Map<EventType, EventSchema>,
) {
    public val declaredTypes: Set<EventType> get() = schemas.keys

    /** Every schema, ordered by type name so that output is deterministic. */
    public val allSchemas: List<EventSchema> get() = schemas.values.sortedBy { it.type.name }

    public fun schemaFor(type: EventType): EventSchema? = schemas[type]

    public fun currentVersionOf(type: EventType): Int? = schemas[type]?.currentVersion

    /** Validates type, version and payload of an envelope about to be appended. */
    public fun validate(envelope: EventEnvelope): Outcome<Unit> {
        val schema = schemas[envelope.type]
            ?: return unregistered(envelope.type)
        if (envelope.schemaVersion > schema.currentVersion) {
            return Outcome.failure(
                PlatformError.Validation(
                    "Event '${envelope.type.name}' is at schema version ${envelope.schemaVersion}, " +
                        "but this build declares up to ${schema.currentVersion}. Refusing to guess " +
                        "at a payload written by a newer build.",
                    field = "schemaVersion",
                ),
            )
        }
        return validatePayload(envelope.type, envelope.schemaVersion, envelope.payload)
    }

    /** Validates a payload against one specific declared version. */
    public fun validatePayload(
        type: EventType,
        version: Int,
        payload: Map<String, String>,
    ): Outcome<Unit> {
        val schema = schemas[type] ?: return unregistered(type)
        val declared = schema.version(version)
            ?: return Outcome.failure(
                PlatformError.Validation(
                    "Event '${type.name}' has no declared schema version $version " +
                        "(declared: 1..${schema.currentVersion})",
                    field = "schemaVersion",
                ),
            )
        // A draft type has an owner and a version but no settled payload yet. Accepting any payload
        // here is deliberate and visible: the alternative is inventing field names for a producer
        // that has not been written, which would be a fiction the tests would then enforce.
        if (schema.status == SchemaStatus.Draft && declared.fields.isEmpty()) {
            return Outcome.success(Unit)
        }

        val missing = (declared.requiredFieldNames - payload.keys).sorted()
        if (missing.isNotEmpty()) {
            return Outcome.failure(
                PlatformError.Validation(
                    "Event '${type.name}' v$version is missing required payload field(s): " +
                        missing.joinToString(", "),
                    field = missing.first(),
                ),
            )
        }
        val blank = declared.requiredFieldNames
            .filter { payload[it]?.isBlank() == true }
            .sorted()
        if (blank.isNotEmpty()) {
            return Outcome.failure(
                PlatformError.Validation(
                    "Event '${type.name}' v$version has blank required payload field(s): " +
                        blank.joinToString(", "),
                    field = blank.first(),
                ),
            )
        }
        val undeclared = (payload.keys - declared.fieldNames).sorted()
        if (undeclared.isNotEmpty()) {
            return Outcome.failure(
                PlatformError.Validation(
                    "Event '${type.name}' v$version carries undeclared payload field(s): " +
                        undeclared.joinToString(", ") +
                        ". Add the field to the registry in the same change that produces it.",
                    field = undeclared.first(),
                ),
            )
        }
        return Outcome.success(Unit)
    }

    /**
     * Lifts a persisted payload to the current declared version.
     *
     * The stored row is not modified. Reading is the only place a payload changes shape, which is
     * what keeps a build downgrade survivable.
     */
    public fun upcast(
        type: EventType,
        fromVersion: Int,
        payload: Map<String, String>,
    ): Outcome<UpcastPayload> {
        val schema = schemas[type] ?: return unregistered(type)
        if (fromVersion > schema.currentVersion) {
            return Outcome.failure(
                PlatformError.Validation(
                    "Stored event '${type.name}' is at schema version $fromVersion; this build " +
                        "declares up to ${schema.currentVersion}. A newer database must not be " +
                        "reinterpreted by an older build.",
                    field = "schemaVersion",
                ),
            )
        }
        when (val valid = validatePayload(type, fromVersion, payload)) {
            is Outcome.Failure -> return Outcome.failure(
                PlatformError.Validation(
                    "Stored event '${type.name}' v$fromVersion does not match its own declared " +
                        "shape: ${valid.error.message}",
                    field = (valid.error as? PlatformError.Validation)?.field,
                ),
            )
            is Outcome.Success -> Unit
        }

        val path = schema.upcastPathFrom(fromVersion)
        var current = payload
        val applied = mutableListOf<Int>()
        for (step in path) {
            val upcast = step.upcastFromPrevious ?: return Outcome.failure(
                PlatformError.InvariantViolation(
                    "Event '${type.name}' v${step.version} has no upcast; the registry should have " +
                        "refused to construct.",
                    invariant = "every version above 1 declares an upcast",
                ),
            )
            current = upcast.apply(current)
            applied += step.version
        }
        return when (val valid = validatePayload(type, schema.currentVersion, current)) {
            is Outcome.Failure -> Outcome.failure(
                PlatformError.Validation(
                    "Upcasting '${type.name}' from v$fromVersion to v${schema.currentVersion} " +
                        "produced an invalid payload: ${valid.error.message}",
                    field = (valid.error as? PlatformError.Validation)?.field,
                ),
            )
            is Outcome.Success -> Outcome.success(
                UpcastPayload(schema.currentVersion, current, applied.toList()),
            )
        }
    }

    /** Convenience for the read path: returns the envelope at the current declared version. */
    public fun upcast(envelope: EventEnvelope): Outcome<EventEnvelope> =
        when (val result = upcast(envelope.type, envelope.schemaVersion, envelope.payload)) {
            is Outcome.Failure -> result
            is Outcome.Success -> Outcome.success(
                if (!result.value.wasUpcast) {
                    envelope
                } else {
                    envelope.copy(
                        schemaVersion = result.value.version,
                        payload = result.value.payload,
                    )
                },
            )
        }

    /**
     * Canonical text form of the declaration. Order is fixed and no timestamp appears, so the
     * [fingerprint] over it changes if and only if the contract changes.
     */
    public fun canonicalForm(): String = buildString {
        allSchemas.forEach { schema ->
            append(schema.type.name).append('|')
                .append(schema.owner.code).append('|')
                .append(schema.status.name).append('|')
                .append(schema.compatibility.name).append('\n')
            schema.versions.forEach { version ->
                append("  v").append(version.version).append(':')
                version.fields.sortedBy { it.name }.forEach { field ->
                    append(' ').append(field.name)
                        .append(if (field.required) "!" else "?")
                        .append(':').append(field.classification.name)
                }
                version.upcastFromPrevious?.let { append(" <- ").append(it.description) }
                append('\n')
            }
        }
    }

    /** Stable identifier for the whole contract, printed into the registry document. */
    public fun fingerprint(): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonicalForm().toByteArray(Charsets.UTF_8))
        return "sha256:" + digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    /** The registry document, generated from the declaration. */
    public fun render(): String = buildString {
        appendLine("# Event registry")
        appendLine()
        appendLine(
            "**Generated from `EventRegistry.RELEASED` by `EventRegistry.render()`. Do not edit " +
                "by hand:** `EventRegistryDocsTest` fails when this file and the declaration " +
                "disagree. Regenerate with " +
                "`./gradlew :core:eventbus:test -Dcryptolyzer.updateEventRegistryDoc=true`.",
        )
        appendLine()
        appendLine("Registry fingerprint: `${fingerprint()}`")
        appendLine()
        appendLine(
            "`Ratified` types accept exactly their declared payload fields. `Draft` types have an " +
                "owner and a version but no settled payload yet, and accept any payload until the " +
                "phase that implements their producer ratifies them. Old versions are never " +
                "removed from this document: a persisted row from an earlier release must stay " +
                "readable, and its upcast is what makes that true.",
        )
        appendLine()
        appendLine("| Event type | Owner | Status | Compatibility | Current version |")
        appendLine("|---|---|---|---|---|")
        allSchemas.forEach { schema ->
            appendLine(
                "| `${schema.type.name}` | ${schema.owner.code} ${schema.owner.domainName} | " +
                    "${schema.status.name} | ${schema.compatibility.name} | " +
                    "${schema.currentVersion} |",
            )
        }
        allSchemas.forEach { schema ->
            appendLine()
            appendLine("## `${schema.type.name}`")
            appendLine()
            appendLine("${schema.purpose}")
            appendLine()
            appendLine(
                "Owner: **${schema.owner.code} ${schema.owner.domainName}** · " +
                    "Status: **${schema.status.name}** · " +
                    "Compatibility: **${schema.compatibility.name}**",
            )
            schema.versions.forEach { version ->
                appendLine()
                appendLine("### Version ${version.version}")
                if (version.note.isNotBlank()) {
                    appendLine()
                    appendLine(version.note)
                }
                version.upcastFromPrevious?.let { upcast ->
                    appendLine()
                    appendLine("Upcast from v${version.version - 1} on read: ${upcast.description}.")
                }
                appendLine()
                if (version.fields.isEmpty()) {
                    appendLine("No payload fields are declared yet (draft).")
                } else {
                    appendLine("| Field | Required | Classification | Description |")
                    appendLine("|---|---|---|---|")
                    version.fields.forEach { field ->
                        appendLine(
                            "| `${field.name}` | ${if (field.required) "yes" else "no"} | " +
                                "${field.classification.name} | ${field.description} |",
                        )
                    }
                }
            }
        }
    }

    public companion object {

        public fun of(schemas: List<EventSchema>): EventRegistry {
            val duplicates = schemas
                .groupingBy { it.type.name }
                .eachCount()
                .filterValues { it > 1 }
                .keys
            require(duplicates.isEmpty()) { "duplicate schemas declared for: $duplicates" }
            return EventRegistry(schemas.associateBy { it.type })
        }

        public fun of(vararg schemas: EventSchema): EventRegistry = of(schemas.toList())

        private fun unregistered(type: EventType): Outcome<Nothing> = Outcome.failure(
            PlatformError.Validation(
                "Event type '${type.name}' is not in the event registry. Declare it in " +
                    "EventRegistry.RELEASED, with an owner and a payload contract, in the same " +
                    "change that produces it.",
                field = "type",
            ),
        )

        /**
         * The shipped declaration.
         *
         * Every type that the platform can currently persist appears here. Field lists are
         * deliberately empty where no producer exists yet: the phase that writes the producer
         * ratifies the payload in the same change, so the registry never claims more than the code
         * delivers. `PLAN_V2.md` names the phase per type.
         */
        public val RELEASED: EventRegistry = of(
            EventSchema(
                type = EventType.CHAIN_BLOCK_OBSERVED,
                owner = ModuleId.BlockchainDataPlane,
                status = SchemaStatus.Draft,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "A block header observed on a configured chain. Read-only observation; " +
                    "ratified by Phase 7 with the RPC adapter that produces it.",
                versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
            ),
            EventSchema(
                type = EventType.CHAIN_BALANCE_OBSERVED,
                owner = ModuleId.BlockchainDataPlane,
                status = SchemaStatus.Draft,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "An account balance read at a known block. Ratified by Phase 7. " +
                    "Amounts are decimal strings, never floating point (ADR-0003).",
                versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
            ),
            EventSchema(
                type = EventType.SIGNAL_PRODUCED,
                owner = ModuleId.DataPipeline,
                status = SchemaStatus.Draft,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "A normalized signal emitted by the intelligence fabric. Ratified by " +
                    "Phase 7 together with the Signal normalizers.",
                versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
            ),
            EventSchema(
                type = EventType.PREDICTION_PRODUCED,
                owner = ModuleId.Prediction,
                status = SchemaStatus.Draft,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "A bounded prediction with its model version and feature fingerprint. " +
                    "Ratified by Phase 10, which cannot ship without provenance.",
                versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
            ),
            EventSchema(
                type = EventType.PREDICTION_EVALUATED,
                owner = ModuleId.Prediction,
                status = SchemaStatus.Draft,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "The realized outcome of an earlier prediction. Exists from Phase 3 " +
                    "onwards so calibration is measurable later; ratified by Phase 10.",
                versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
            ),
            EventSchema(
                type = EventType.TREASURY_SNAPSHOT_TAKEN,
                owner = ModuleId.Treasury,
                status = SchemaStatus.Draft,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "A point-in-time treasury balance and reservation snapshot. Ratified " +
                    "with the treasury read model in Phase 9.",
                versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
            ),
            EventSchema(
                type = EventType.POLICY_DECISION_RECORDED,
                owner = ModuleId.SecurityAndRisk,
                status = SchemaStatus.Draft,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "The audit record of a policy decision: request, verdict, and deciding " +
                    "rule. Ratified by Phase 8b, which requires every decision to be auditable.",
                versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
            ),
            EventSchema(
                type = EventType.RUNTIME_STATE_CHANGED,
                owner = ModuleId.Orchestration,
                status = SchemaStatus.Draft,
                compatibility = CompatibilityPolicy.AdditiveOnly,
                purpose = "A service lifecycle transition, including degraded health. Ratified " +
                    "with the diagnostics surface in Phase 6.",
                versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
            ),
        )
    }
}

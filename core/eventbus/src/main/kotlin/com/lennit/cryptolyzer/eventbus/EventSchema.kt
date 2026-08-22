package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.contracts.ModuleId

/**
 * Redaction class of a single payload field.
 *
 * Ordering is meaningful: [Sensitive] is stricter than [Operational], which is stricter than
 * [Public]. [EventSchema] uses that ordering to refuse a schema revision that would weaken an
 * existing field's classification, because a redaction rule that a later version can quietly
 * relax is not a control.
 */
public enum class DataClassification {
    /** Safe in a shared diagnostics bundle: block heights, chain ids, model versions. */
    Public,

    /** Internal operational detail. Retained locally, excluded from any optional upload. */
    Operational,

    /** Never leaves the device in raw form and is redacted in diagnostics exports. */
    Sensitive,
    ;

    public val redactedInDiagnostics: Boolean get() = this == Sensitive
}

/** One declared key of an event payload. */
public data class PayloadField(
    val name: String,
    val required: Boolean,
    val classification: DataClassification,
    val description: String,
) {
    init {
        require(name.matches(NAME_PATTERN)) {
            "Payload field must be lowercase snake_case, got '$name'"
        }
        require(description.isNotBlank()) { "Payload field '$name' needs a description" }
    }

    private companion object {
        val NAME_PATTERN: Regex = Regex("^[a-z][a-z0-9_]*$")
    }
}

/**
 * A declarative payload transformation from schema version `n` to `n + 1`.
 *
 * Upcasts are declarative rather than arbitrary lambdas so that they can be printed into
 * documentation, compared, and reasoned about in review. They run on read, never on the stored
 * row: an applied migration rewrites nothing, so a downgrade to an older build still finds the
 * bytes it wrote (see ADR-0015).
 */
public interface PayloadUpcast {
    /** Human-readable form, rendered into the event registry document. */
    public val description: String

    public fun apply(payload: Map<String, String>): Map<String, String>
}

/** Renames a key, leaving the value untouched. A no-op when [from] is absent. */
public data class RenameField(val from: String, val to: String) : PayloadUpcast {
    init {
        require(from.isNotBlank() && to.isNotBlank()) { "rename needs both keys" }
        require(from != to) { "rename from and to are identical: '$from'" }
    }

    override val description: String get() = "rename `$from` to `$to`"

    override fun apply(payload: Map<String, String>): Map<String, String> {
        val value = payload[from] ?: return payload
        return payload.minus(from).plus(to to value)
    }
}

/** Supplies a value for a key introduced by the newer version. Never overwrites. */
public data class DefaultField(val name: String, val value: String) : PayloadUpcast {
    override val description: String get() = "default `$name` to `$value` when absent"

    override fun apply(payload: Map<String, String>): Map<String, String> =
        if (payload.containsKey(name)) payload else payload.plus(name to value)
}

/** Removes a key that the newer version no longer declares. */
public data class DropField(val name: String) : PayloadUpcast {
    override val description: String get() = "drop `$name`"

    override fun apply(payload: Map<String, String>): Map<String, String> = payload.minus(name)
}

/** Applies several steps in declaration order. */
public data class UpcastSteps(val steps: List<PayloadUpcast>) : PayloadUpcast {
    public constructor(vararg steps: PayloadUpcast) : this(steps.toList())

    init {
        require(steps.isNotEmpty()) { "an upcast needs at least one step" }
    }

    override val description: String get() = steps.joinToString("; ") { it.description }

    override fun apply(payload: Map<String, String>): Map<String, String> =
        steps.fold(payload) { current, step -> step.apply(current) }
}

/** The declared shape of one payload version. */
public data class EventSchemaVersion(
    val version: Int,
    val fields: List<PayloadField>,
    val upcastFromPrevious: PayloadUpcast? = null,
    val note: String = "",
) {
    init {
        require(version >= 1) { "schema versions start at 1" }
        val duplicates = fields.groupingBy { it.name }.eachCount().filterValues { it > 1 }.keys
        require(duplicates.isEmpty()) { "duplicate payload fields declared: $duplicates" }
    }

    public val fieldNames: Set<String> get() = fields.map { it.name }.toSet()

    public val requiredFieldNames: Set<String>
        get() = fields.filter { it.required }.map { it.name }.toSet()

    public fun field(name: String): PayloadField? = fields.firstOrNull { it.name == name }
}

/**
 * How later versions of an event may differ from earlier ones.
 *
 * The distinction is recorded per event type because the two cases carry different review
 * obligations: an [AdditiveOnly] revision cannot break an older consumer, while a [Transforming]
 * one can and therefore has to justify itself in the pull request that introduces it.
 */
public enum class CompatibilityPolicy {
    /** Fields may be added. An existing field may never be removed. */
    AdditiveOnly,

    /** Fields may be renamed or removed, provided the version's upcast handles it. */
    Transforming,
}

/**
 * Whether the payload contract is settled.
 *
 * [Draft] exists so the registry can be honest: an event type that no producer implements yet has
 * an owner, a name and a version, but its field list is not yet knowable. Draft types accept any
 * payload; [Ratified] types accept only their declared fields. `EventRegistryTest` fails if a
 * ratified schema declares no fields, so ratification cannot be a rubber stamp.
 */
public enum class SchemaStatus { Draft, Ratified }

/**
 * The registry entry for one event type: who owns it, how it may evolve, and every version of its
 * payload that has ever been persisted.
 *
 * Old versions are never deleted from this list. The event log outlives the code that wrote it, so
 * the only way to keep a two-year-old row readable is to keep its shape and its upcast declared.
 */
public data class EventSchema(
    val type: EventType,
    val owner: ModuleId,
    val status: SchemaStatus,
    val compatibility: CompatibilityPolicy,
    val versions: List<EventSchemaVersion>,
    val purpose: String,
) {
    init {
        require(versions.isNotEmpty()) { "${type.name} must declare at least version 1" }
        require(versions.map { it.version } == List(versions.size) { it + 1 }) {
            "${type.name} versions must be contiguous starting at 1, got " +
                versions.map { it.version }
        }
        require(versions.first().upcastFromPrevious == null) {
            "${type.name} version 1 cannot have an upcast from a previous version"
        }
        require(purpose.isNotBlank()) { "${type.name} needs a purpose" }
        for (declared in versions.drop(1)) {
            require(declared.upcastFromPrevious != null) {
                "${type.name} version ${declared.version} needs an upcast from " +
                    "version ${declared.version - 1}: a persisted row cannot upgrade itself"
            }
        }
        if (status == SchemaStatus.Ratified) {
            require(versions.last().fields.isNotEmpty()) {
                "${type.name} is ratified but declares no payload fields"
            }
        }
        for ((older, newer) in versions.zipWithNext()) {
            if (compatibility == CompatibilityPolicy.AdditiveOnly) {
                val removed = older.fieldNames - newer.fieldNames
                require(removed.isEmpty()) {
                    "${type.name} is AdditiveOnly but version ${newer.version} removes $removed"
                }
            }
            for (old in older.fields) {
                val current = newer.field(old.name) ?: continue
                require(current.classification >= old.classification) {
                    "${type.name} v${newer.version} weakens the classification of `${old.name}` " +
                        "from ${old.classification} to ${current.classification}: " +
                        "redaction must never regress"
                }
            }
        }
    }

    public val currentVersion: Int get() = versions.size

    public fun version(version: Int): EventSchemaVersion? =
        versions.firstOrNull { it.version == version }

    public fun sensitiveFieldsAt(version: Int): Set<String> =
        version(version)
            ?.fields
            ?.filter { it.classification.redactedInDiagnostics }
            ?.map { it.name }
            ?.toSet()
            .orEmpty()

    /** Upcasts declared between [fromVersion] (exclusive) and [currentVersion] (inclusive). */
    public fun upcastPathFrom(fromVersion: Int): List<EventSchemaVersion> =
        versions.filter { it.version > fromVersion }
}

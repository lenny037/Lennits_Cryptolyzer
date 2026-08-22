package com.lennit.cryptolyzer.eventbus.testing

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.eventbus.CompatibilityPolicy
import com.lennit.cryptolyzer.eventbus.DataClassification
import com.lennit.cryptolyzer.eventbus.DefaultField
import com.lennit.cryptolyzer.eventbus.EventEnvelope
import com.lennit.cryptolyzer.eventbus.EventRegistry
import com.lennit.cryptolyzer.eventbus.EventSchema
import com.lennit.cryptolyzer.eventbus.EventSchemaVersion
import com.lennit.cryptolyzer.eventbus.EventType
import com.lennit.cryptolyzer.eventbus.PayloadField
import com.lennit.cryptolyzer.eventbus.RenameField
import com.lennit.cryptolyzer.eventbus.SchemaStatus
import com.lennit.cryptolyzer.eventbus.UpcastSteps

/**
 * A multi-version registry used to verify the compatibility machinery.
 *
 * Deliberately separate from `EventRegistry.RELEASED`. The shipped registry must describe only
 * what the platform actually produces, so it cannot carry a three-version type invented to give
 * the upcast path something to chew on. These fixtures carry that instead, and the persistence
 * fixture databases are written against them — which is also why an old fixture row can be
 * checked against a current declaration without touching production contracts.
 *
 * Versions declared here are frozen. A retained fixture database references them, and editing a
 * released version's shape is exactly the mistake the Phase 4 gate exists to catch.
 */
public object RegistryFixtures {

    /** Three versions: a rename, then an added required field with a default. */
    public val WIDGET_OBSERVED: EventType = EventType("test.widget_observed")

    /** Two versions, additive only: an optional field appears in v2. */
    public val LEDGER_ENTRY: EventType = EventType("test.ledger_entry")

    /** Ratified, single version. Used for the unregistered/undeclared-field cases. */
    public val SENSOR_READING: EventType = EventType("test.sensor_reading")

    /** Owner declared, payload not settled. Accepts any payload. */
    public val DRAFT_ONLY: EventType = EventType("test.draft_only")

    /** Never registered. Used to prove the write path refuses unknown types. */
    public val NEVER_REGISTERED: EventType = EventType("test.never_registered")

    public val widgetObserved: EventSchema = EventSchema(
        type = WIDGET_OBSERVED,
        owner = ModuleId.BlockchainDataPlane,
        status = SchemaStatus.Ratified,
        compatibility = CompatibilityPolicy.Transforming,
        purpose = "Fixture type exercising a rename and an added required field.",
        versions = listOf(
            EventSchemaVersion(
                version = 1,
                fields = listOf(
                    PayloadField("source", required = true, DataClassification.Public, "Producer name."),
                    PayloadField("value_raw", required = true, DataClassification.Operational, "Observed value."),
                ),
            ),
            EventSchemaVersion(
                version = 2,
                fields = listOf(
                    PayloadField("source", required = true, DataClassification.Public, "Producer name."),
                    PayloadField("value", required = true, DataClassification.Operational, "Observed value."),
                ),
                upcastFromPrevious = RenameField(from = "value_raw", to = "value"),
                note = "`value_raw` became `value`.",
            ),
            EventSchemaVersion(
                version = 3,
                fields = listOf(
                    PayloadField("source", required = true, DataClassification.Public, "Producer name."),
                    PayloadField("value", required = true, DataClassification.Operational, "Observed value."),
                    PayloadField("unit", required = true, DataClassification.Public, "Unit of the value."),
                ),
                upcastFromPrevious = UpcastSteps(DefaultField("unit", "wei")),
                note = "`unit` became required; historical rows are wei by definition.",
            ),
        ),
    )

    public val ledgerEntry: EventSchema = EventSchema(
        type = LEDGER_ENTRY,
        owner = ModuleId.Treasury,
        status = SchemaStatus.Ratified,
        compatibility = CompatibilityPolicy.AdditiveOnly,
        purpose = "Fixture type exercising a purely additive revision.",
        versions = listOf(
            EventSchemaVersion(
                version = 1,
                fields = listOf(
                    PayloadField("amount", required = true, DataClassification.Operational, "Decimal amount."),
                ),
            ),
            EventSchemaVersion(
                version = 2,
                fields = listOf(
                    PayloadField("amount", required = true, DataClassification.Operational, "Decimal amount."),
                    PayloadField("memo", required = false, DataClassification.Sensitive, "Free-text memo."),
                ),
                upcastFromPrevious = UpcastSteps(DefaultField("memo", "")),
                note = "Optional `memo` added; absent on historical rows.",
            ),
        ),
    )

    public val sensorReading: EventSchema = EventSchema(
        type = SENSOR_READING,
        owner = ModuleId.Ingestion,
        status = SchemaStatus.Ratified,
        compatibility = CompatibilityPolicy.AdditiveOnly,
        purpose = "Fixture type with one settled version.",
        versions = listOf(
            EventSchemaVersion(
                version = 1,
                fields = listOf(
                    PayloadField("sensor", required = true, DataClassification.Public, "Sensor id."),
                    PayloadField("reading", required = true, DataClassification.Operational, "Raw reading."),
                    PayloadField("note", required = false, DataClassification.Operational, "Optional note."),
                ),
            ),
        ),
    )

    public val draftOnly: EventSchema = EventSchema(
        type = DRAFT_ONLY,
        owner = ModuleId.Analytics,
        status = SchemaStatus.Draft,
        compatibility = CompatibilityPolicy.AdditiveOnly,
        purpose = "Fixture type whose producer does not exist yet.",
        versions = listOf(EventSchemaVersion(version = 1, fields = emptyList())),
    )

    public val registry: EventRegistry =
        EventRegistry.of(widgetObserved, ledgerEntry, sensorReading, draftOnly)

    /** Envelope builder with the mechanical fields filled in. */
    public fun envelope(
        eventId: String,
        type: EventType,
        schemaVersion: Int,
        payload: Map<String, String>,
        producer: ModuleId = ModuleId.Ingestion,
        occurredAtEpochMillis: Long = 1_700_000_000_000,
        idempotencyKey: String = eventId,
    ): EventEnvelope = EventEnvelope(
        eventId = eventId,
        type = type,
        schemaVersion = schemaVersion,
        producer = producer,
        occurredAtEpochMillis = occurredAtEpochMillis,
        recordedAtEpochMillis = occurredAtEpochMillis,
        idempotencyKey = idempotencyKey,
        payload = payload,
    )
}

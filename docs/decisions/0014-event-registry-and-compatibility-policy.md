# ADR-0014: Declare event contracts in a machine-checked registry with per-type compatibility policy

## Status

Accepted.

## Context

ADR-0005 made the event log append-only and ADR-0010 committed to versioned events and forward-only
migrations, but nothing in the build described what a given event's payload contains. `EventEnvelope`
carries `payload: Map<String, String>` and a `schemaVersion` integer, and until now both were
unconstrained: any producer could write any keys under any version number, and a consumer had no
declared shape to read against. `EventType` also listed eight types that no document described.

That is a durability problem rather than a tidiness problem. The event log is retained forever on a
device that cannot be migrated by an operator, so every payload written today is an input to code
written years later. Undocumented payloads become undecodable payloads, and the failure surfaces as
a consumer silently reading a key that no longer exists.

`docs/PLAN_CRITIQUE.md` also recorded that this repository has already been harmed by documentation
that nothing verified.

## Decision

Declare every event contract in `EventRegistry.RELEASED`:

- Each `EventSchema` names an owning `ModuleId`, a purpose, a `CompatibilityPolicy`, a
  `SchemaStatus`, and one `EventSchemaVersion` per released version.
- Each version declares its `PayloadField`s with a required flag and a `DataClassification`, and —
  for every version above 1 — a `PayloadUpcast` from the previous version. The declaration refuses to
  construct without one, because a stored row cannot upgrade itself.
- `AdditiveOnly` types may not remove a field; `Transforming` types may rename or drop, and must
  reconstruct the current shape in the upcast. A field's classification may be tightened, never
  loosened.
- Enforcement is a decorator, `RegisteredEventStore`, wrapping any `EventStore`. It validates on
  append and lifts payloads to the current version on read.
- Stored rows are never rewritten. Upcasts apply on read only.
- An event that cannot be lifted is dead-lettered, not dropped and not retried forever.
- Types whose producer does not exist yet are `Draft`: owner and version declared, payload not
  settled, any payload accepted. The phase that implements the producer ratifies the schema.
- `docs/events/EVENT_REGISTRY.md` is generated from the declaration and verified by
  `EventRegistryDocsTest`, so it cannot drift.

## Consequences

A payload change is now a visible, reviewable, test-gated edit to a declaration, and a consumer can
rely on receiving the current shape regardless of when a row was written. `EventRegistry.fingerprint()`
gives the contract surface a stable identity for later replay work (Phase 5).

Costs, stated plainly: producers must declare fields before writing them; the registry is a second
place to edit when a payload changes; and `Draft` types provide no payload guarantee at all until
their producing phase ratifies them, so the registry currently documents ownership more than shape.

Enforcement is opt-in per store instance. A component wired directly to `SqliteEventStore` bypasses
the registry. That is intentional — the storage contract suite must be able to exercise arbitrary
payloads — but it means the runtime wiring is what makes the guarantee real, and Phase 5 must wire it.

## Alternatives considered

1. **Enforce inside `SqliteEventStore`.** Rejected: it would couple storage to schema policy and
   would force the shared `EventStoreContract` (which deliberately appends arbitrary payloads) to be
   weakened to keep passing. A weakened contract suite is a worse outcome than an opt-in decorator.
2. **A serialization framework with generated schemas** (protobuf, Avro, kotlinx-serialization with
   a schema registry). Rejected for now: the payload is a flat string map by ADR decision, the codec
   must stay reflection-free and deterministic on a battery-constrained device, and none of these
   would supply the upcast-on-read behaviour that the durability requirement actually needs.
3. **Rewrite rows in place during migration.** Rejected: it destroys the bytes a downgraded build
   would need, converts a read-path concern into a write amplification event on a phone, and makes
   the log no longer a verbatim record of what was observed.
4. **Document payloads in prose only.** Rejected: this repository has already demonstrated what
   unverified documentation is worth.
5. **Ratify all eight event types now.** Rejected: it would mean inventing fields for producers that
   do not exist, and the implementing phase would have to break the contract to write anything real.

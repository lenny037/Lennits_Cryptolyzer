# ADR-0010: Events are schema-versioned and persistence migrations are forward-only

## Status

Accepted.

## Context

`EventEnvelope` requires `schemaVersion` of at least one. SQLite migrations are numbered, applied in order, and documented to forbid edits to shipped migrations and deletion of event rows. `PayloadCodec` creates deterministic, escaped map encoding.

## Decision

Every persisted event has an explicit event schema version. Database changes are additive, numbered, and forward-only: never edit a released migration, append a new migration instead. Maintain fixture databases and retained event samples for supported versions. Define compatibility handling before changing a payload reader or writer.

## Consequences

Installed local data is treated as durable product state rather than disposable cache. Migration work and compatibility tests become mandatory release work. Rollback may require a compatible reader or an export/restore path rather than a destructive database reset.

## Alternatives considered

1. Unversioned payloads with best-effort decoding: rejected because old events become ambiguous after evolution.
2. Editing an old migration before release distribution: rejected because it becomes unsafe once any installation has applied it.
3. Dropping and recreating the database: rejected because it destroys financial and audit evidence.

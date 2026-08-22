# ADR-0005: Use a durable append-only event log with idempotency keys and lease-based claiming

## Status

Accepted.

## Context

A phone can lose process state during background work. `EventEnvelope` requires a schema version and idempotency key. `EventStore` has append, claim, terminal-state, retry, and lease-release operations but no delete operation. Both in-memory and SQLite stores execute the shared `EventStoreContract`; SQLite enables WAL and `synchronous=FULL`.

## Decision

Persist every operational fact in an append-only local event log. Producers supply stable idempotency keys. Consumers claim due events with time-bounded leases; expired leases are claimable again. Processed and dead-lettered events are retained. Retention or export policy may add derived storage management, but must not silently delete the event/audit record.

## Consequences

The system provides at-least-once delivery and requires idempotent handlers. Process death is recoverable without treating a transient in-flight state as lost work. Storage growth must be budgeted and surfaced to the operator.

## Alternatives considered

1. Process-local pub/sub: rejected because it loses work on process death.
2. Destructive queue acknowledgement: rejected because it removes diagnostic and replay evidence.
3. Exactly-once execution claims: rejected because they are not credible across local process death and external side effects.

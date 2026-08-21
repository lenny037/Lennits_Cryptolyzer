# ADR-0006: Time is injected through a Clock port

## Status

Accepted.

## Context

Event ordering, retry schedules, policy audit records, simulation freshness, and runtime health all depend on time. `Clock` provides the time port and `MutableClock` supports tests and replay. Event factories and policy/runtime code already accept a clock dependency.

## Decision

Core code obtains time only through `Clock` or a higher-level dependency that receives `Clock`. Tests use `MutableClock` or an equivalent deterministic implementation. A system clock is an adapter at the composition boundary. Randomized retry spread must derive from deterministic inputs when reproducibility is required.

## Consequences

The same inputs can produce reproducible records and decision outcomes. Construction is more explicit and adapters must be wired deliberately. Clock integrity remains a system concern; injected time is not proof that device wall time is trustworthy.

## Alternatives considered

1. Direct wall-clock reads in every module: rejected because replay and timing tests become nondeterministic.
2. Global time singleton: rejected because it hides dependencies and is difficult to substitute safely.
3. Timestamp-free events: rejected because offline ordering, expiry, and audit needs require time.

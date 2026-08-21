# ADR-0004: Expected failures are Outcome values, not exceptions

## Status

Accepted.

## Context

`core:contracts` defines `Outcome.Success` and `Outcome.Failure`, with a classified `PlatformError` hierarchy. Event dispatch uses `retryable` and severity to choose retry or dead-letter handling. The policy engine converts an unexpected rule failure into a denial.

## Decision

Every fallible boundary with an expected operational failure returns `Outcome<T>` and a `PlatformError`. Expected validation, transport, upstream, storage, timeout, policy-refusal, and invariant outcomes must not rely on exceptions for normal control flow. Exceptions remain appropriate for programmer defects, unrecoverable platform failure, and coroutine cancellation; cancellation must propagate.

## Consequences

Callers must handle success and failure explicitly, and retry behavior can be mechanical rather than message-based. API signatures are more verbose. A broad catch that converts all defects into a generic success or ignores them is prohibited.

## Alternatives considered

1. Exception-only control flow: rejected because expected failures become easy to lose and difficult to classify.
2. Kotlin `Result`: rejected because it carries arbitrary throwables rather than the platform error taxonomy.
3. Nullable results: rejected because they lose error category and retryability.

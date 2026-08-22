# ADR-0007: Policy is fail-closed and mediates every value-moving action

## Status

Accepted.

## Context

`core:policy` supplies ordered rules and `PolicyEngine` returns denial if no rule matches or a rule throws. Its default set includes safe mode, approval, simulation, limits, treasury coverage, expected value, confidence, and final allow rules. `AuditRecord` records every decision. No signer implementation was found in the inspected core.

## Decision

Every value-moving request must enter through a policy gateway before it can reach a signer or execution adapter. A strategy may create a request and receive a decision, but it may not hold or call a signer directly. Denial is the default for missing, malformed, unavailable, or exceptional control input. Every decision must be recorded as an auditable fact.

## Consequences

New execution adapters need an explicit composition path and tests that prove direct strategy-to-signer access is impossible. Fail-closed behavior can refuse actions during outages or incomplete configuration, which is preferred to unsafe fallback.

## Alternatives considered

1. Strategy-owned signing: rejected because it bypasses shared limits, approval, and audit controls.
2. Fail-open when policy is unavailable: rejected because an unavailable safeguard is not a pass.
3. Advisory risk scoring only: rejected because a score without enforcement does not mediate value movement.

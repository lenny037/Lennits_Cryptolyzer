# ADR-0008: v1 is observation and simulation only; no autonomous on-chain execution

## Status

Accepted.

## Context

The generic core policy model includes `Transact` so that policy semantics can be tested, but the inspected core contains no signer. A phone can be stolen, malware-compromised, rooted, or killed and restarted under background constraints. A hot key with autonomous spend authority would convert those risks into direct custody loss.

## Decision

The v1 release scope is read-only observation and simulation. It must not include autonomous on-chain execution, unattended transaction submission, or a device hot key with autonomous spend authority. Any future value movement requires a hardware-backed key boundary, explicit user approval for the specific action, policy mediation, and an audit record. This ADR does not authorize such future functionality.

## Consequences

Strategy and intelligence modules can analyze and simulate opportunities but cannot monetize them autonomously in v1. Product claims, UI copy, connector designs, and tests must not imply unattended execution. A future custody design needs a new ADR, threat model, legal review, and device-level verification.

## Alternatives considered

1. Autonomous executor with a local hot key: rejected because phone compromise and background instability create unacceptable custody exposure.
2. Cloud-held execution key: rejected for v1 because it adds hosted custody and conflicts with local-first control.
3. Manual transaction drafting without hardware-backed approval: rejected because it weakens the intended approval boundary.

# ADR-0011: CI is a Phase 0.5 gate, not a Phase 19 activity

## Status

Accepted.

## Context

The original reconstruction plan scheduled deployment normalization after implementation phases. The current root build already provides `architectureGuard` and `verifyAll`, and existing test reports cover 183 passing JVM tests across five tested core modules. A local command alone does not demonstrate a protected CI process.

## Decision

Phase 0.5 requires CI to run the full local verification gate on every proposed change before feature sequencing continues. CI must use the documented JDK/runtime environment, retain test and lint artifacts, and prevent merging when the required gate fails. Device checks are added when the Android presentation exists.

## Consequences

Broken architectural dependencies or core tests are found near the change that introduced them. CI configuration becomes maintained product infrastructure, not a release-week task. Build time and dependency caching require active stewardship.

## Alternatives considered

1. Defer CI until deployment: rejected because earlier phases then lack enforceable verification.
2. Run only unit tests in CI: rejected because architecture rules and module checks can regress independently.
3. Depend on developer-local verification: rejected because it is not consistently reproducible or enforceable.

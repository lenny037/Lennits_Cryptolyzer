# ADR-0013: Use Android-only presentation now; preserve a KMP option through a pure JVM core

## Status

Accepted.

## Context

The product direction is Android-first. The present core is Kotlin/JVM and has no Android dependencies, while secure storage, biometrics, UI, notifications, and lifecycle integration are Android-specific. KMP could later broaden supported platforms but would impose current build, API, testing, and dependency constraints without an identified non-Android presentation requirement.

## Decision

Build the presentation and platform adapters for Android now. Keep core contracts and domain logic pure Kotlin/JVM with platform boundaries, so a future KMP evaluation starts from separated logic rather than Android-coupled business rules. Do not claim present KMP compatibility or add KMP targets until a supported-platform requirement, maintenance owner, and acceptance matrix are approved.

## Consequences

The first presentation can focus on Android accessibility, Keystore, biometrics, and background constraints. Some JVM APIs chosen today may need abstraction for KMP later; that is an accepted tradeoff tracked at module boundaries. No iOS, desktop, or web delivery promise follows from this ADR.

## Alternatives considered

1. Full KMP immediately: deferred because current scope does not justify the added platform and test surface.
2. Android-dependent business logic: rejected because it would make later reuse costly and weaken JVM tests.
3. Web or React presentation first: rejected because it conflicts with the Android-first direction.

# ADR-0002: The core is pure Kotlin/JVM; Android is a presentation module only

## Status

Accepted.

## Context

The repository contains seven Kotlin/JVM core modules. The convention plugin enables explicit API mode, warnings as errors, and JVM target 17. `architectureGuard` blocks Android and AndroidX dependencies from core. Android still owns platform functions such as UI, Keystore integration, biometrics, notifications, and OS scheduling.

## Decision

Keep contracts, domain, telemetry, eventbus, persistence, runtime, and policy pure Kotlin/JVM. Place Compose UI, Android framework APIs, secure-storage implementations, and lifecycle adapters in Android presentation/platform modules. Core interfaces define the seam; Android supplies implementations.

## Consequences

Core behavior is unit-testable without an emulator and has clearer dependency boundaries. Android integration requires adapters rather than direct framework calls. This decision does not claim that a complete Android presentation module already exists.

## Alternatives considered

1. Android dependencies throughout the core: rejected because it would make deterministic JVM testing and portability harder.
2. A server-first Kotlin core: rejected because it reverses the Android-first, local-first priority.
3. A full KMP presentation now: deferred by ADR-0013.

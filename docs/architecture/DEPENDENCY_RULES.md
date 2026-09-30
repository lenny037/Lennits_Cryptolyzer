# Dependency Rules

## Canonical layer order

```text
presentation -> application -> domain -> infrastructure -> platform
```

Dependencies may point downward only. Domain code must not depend on Android UI, network clients, database implementations, cloud SDKs, or blockchain providers.

## Runtime rules

1. Core application behavior must work without network connectivity.
2. Firebase and Firestore are prohibited from the canonical core runtime.
3. Remote services must be accessed through explicit adapter interfaces.
4. Private keys and signing material must remain outside blockchain observation adapters.
5. Financial decisions must pass through risk and execution policy boundaries.
6. Background work must be idempotent and restart-safe.
7. Durable events must not be acknowledged before successful processing.
8. Unknown event types remain pending until a compatible handler is available.
9. Numeric domains involving blockchain quantities must not rely on fixed-width integer assumptions where uint256 values are possible.
10. Model providers are adapters; model-specific code must not leak into domain contracts.
11. Persistence implementations are replaceable behind repository interfaces where practical.
12. New dependencies require a documented reason based on correctness, security, performance, or maintainability.

## Mobile constraints

The Android runtime is the primary deployment target. New components must account for process death, limited background execution, battery consumption, storage growth, memory pressure, intermittent connectivity, and incremental application upgrades.

## Definition of done

A component is complete only when its implementation, validation, tests, documentation, and repository commit are present. Placeholder classes do not satisfy completion.

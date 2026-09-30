# ADR-001: Firebase-Free, Local-First Runtime

- Status: Accepted
- Date: 2026-08-21
- Scope: Canonical application architecture

## Decision

LENNIT_CRYPTOLYZER will not require Firebase or Firestore for core operation.

The Android/mobile runtime is local-first. Durable application state is owned by the local persistence boundary, with Room/SQLite as the initial implementation. Remote infrastructure is an optional adapter for synchronization, backup, institutional analytics, or distributed workloads.

## Rationale

The historical AFSU specification defined Firebase and Firestore as foundational infrastructure. That architecture is superseded for the rebuilt platform because it conflicts with the current mobile-first and low-cost requirements and creates unnecessary coupling between application correctness and a managed cloud backend.

The rebuilt architecture must continue to operate when network connectivity is unavailable and must not require a cloud service for core event processing, local intelligence, risk evaluation, or application state.

## Canonical boundaries

```text
Android Runtime
  -> Domain Contracts
  -> Room/SQLite
  -> Durable Event Store
  -> Local Intelligence
  -> Risk/Execution Policy
  -> Optional Sync Adapter
```

Remote systems must be implemented behind explicit interfaces. No Firebase SDK, Firestore collection, Firebase Function, or Firebase-specific authentication mechanism may become a required dependency of the core runtime.

## Data strategy

- Local state: Room/SQLite.
- Secrets/credentials: Android Keystore-backed secure storage.
- Events: durable local event store with retry semantics.
- Synchronization: optional, idempotent sync queue and adapter.
- Institutional/large-scale analytics: optional remote data platform behind an adapter.
- AI/model providers: provider-agnostic interfaces; local inference is preferred when device performance and model size justify it.

## Consequences

### Positive

- Offline operation is a first-class capability.
- Lower recurring infrastructure cost.
- Reduced vendor lock-in.
- Deterministic local behavior.
- Easier local testing and reproducibility.
- Cloud availability cannot directly disable core application behavior.

### Negative

- Synchronization becomes an explicit engineering problem.
- Conflict resolution and remote backup must be designed rather than delegated to Firestore.
- Large-scale distributed workloads require a separate deployment architecture.

## Migration rule

Legacy Firebase/Firestore code and configuration are migration inputs only. They must not be copied into the canonical runtime without an explicit architectural justification and adapter boundary.

## Acceptance criteria

1. Core Android runtime builds without Firebase or Firestore dependencies.
2. Core domain/event processing works with network unavailable.
3. Local state remains durable across process/application restarts.
4. Remote synchronization is optional and replaceable.
5. No secrets are embedded in source or local database records in plaintext.
6. CI verifies the absence of prohibited Firebase/Firestore dependencies in the core build.

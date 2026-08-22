# ADR-0001: Firebase and Firestore are excluded from core

## Status

Accepted.

## Context

The owner direction is local-first and Firebase/Firestore-free. The legacy root documents prescribe Firestore as system state, Cloud Functions as execution, and Pub/Sub as the event backbone; the inventory and deletion manifest classify that direction as superseded. The current `architectureGuard` rejects Firebase, Google Mobile Services, Firestore, AndroidX, and Android dependencies in `:core` modules.

## Decision

Core modules must not depend on Firebase, Firestore, Cloud Functions, Pub/Sub, or Firebase configuration. Local persistence is the default authority. Any future hosted capability is an opt-in adapter outside core, must tolerate absence, and may not replace local correctness or offline operation.

## Consequences

The core can be built and tested without backend credentials or a Firebase project. Synchronization, remote inference, and telemetry exports require explicit ports and consent. The product does not receive Firebase-managed realtime state as an architectural shortcut.

## Alternatives considered

1. Firebase-first state and functions: rejected because it contradicts the canonical direction and makes offline operation secondary.
2. Firestore as a cache behind a local facade: rejected because it preserves cloud authority and dependency pressure in the design.
3. A different mandatory cloud backend: rejected for the same local-first reason.

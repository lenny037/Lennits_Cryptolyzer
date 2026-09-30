# Phase 02 — Mobile Runtime Foundation

## Completed work

- Established an explicit process-local runtime lifecycle state machine.
- Added `NEW -> RUNNING -> PAUSED -> RUNNING -> STOPPED` transitions.
- Added a domain-work permission boundary so background workers can determine whether domain processing is currently allowed.
- Added lifecycle transition tests, including restart after stop.
- Kept lifecycle state independent of UI state and network availability.
- Preserved the local-first architecture: runtime correctness does not depend on Firebase, Firestore, or continuous connectivity.

## Existing runtime work carried into this phase

The rebuild already contains the mobile background-processing direction based on WorkManager, durable local events, bounded processing, handler registration, and retry-safe event acknowledgement. This phase formalizes the missing lifecycle boundary around that work.

## Remaining work carried forward

The following items are intentionally carried into later phases because they require the canonical Android source tree and build validation rather than another isolated contract:

- Wire the lifecycle controller into the concrete application runtime.
- Connect WorkManager workers to lifecycle permission checks.
- Complete dependency injection wiring.
- Validate the complete Android Gradle build on the reconstructed source tree.
- Add process-death/restart integration tests on an Android runtime.

These are not being marked complete without a verified build and runtime integration.

# Corrected Master Plan of Action (v2)

**Status:** Active planning baseline. This document supersedes the original Phase 0–20 Master Plan and the Firebase-native reconstruction directives identified in `docs/migration/DELETION_MANIFEST.md`. It does not assert release readiness.

## Direction and scope

The canonical direction is **Android-first, local-first, Firebase/Firestore-free, modular intelligence, optional cloud infrastructure, and repository-backed development**. The core is a pure Kotlin/JVM system; Android is a presentation and platform-integration layer. Local operation, durable local state, and explicit user control are the default. Cloud services, if later approved, are optional adapters rather than the system of record.

The target is not a port of the archived FastAPI, React, Firebase, or PWA runtimes. `docs/inventory/SOURCE_INVENTORY.md`, `docs/inventory/DUPLICATE_CLUSTERS.md`, and `docs/migration/LEGACY_TO_CORE_MAP.md` are the source-selection record. The deletion manifest is the retirement plan for contradictory and obsolete material; it does not itself perform deletion.

### v2 changelog

| Area | v1 approach | v2 correction |
|---|---|---|
| System of record | Firebase/Firestore and cloud functions were prescribed by root directives. | Local durable SQLite event log is the default record. Firebase and Firestore are excluded from core. |
| Delivery controls | CI/CD appeared late in the plan. | CI is Phase 0.5 and is required before feature work. |
| Security | Security appeared after execution-oriented work. | Secure storage and credential isolation are Phase 8a; policy is Phase 8b; v1 has no autonomous execution. |
| Determinism | A deterministic phase depended on ambient time. | `Clock` and deterministic retry inputs are foundation contracts. |
| Learning | Prediction preceded a durable outcome journal. | Prediction/outcome contracts and event types precede calibration and learning. |
| Module names | Bare M00–M20 directories were proposed. | Domain-named modules are used; M identifiers remain metadata in `ModuleId`. |
| Gates | Several gates were prose assertions. | Every phase below has a command or test that can fail. |
| Platform completeness | Observability, schema migration, test strategy, accessibility, cost, rollback, licensing, and KMP decision were absent or implicit. | Each is an explicit workstream and gate. |

## Governing delivery loop

Every phase follows this loop, without skipping the repository-verification step:

`SOURCE MATERIAL -> ARCHITECTURAL ANALYSIS -> IMPLEMENTATION -> UNIT TEST -> INTEGRATION TEST -> SECURITY REVIEW -> PERFORMANCE REVIEW -> GIT COMMIT -> REPOSITORY VERIFICATION -> NEXT PHASE`

A phase may use only the archived source material designated by the migration map as conceptual or UX reference. The loop does not authorize copying legacy Firebase coupling, mock state, or floating-point financial logic.

## Verified rebuild baseline

The table distinguishes implementation evidence from plan intent. Existing XML reports show 183 JVM tests with zero failures across the five tested core modules: domain 61, persistence 33, policy 32, eventbus 31, and runtime 26. No coverage percentage or benchmark result is claimed.

| Phase | Status | Concrete evidence in the current repository |
|---|---|---|
| 0 — repository and architectural baseline | **DONE** | `settings.gradle.kts` declares the seven `:core` modules; `build.gradle.kts` provides `architectureGuard` and `verifyAll`; `ModuleId.kt` retains M00–M20 as metadata. |
| 0.5 — CI and reproducible verification | **DONE for the JVM core; branch protection outstanding** | `verifyAll` is the local aggregate gate. `.github/workflows/ci.yml` runs `./gradlew verifyAll` on JDK 21, validates the Gradle wrapper checksum, uploads test reports, and runs `scripts/firebase-guard.sh`, `scripts/float-guard.sh`, a tracked-binary guard, a 2 MiB file-size guard, and secret scanning. Branch protection is a repository setting and cannot be evidenced from source; it must be enabled on `main` before merge. An Android job is deliberately absent until `:android:app` exists, so no gate reports green for work that does not exist. |
| 1 — contracts and deterministic runtime foundation | **DONE** | `Clock.kt`, `Outcome.kt`, `PlatformError.kt`, `RuntimeService.kt`; `RuntimeGraphTest` and `CryptolyzerRuntimeTest` cover graph ordering, cycles, lifecycle rollback, reverse stop, and degraded health. |
| 2 — exact financial domain and intelligence contracts | **DONE** | `AmountTest`, `AssetTest`, `EvmAddressTest`, `SignalTest`, `PredictionTest`, and `TreasuryTest` cover decimal amounts, asset invariants, EIP-55 validation, signal dedupe, prediction provenance, and treasury reservations. |
| 3 — durable event fabric and outcome record | **DONE** | `EventStoreContract` runs through `InMemoryEventStoreTest` and `SqliteEventStoreTest`; `EventDispatcherTest` covers dispositions; `EventEnvelope` includes schema version and `EventType` includes prediction-produced and prediction-evaluated events. |

The Kotlin convention plugin fixes Kotlin 2.2, explicit API mode, warnings as errors, JVM bytecode target 17, and — following the audit that produced this document — an explicit `jvmToolchain(21)`. Target level and toolchain are separate decisions and both are now declared, so the build no longer depends on whichever JDK happens to be on the runner's PATH. The architecture guard was also strengthened during the same pass: it previously inspected only declared dependencies, and now each core module additionally fails `check` if a forbidden coordinate appears anywhere on its resolved runtime classpath.

## Phase plan and executable exit gates

Commands marked **planned** are acceptance commands to add with the deliverable. They are not claims that the task already exists. Existing commands are identified explicitly.

| Phase | Purpose | Deliverables | Machine-checkable exit gates |
|---|---|---|---|
| 0 — Foundation baseline **DONE** | Establish bounded modules, dependency rules, and source-selection authority. | Pure core module layout; domain module identity; architecture guard; inventory and migration records. | Existing: `gradle architectureGuard`; `gradle verifyAll`. |
| 0.5 — CI and verification **DONE (JVM core)** | Make the local gate mandatory before feature expansion. | CI workflow on JDK 21 running `./gradlew verifyAll`; dependency caching; test-report artifacts; wrapper-checksum validation; executable Firebase, floating-point, binary-artifact, file-size and secret guards. | Existing: `./gradlew verifyAll`; `./scripts/firebase-guard.sh`; `./scripts/float-guard.sh`; per-module `classpathGuard` wired into `check`. Outstanding: enable branch protection on `main` requiring the `core`, `guards` and `secrets` checks. |
| 1 — Contracts, runtime, determinism **DONE** | Make failure, time, lifecycle, configuration, and module identity explicit. | `Outcome`, `PlatformError`, `Clock`, `IdGenerator`, runtime graph and supervisor contracts. | Existing: `gradle :core:runtime:test`; `RuntimeGraphTest`; `CryptolyzerRuntimeTest`. Add planned static rule banning `System.currentTimeMillis()` outside the system-clock adapter. |
| 2 — Financial and intelligence domain **DONE** | Establish exact monetary values and evaluable facts. | `Amount`, assets, treasury invariants, signals, predictions, model versions, prediction outcomes. | Existing: `gradle :core:domain:test`; `AmountTest`; `TreasuryTest`; `PredictionTest`; `SignalTest`. |
| 3 — Local event fabric and outcome journal **DONE** | Preserve facts across process death and support later evaluation. | Versioned envelope, idempotency key, lease claim, retry/dead-letter disposition, SQLite store, prediction outcome event types. | Existing: `gradle :core:eventbus:test :core:persistence:test`; `EventStoreContract`; `EventDispatcherTest`; `SqliteEventStoreDurabilityTest`. |
| 4 — Event/schema evolution and migrations | Define compatibility before additional persisted concepts are introduced. | Event registry with owner and compatibility policy; migration authoring guide; fixture databases for every released schema version; forward-only migration tests. | Planned: `gradle :core:persistence:test --tests '*Migration*'`; compatibility test that decodes retained fixture events for each supported schema version; test rejects mutation of an applied migration checksum. |
| 5 — Deterministic replay and test strategy | Prove replayable decisions from recorded facts and prevent nondeterministic tests. | Replay harness using injected clock and identifiers; deterministic seed policy; test pyramid and contract-test contribution guide. | Planned: `gradle :core:replay:test`; golden replay test executed twice with byte-identical output; static test fails on ambient clock or unseeded random use in core. |
| 6 — Observability and diagnostics | Make local behavior observable without leaking secrets. | Telemetry event catalogue, redaction tests, local diagnostics export, event/dead-letter inspection, retention policy. | Existing partial: `RecordingTelemetry` and `Redactor`. Planned: `gradle :core:telemetry:test`; redaction corpus test; integration test verifying every policy decision emits an auditable record. |
| 7 — Offline data adapters and Signal fabric | Implement read-only acquisition and normalization for M00 ingestion, M08 blockchain data plane, M09 social signals, M10 tokenomics, and M11 governance without introducing provider credentials. | Public/read-only provider ports, cache/refresh policy, source provenance, Signal normalizers, bounded background schedules. Authenticated-provider credentials are not permitted before Phase 8a. | Planned: `gradle :core:ingestion:test :core:domain:test`; contract test for each provider adapter; offline-mode test that reads cached signals without a network; static dependency test that the phase has no secret-store or signer dependency. |
| 8a — Credential security and custody boundary | Put platform credential controls ahead of any signing or privileged connector. | Android Keystore-backed secret port, biometric explicit approval flow, credential isolation, audit trail, recovery/erase design, threat model. | Planned: `gradle :android:app:connectedCheck`; instrumentation test proving a protected operation requires explicit approval; secret-leak test over diagnostics export; dependency scan gate. |
| 8b — Policy and simulation boundary | Mediate every value-moving request and preserve a safe v1 scope. | Policy gateway, simulation adapters, policy-event persistence, decision review UI, signer interface isolated behind Android approval. | Existing core partial: `PolicyEngineTest` and 11 default rules. Planned end-to-end test proving a strategy cannot reach a signer except through policy plus explicit approval. |
| 9 — Android presentation and accessibility | Deliver an Android-first, offline-capable interface for observation and simulation. | Compose presentation module, local read models, safe-mode control, diagnostics, notification policy, accessible UI semantics. | Planned: `gradle :android:app:lint :android:app:connectedCheck`; Compose semantic tests for names, roles, state, traversal, and minimum touch targets; TalkBack manual-test checklist captured as a release artifact. |
| 10 — Prediction and evaluation | Produce bounded predictions only after source provenance and outcome recording exist. | Baseline model contract, feature-fingerprint generation, outcome scheduler, calibration report format, model registry. | Planned: `gradle :core:prediction:test`; test rejects prediction without model version/fingerprint; integration test appends and later evaluates an outcome. |
| 11 — Memory, learning, analytics, and AI bridge | Add M16 memory, M20 learning, M15 analytics, and M17 optional AI integration without making them authorities. | Append-derived memory model, evaluation metrics, local model baseline, AI adapter port with consent and limits. | Planned: `gradle :core:memory:test :core:learning:test :core:analytics:test`; replay test confirms derived state can be rebuilt from events; AI adapter contract test runs with network disabled. |
| 12 — Identity, governance, social, and sports analysis | Complete M13 identity, M11 governance, M09 social, and sports analytics as read-only intelligence domains. | Pseudonymous identity model; governance/social signal normalizers; sports EV calculator; jurisdiction notice and disablement control. | Planned: `gradle :core:identity:test :core:governance:test :core:social:test :core:sports:test`; sports module test proves it emits analysis only and has no betting-account or wagering adapter dependency. |
| 13 — Optional cloud and synchronization | Add opt-in transport only after local correctness is established. | Sync protocol, encrypted export/import, conflict model, provider abstraction, consent/settings UI, cost telemetry. | Planned: offline integration test with no cloud credentials; sync contract test for idempotent re-upload; restore test from encrypted export; architecture test banning cloud SDKs from `:core`. |
| 14 — Reliability and performance | Validate behavior within phone constraints before wider release. | Macrobenchmark suite, battery/background plan, database maintenance policy, failure-injection suite, release operational runbook. | Planned: `gradle :android:app:connectedCheck :android:app:macrobenchmark`; benchmark gate compares measured results with the budgets below; database-growth fixture test; process-death recovery test. |
| 15 — Release governance and ongoing maintenance | Package a reviewable, reversible release process. | Release checklist, SBOM and license notices, privacy data map, incident/rollback playbook, owner approval record. | Planned: `gradle verifyAll`; SBOM/license check; signed release-candidate checklist test; restore-and-downgrade rehearsal on a fixture database. |

## Definition of Done

A feature is done only when all of the following are true:

1. It has a named owner module, documented inputs/outputs, and no forbidden core dependency.
2. Expected failures are represented as `Outcome` and `PlatformError`; programming defects are not relabelled as normal outcomes.
3. Financial quantities use `Amount` and `AssetAmount`; there is no floating-point value in a financial path.
4. Persisted facts have an event type, schema version, idempotency key, migration/compatibility treatment, and redaction classification.
5. Unit tests, integration or contract tests, and required security and performance gates pass in CI.
6. UI work satisfies the accessibility gates in Phase 9 and provides a usable offline/degraded state.
7. The change has an operator-visible rollback or safe-mode path and a licensing/IP review result.

## Phone-oriented SLO and performance budgets

These are **acceptance budgets**, not measured results. Phase 14 must measure them on a documented reference Android device and retain the raw benchmark artifacts.

| Area | Acceptance budget | Measurement rule |
|---|---|---|
| Cold start to usable local dashboard | p95 no greater than 2 seconds with an existing local database | Macrobenchmark from process start to first usable local state. |
| Event handling | A 32-event local batch completes within 250 ms p95 when handlers perform no network I/O | Instrumented benchmark with SQLite WAL and a warm database. |
| Local database growth | Event log growth is measured per retained event; warnings occur before the user-configured storage cap, and compaction never deletes audit/event facts | Fixture appends a fixed corpus and verifies retained event count before/after maintenance. |
| Battery and wake locks | No persistent wake lock; background work uses OS scheduling and is cancellable | Instrumentation/manifest check plus device power run that fails on an unreleased wake lock. |
| Network | Offline launch, cached read, and policy evaluation remain usable; network absence may only degrade remote refresh | Automated airplane-mode integration test. |

## Optional-cloud cost model

The local baseline has no required hosted-backend spend. Optional cloud work must be opt-in, independently disableable, and charged to a named feature budget. For each provider, estimate monthly cost as

\[
C_{monthly} = C_{fixed} + (N_{requests} \times C_{request}) + (B_{egress} \times C_{egress}) + (G_{storage} \times C_{storage})
\]

The settings surface must disclose the provider, data categories, retention, and the current user-configured budget before enabling a connector. A budget breach disables new optional requests and leaves local observation, simulation, and diagnostics available. Firebase and Firestore are not fallback options for this model.

## Rollback and recovery

1. Make persistence changes additive and forward-only. Do not edit a shipped migration or delete event rows.
2. Before a release that changes persistence, export an encrypted local backup and test restore against the prior fixture database.
3. Use feature flags and disabled-by-default adapters for new providers and models.
4. On policy, credential, or integrity failure, latch safe mode, stop value-moving capability, retain evidence in the local event log, and preserve read-only diagnostics.
5. If an application build must roll back, keep the newer database readable through a compatibility path or block downgrade with an export/restore instruction. A binary rollback must never silently reinterpret data.

## Accessibility requirements

The Android presentation must support TalkBack, keyboard or switch navigation where available, scalable text without clipped essential controls, sufficient contrast, semantic names/roles/states for actionable components, non-color-only status cues, and error messages linked to the relevant control. Financial values and policy refusals must be intelligible without relying on charts or color. Accessibility regressions are release blockers under the Phase 9 gate.

## Licensing and IP

Retain provenance from the inventory and migration map for every reused concept, asset, or text. Do not copy legacy code without a recorded license and ownership review. Add SPDX identifiers or an equivalent repository license policy before external distribution; produce an SBOM and third-party notices at release. Model weights, provider terms, and sports-data licences require separate approval because their redistribution and commercial-use rights may differ from source-code rights.

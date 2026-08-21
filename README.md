# Lennit Cryptolyzer

An Android-first, local-first intelligence and treasury platform. The device is the system of
record: the application runs, stores, decides, and remains fully usable with no network and no
cloud account. Cloud services are optional accelerators, never dependencies.

This repository is a rebuild. What preceded it was a staging area of committed zip archives and
three mutually incompatible prototypes; see `legacy/README.md` and `docs/PLAN_CRITIQUE.md` for the
honest assessment.

## Status

| Area | State |
| --- | --- |
| Kotlin/JVM core (7 modules) | Building, tested |
| JVM test suite | 183 tests, 0 failures |
| Architecture guards | Enforced in the build and in CI |
| Android application module | Not started — PLAN_V2 Phase 4 |
| On-chain execution | Deliberately out of scope for v1 (ADR-0008) |

Nothing here is labelled production-ready. That label is reserved for what passes the gates defined
in `docs/PLAN_V2.md`.

## Architecture

Pure Kotlin/JVM core, with Android confined to presentation (ADR-0002). The core has no Android
dependency, which is why it builds and its entire test suite runs in CI in seconds, on any machine,
with no emulator.

```
:core:contracts     Ports and shared vocabulary. Depends on nothing.
:core:domain        Money, addresses, assets, signals, predictions, treasury invariants.
:core:telemetry     Observability port plus redaction.
:core:eventbus      Append-only event log port, retry policy, dispatcher.
:core:persistence   SQLite implementation of the event log, with forward-only migrations.
:core:runtime       Service dependency graph and lifecycle supervisor.
:core:policy        Fail-closed policy engine mediating every value-moving action.
```

Dependencies point inward only. `:core:contracts` knows nothing about the others, and no core
module may reference Android, Firebase, or Google Cloud — enforced, not requested.

### Load-bearing decisions

- **No Firebase or Firestore in the core.** Offline is the default state, not a degraded mode. (ADR-0001)
- **Money is `BigDecimal` via `Amount`.** Binary floating point is banned in financial paths and a
  CI guard enforces it. `toBaseUnits` refuses a conversion that would lose precision rather than
  rounding it away. (ADR-0003)
- **Expected failures are values,** returned as `Outcome<T>` carrying a `PlatformError`, not thrown. (ADR-0004)
- **The event log is append-only** with idempotency keys and lease-based claiming. There is no
  delete operation, and dead letters are retained. No event silently disappears. (ADR-0005)
- **Time is injected** through a `Clock` port, which is what makes determinism testable. (ADR-0006)
- **The policy engine is fail-closed.** An empty rule set, an abstaining rule set, and a rule that
  throws all resolve to `Deny`. No strategy module can reach a signer. (ADR-0007)
- **v1 observes and simulates; it does not execute.** No hot key with autonomous spend authority
  lives on the phone. (ADR-0008)

Full set: `docs/decisions/`.

## Building

Requires JDK 21. The Gradle wrapper handles Gradle itself.

```bash
./gradlew verifyAll        # build, test, and run the architecture guards
./gradlew test             # tests only
./gradlew architectureGuard
./scripts/firebase-guard.sh
./scripts/float-guard.sh
```

The Android SDK is not required to build or test the core.

## Repository layout

```
core/            The buildable, tested system.
buildSrc/        Convention plugin: explicit API mode, warnings as errors, JVM target.
scripts/         Executable architecture guards, also run by CI.
docs/            PLAN_V2, decisions (ADRs), inventory, migration mapping, superseded material.
legacy/          Prior prototypes, as readable text. Not built, not imported.
.github/         CI: core build and test, guards, secret scanning.
```

## Documentation worth reading first

| Document | Why |
| --- | --- |
| `docs/PLAN_V2.md` | The corrected plan of action, with machine-checkable phase gates. |
| `docs/PLAN_CRITIQUE.md` | What was wrong with the original plan and how each item was fixed. |
| `docs/decisions/` | Architecture decision records. |
| `docs/inventory/SOURCE_INVENTORY.md` | Every legacy file classified: retain, refactor, rewrite, concept-only, obsolete. |
| `docs/migration/DELETION_MANIFEST.md` | What was removed, why, and what still needs a history rewrite. |

## Licence

Proprietary. See `LICENSE`. This software is not financial, investment, or legal advice.

# Canonical Module Reconciliation — LSTECH ARCHIVES

The archive contains M00-M22 implementation families. The platform remains bounded by M01-M20 canonical module ownership. M21/M22 are extensions and cannot bypass shared policy/security.

| Archive agent | Canonical owner | Runtime target | Status |
|---|---|---|---|
| M00 Extractor | Ingestion/tooling | Kotlin + TypeScript adapters | Consolidate |
| M01 Orchestralyzer | M01 | Kotlin domain/orchestration | Rewrite |
| M02 Treasurolyzer | M02 | Kotlin + Room | Rewrite |
| M03 Airdrolyzer | M03 | Kotlin + network adapters | Rewrite |
| M04 Cryptollector | M04 | Kotlin WorkManager | Rewrite |
| M05 Predictlyzer | M05 | Kotlin/Rust | Rewrite |
| M06 Arbitrader | M06 | Kotlin + Rust/native where justified | Rewrite; execution quarantined |
| M07 Stakeolyzer | M07 | Kotlin | Rewrite |
| M08 Chainsighter | M08 | Kotlin + RPC adapters | Rewrite |
| M09 Vibelyzer | M09 | Kotlin | Rewrite |
| M10 Tokenomizer | M10 | Kotlin | Rewrite |
| M11 Consensor | M11 | Kotlin | Rewrite |
| M12 Sentinelyzer | M12 | Kotlin security/policy | Rewrite |
| M13 Personolyzer | M13 | Kotlin | Rewrite |
| M14 Fluxolyzer | M14 | Kotlin event pipeline | Rewrite |
| M15 Insightolyzer | M15 | Kotlin analytics | Rewrite |
| M16 Cortexolyzer | M16 | Kotlin + Room/vector adapter | Rewrite |
| M17 Synapsolyzer | M17 | Kotlin tool/AI bridge | Rewrite |
| M18 Nexolyzer | M18 | Android operations | Rewrite |
| M19 Revolyzer | M19 | Kotlin/cloud adapter | Rewrite |
| M20 Ascendolyzer | M20 | Kotlin/Rust research loop | Controlled implementation |
| M21 Betolyzer | M21 extension | Policy-gated adapter | Optional/regulated |
| M22 Minelyzer | M22 extension | Rust/native compute | Optional |

## Legacy runtime policy

Python FastAPI/SQLAlchemy/Redis services are retained only as migration/reference material. They are not part of the phone runtime.

Firebase Functions are retained as a compatibility/cloud adapter while the phone-first architecture is built. Firestore is not the canonical data store.

The new source-of-truth boundary is:

Android domain -> Room -> optional CloudSyncProvider

## Non-negotiable boundary

All financial, wallet, or privileged operations must cross M12 security/policy and the device authorization boundary before execution.

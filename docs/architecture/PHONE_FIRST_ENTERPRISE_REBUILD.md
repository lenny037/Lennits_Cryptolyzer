# LENNIT_CRYPTOLYZER — Phone-First Enterprise Rebuild
# Design Baseline: 2026-08-17

## Decision

The canonical runtime becomes **local-first Android**, with:
- Kotlin/Jetpack Compose for the application and orchestration UI.
- Kotlin coroutines/Flow for concurrency and reactive state.
- Room/SQLite for authoritative local state.
- WorkManager for durable background scheduling.
- Android Keystore/biometric authorization for secrets and privileged actions.
- Rust NDK core for CPU-intensive cryptography, numerical processing, and selected performance-critical routines.
- TypeScript/Node remains the optional cloud/edge control plane and API adapter, not the primary runtime.
- Python becomes archive/research/migration material; it is not a production runtime dependency.
- Firestore is removed from the canonical architecture.
- A storage/sync provider interface permits no-cloud operation and optional Supabase synchronization.
- Supabase Free is the default optional cloud adapter for development/small deployments; production deployments can replace it without changing domain logic.

## Runtime layers

1. UI / presentation
2. Application orchestration
3. Domain modules
4. Policy and execution control
5. Local persistence
6. Network/data adapters
7. Native Rust acceleration
8. Optional cloud synchronization

## Canonical data flow

UI -> ViewModel -> Use Case -> Domain Module -> Policy Gate -> Repository -> Local DB

Network adapters update repositories through validated commands/events.

Cloud synchronization is an adapter, never a source-of-truth requirement.

## Execution safety

Financial/on-chain operations MUST default to:
OBSERVE -> SIMULATE -> RISK CHECK -> POLICY CHECK -> USER/DEVICE AUTHORIZATION -> EXECUTE -> VERIFY -> AUDIT.

No module may directly submit a transaction.

Direct transaction submission from legacy modules is classified as a migration defect.

## Operating modes

OBSERVE
PAPER
SHADOW
SIMULATE
MANUAL_APPROVAL
CONTROLLED_LIVE
SAFE_MODE
SHUTDOWN

The default installed state is OBSERVE.

## Module reconciliation

The existing canonical M01-M20 architecture is retained as the stable platform boundary. Additional archive agents are mapped into those domains instead of creating uncontrolled parallel module systems.

M00 Extractor -> ingestion/tooling
M01 Orchestralyzer -> M01
M02 Treasurolyzer -> M02
M03 Airdrolyzer -> M03
M04 Cryptollector -> M04
M05 Predictlyzer -> M05
M06 Arbitrader -> M06
M07 Stakeolyzer -> M07
M08 Chainsighter -> M08
M09 Vibelyzer -> M09
M10 Tokenomizer -> M10
M11 Consensor -> M11
M12 Sentinelyzer -> M12
M13 Personolyzer -> M13
M14 Fluxolyzer -> M14
M15 Insightolyzer -> M15
M16 Cortexolyzer -> M16
M17 Synapsolyzer -> M17
M18 Nexolyzer -> M18
M19 Revolyzer -> M19
M20 Ascendolyzer -> M20
M21 Betolyzer -> optional regulated-domain extension
M22 Minelyzer -> optional compute extension

M21 and M22 are not allowed to bypass the common security/policy layer.

## Language strategy

Python is not translated mechanically line-for-line.

- Kotlin: Android application, orchestration, persistence, scheduling, UI, device integrations.
- Rust: cryptographic/native acceleration where benchmarks justify it.
- TypeScript: optional cloud/edge functions, web dashboard, API adapters and migration compatibility.
- Python: research notebooks, offline analysis, legacy reference only.

This minimizes runtime complexity and eliminates Python interpreter/async-service overhead on the phone.

## Persistence

Room is authoritative local state.

Recommended stores:
- Room: structured domain state, events, execution records, cached market data.
- DataStore: preferences and non-relational configuration.
- Android Keystore: private keys and encryption material.
- App-private files: encrypted exports and model artifacts.

No secrets in source, assets, Git, or plain-text configuration.

## Cloud

Canonical interface:

CloudSyncProvider
  - NoneProvider
  - SupabaseProvider
  - FutureProvider

Supabase is optional. Its current Free tier provides a 500 MB Postgres database, 5 GB egress, 1 GB storage, 50,000 MAU, and two free projects, but free projects may pause after inactivity. It is therefore suitable as an optional development/sync layer, not as the sole institutional availability mechanism.

## AI

AI is provider-neutral.

On-device path:
- Android system/on-device AI where supported.
- Local model adapter where device capability permits.

Cloud path:
- Gemini adapter or another provider through a provider interface.

The AI layer never receives raw signing keys.

AI can propose actions; policy code authorizes actions.

## Security baseline

- Android Keystore-backed key material.
- Biometric/device credential authorization for privileged operations.
- Certificate/network security configuration.
- No cleartext traffic.
- Strict input validation.
- Signed/audited event records.
- Least-privilege permissions.
- Secure export/import.
- Dependency lockfiles and vulnerability scanning.
- Secret scanning in CI.
- SBOM generation.
- Reproducible release artifacts where practical.

## Testing

Required layers:
- Unit tests
- Repository tests
- Room migration tests
- Domain contract tests
- Policy/security tests
- Network adapter tests
- Android instrumentation tests
- End-to-end tests
- Property-based tests for financial calculations
- Fuzz tests for parsers/decoders
- Static analysis
- Dependency/security scanning

## Repository target

apps/android/
shared/domain/
shared/contracts/
shared/security/
shared/storage/
shared/network/
native/rust-core/
cloud/edge/
web/
infra/
docs/
archive/
tests/

The archive remains immutable/reference material. Canonical source lives outside archive/.

## Migration rule

Every migrated feature must have:
1. source provenance
2. canonical owner
3. implementation status
4. test coverage
5. security classification
6. removal/deprecation decision for the old implementation

No silent deletion.

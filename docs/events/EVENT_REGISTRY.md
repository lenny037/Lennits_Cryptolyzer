# Event registry

**Generated from `EventRegistry.RELEASED` by `EventRegistry.render()`. Do not edit by hand:** `EventRegistryDocsTest` fails when this file and the declaration disagree. Regenerate with `./gradlew :core:eventbus:test -Dcryptolyzer.updateEventRegistryDoc=true`.

Registry fingerprint: `sha256:da1ea5077ba3a471631586f16c7eb0da84681b844f8fc14ae3e6f0a9fb639f94`

`Ratified` types accept exactly their declared payload fields. `Draft` types have an owner and a version but no settled payload yet, and accept any payload until the phase that implements their producer ratifies them. Old versions are never removed from this document: a persisted row from an earlier release must stay readable, and its upcast is what makes that true.

| Event type | Owner | Status | Compatibility | Current version |
|---|---|---|---|---|
| `chain.balance_observed` | M08 blockchain-data-plane | Draft | AdditiveOnly | 1 |
| `chain.block_observed` | M08 blockchain-data-plane | Draft | AdditiveOnly | 1 |
| `intelligence.signal_produced` | M14 data-pipeline | Draft | AdditiveOnly | 1 |
| `policy.decision_recorded` | M12 security-risk | Draft | AdditiveOnly | 1 |
| `prediction.evaluated` | M05 prediction | Draft | AdditiveOnly | 1 |
| `prediction.produced` | M05 prediction | Draft | AdditiveOnly | 1 |
| `runtime.state_changed` | M01 orchestration | Draft | AdditiveOnly | 1 |
| `treasury.snapshot_taken` | M02 treasury | Draft | AdditiveOnly | 1 |

## `chain.balance_observed`

An account balance read at a known block. Ratified by Phase 7. Amounts are decimal strings, never floating point (ADR-0003).

Owner: **M08 blockchain-data-plane** · Status: **Draft** · Compatibility: **AdditiveOnly**

### Version 1

No payload fields are declared yet (draft).

## `chain.block_observed`

A block header observed on a configured chain. Read-only observation; ratified by Phase 7 with the RPC adapter that produces it.

Owner: **M08 blockchain-data-plane** · Status: **Draft** · Compatibility: **AdditiveOnly**

### Version 1

No payload fields are declared yet (draft).

## `intelligence.signal_produced`

A normalized signal emitted by the intelligence fabric. Ratified by Phase 7 together with the Signal normalizers.

Owner: **M14 data-pipeline** · Status: **Draft** · Compatibility: **AdditiveOnly**

### Version 1

No payload fields are declared yet (draft).

## `policy.decision_recorded`

The audit record of a policy decision: request, verdict, and deciding rule. Ratified by Phase 8b, which requires every decision to be auditable.

Owner: **M12 security-risk** · Status: **Draft** · Compatibility: **AdditiveOnly**

### Version 1

No payload fields are declared yet (draft).

## `prediction.evaluated`

The realized outcome of an earlier prediction. Exists from Phase 3 onwards so calibration is measurable later; ratified by Phase 10.

Owner: **M05 prediction** · Status: **Draft** · Compatibility: **AdditiveOnly**

### Version 1

No payload fields are declared yet (draft).

## `prediction.produced`

A bounded prediction with its model version and feature fingerprint. Ratified by Phase 10, which cannot ship without provenance.

Owner: **M05 prediction** · Status: **Draft** · Compatibility: **AdditiveOnly**

### Version 1

No payload fields are declared yet (draft).

## `runtime.state_changed`

A service lifecycle transition, including degraded health. Ratified with the diagnostics surface in Phase 6.

Owner: **M01 orchestration** · Status: **Draft** · Compatibility: **AdditiveOnly**

### Version 1

No payload fields are declared yet (draft).

## `treasury.snapshot_taken`

A point-in-time treasury balance and reservation snapshot. Ratified with the treasury read model in Phase 9.

Owner: **M02 treasury** · Status: **Draft** · Compatibility: **AdditiveOnly**

### Version 1

No payload fields are declared yet (draft).

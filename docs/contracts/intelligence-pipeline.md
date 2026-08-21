# Intelligence Pipeline Contract — M00 / M14 / M15

## Pipeline

```text
Raw observation
    -> M00 extraction
    -> M14 normalization
    -> M15 analytics
    -> canonical signal
```

## Invariants

- Every signal has a stable identifier.
- Source timestamps are preserved; ingestion time is separate.
- Duplicate observations are idempotently collapsed.
- Invalid numeric values are rejected rather than coerced.
- Units and asset/chain identity are explicit.
- Derived values record their source observation identifiers.
- Historical records are immutable; corrections are represented as new observations.
- Local persistence is the authoritative operational boundary.

## Canonical signal fields

- `signalId`
- `sourceObservationIds`
- `assetId`
- `chainId`
- `observedAt`
- `ingestedAt`
- `featureSetVersion`
- `values`
- `qualityScore`
- `provenance`

M15 output is suitable for prediction consumers but is not itself an execution authorization.

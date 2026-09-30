# M05 — Prediction Contract

M05 consumes versioned canonical signals and produces a prediction artifact. It does not authorize execution.

## Input

- canonical signal set
- feature-set version
- model identifier/version
- inference timestamp

## Output

- prediction identifier
- model identifier/version
- feature-set version
- predicted value/class
- confidence or calibrated probability
- inference timestamp
- provenance references
- quality/status code

## Rules

1. Model identity is immutable for an inference result.
2. Feature schema and model version are always recorded.
3. Non-finite model outputs are rejected.
4. Confidence values must be validated against their declared range.
5. Model failure is an explicit failure state, never a fabricated prediction.
6. Prediction artifacts are persisted for later outcome evaluation.
7. A prediction cannot directly invoke a transaction or signing operation.
8. Local inference and remote inference implement the same contract.

## Mobile constraint

Inference implementations must expose resource/latency characteristics so the runtime can select an appropriate execution path without embedding model-specific assumptions in the domain layer.

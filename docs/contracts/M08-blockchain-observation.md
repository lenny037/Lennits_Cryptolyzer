# M08 — Blockchain Observation Contract

M08 is an observation/simulation boundary. It does not own signing keys or autonomous financial authorization.

## Required operations

- `getChainStatus(chainId)`
- `getLatestBlock(chainId)`
- `getBalance(chainId, address)`
- `getTransaction(chainId, txHash)`
- `estimateGas(chainId, call)`
- `simulateCall(chainId, call)`

## Provider rules

1. Providers are adapters behind a stable domain-facing interface.
2. Chain identity is explicit; callers may not infer a chain from a URL.
3. Responses must include provider/chain metadata and observation time.
4. Transport failures are typed and retryable.
5. Invalid responses are rejected before entering domain state.
6. uint256-compatible quantities must be represented without unsafe fixed-width assumptions.
7. Simulation is distinct from execution.
8. Private keys and signing material are outside M08.
9. Provider failover must not silently change chain identity.

## Safety boundary

M08 can observe and simulate. It cannot bypass the risk/execution policy boundary.

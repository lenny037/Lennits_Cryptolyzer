# Legacy Security Audit — 2026-08-17

## Critical migration findings

### 1. Direct transaction execution exists in legacy M06
`functions/src/modules/m06-arbitrader/index.ts` contains direct router transaction submission. It explicitly describes direct execution and bypasses the intended simulation-first architecture.

**Disposition:** quarantine as legacy/reference; rewrite behind the canonical Execution Policy Gate.

### 2. Legacy risk engine contains placeholder security checks
`src/modules/m12-security/RiskEngine.ts` contains a proxy check that returns `false` unconditionally and uses fixed threshold/proxy logic.

**Disposition:** replace with deterministic, testable risk adapters and explicit unknown-state handling. Unknown security state must fail closed for privileged execution.

### 3. Legacy MEV engine contains simulated transaction hashes and quotes
`src/modules/m06-mev/MevEngine.ts` creates pseudo transaction hashes and hard-coded quote/gas values.

**Disposition:** simulation/test fixture only. Never expose as a production execution implementation.

### 4. Legacy treasury uses in-memory positions
`src/modules/m02-treasury/TreasuryEngine.ts` initializes hard-coded positions in a process-local Map.

**Disposition:** replace with Room repositories on-device and explicit sync adapters.

### 5. Legacy AI bridge embeds an old model selection
`src/modules/m17-execution/GeminiBridge.ts` directly selects `gemini-1.5-flash`.

**Disposition:** provider-neutral AI adapter. Model selection must be configuration/policy driven and must never receive signing material.

### 6. Credential material in source archive
The source archive contains `github-recovery-codes.txt` and other secret/configuration candidates.

**Disposition:** treat all exposed recovery credentials as compromised; revoke/rotate them. Remove credentials from source history where operationally possible. Add secret scanning and repository rules before publication.

## Security architecture replacement

All privileged operations must follow:

OBSERVE -> SIMULATE -> RISK -> POLICY -> DEVICE/USER AUTH -> EXECUTE -> VERIFY -> AUDIT

No module is permitted to call a blockchain signer directly. Signing is isolated behind a device-secure authorization boundary.

## Required CI controls

- Secret scanning
- Dependency vulnerability scanning
- SBOM generation
- Static analysis
- Unit and integration tests
- Release artifact verification
- No plaintext private keys/recovery codes
- No debug signing material in release builds

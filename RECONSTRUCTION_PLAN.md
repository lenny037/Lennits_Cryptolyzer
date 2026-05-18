# RECONSTRUCTION_PLAN.md
# LENNIT_CRYPTOLYZER — Canonical Reconstruction Execution Plan
# Generated: Reconstruction Phase 02

---

## EXECUTION PHASES

### PHASE 01 — GLOBAL INGESTION ✅ COMPLETE
All archives extracted and fully read:
- 161 files catalogued
- All Python, Kotlin, TypeScript, Rust, config files ingested
- Architecture intent fully extracted from all text documents
- Conversation logs catalogued

### PHASE 02 — INVENTORY GENERATION ✅ COMPLETE
Generated:
- MASTER_INVENTORY.md ✅
- SYSTEM_RELATIONSHIP_MAP.md ✅
- DUPLICATE_CLUSTER_REPORT.md ✅
- CANONICAL_MODULE_MAP.md ✅
- RECONSTRUCTION_PLAN.md ✅ (this file)

### PHASE 03 — ARCHITECTURE EXTRACTION ✅ COMPLETE
- 20 canonical modules identified and bounded
- Dependency graph constructed
- Data flow lifecycle mapped
- Event contracts specified
- Firestore schema topology mapped

### PHASE 04 — DUPLICATE CLUSTERING ✅ COMPLETE
- 8 duplicate clusters identified
- Authoritative versions selected per cluster
- Unique features catalogued for preservation
- Merge/discard/defer decisions made for all duplicates

### PHASE 05 — CANONICALIZATION (IN PROGRESS)
Constructing authoritative files:
- [x] Repository directory structure
- [x] Core infrastructure (logger, config, validation, observability)
- [x] Shared types and event contracts
- [ ] All 20 module implementations
- [ ] API gateway (M18)
- [ ] Firestore rules
- [ ] Pub/Sub topic registry

### PHASE 06 — MODERNIZATION
Migration targets:
- Python → TypeScript Cloud Functions
- In-memory state → Firestore
- Custom EventBus → Pub/Sub
- Docker-only → Firebase-native (Docker preserved for dev)
- Mock data → structured stubs with Firestore integration
- FastAPI routes → Firebase HTTPS Functions

### PHASE 07 — REPOSITORY RECONSTRUCTION
Generate complete canonical repository output.

### PHASE 08 — DEPLOYMENT NORMALIZATION
Firebase project configuration, CI/CD pipelines.

### PHASE 09 — DOCUMENTATION REGENERATION
Full API contracts, event contracts, deployment runbooks.

---

## PRIORITY ORDER FOR MODULE RECONSTRUCTION

### TIER 1 — FOUNDATION (must exist before others)
1. **M14 Data Pipeline** — Pub/Sub routing (all others depend on this)
2. **M12 Security Engine** — risk scoring (required before any execution)
3. **M01 Orchestration Core** — agent lifecycle management
4. **Core Infrastructure** — logger, config, validation, observability

### TIER 2 — EXECUTION (financial operations)
5. **M06 MEV & Arbitrage** — primary revenue engine (simulation-first)
6. **M07 DeFi Farming** — yield generation
7. **M02 Treasury** — capital management
8. **M08 Blockchain Intelligence** — data substrate for all execution

### TIER 3 — INTELLIGENCE (data gathering)
9. **M05 Prediction Engine** — signal generation
10. **M03 Airdrop Intelligence** — opportunity discovery
11. **M04 Faucet Harvester** — reward automation
12. **M09 Social Signal Engine** — sentiment data

### TIER 4 — PLATFORM (user-facing and persistence)
13. **M16 Agent Memory** — semantic memory (MCP-compatible)
14. **M17 AI Execution Bridge** — LLM tool server
15. **M15 Analytics Engine** — dashboards and KPIs
16. **M18 Mobile Operations** — API gateway + push notifications

### TIER 5 — ECOSYSTEM (advanced platform features)
17. **M13 Identity & Profile** — wallet identity
18. **M10 Tokenomics** — token economics
19. **M11 Governance** — governance workflows
20. **M19 Monetization** — SaaS billing
21. **M20 Self-Improvement** — RL feedback loops

---

## MODERNIZATION REQUIREMENTS PER MODULE

| Requirement | Target | Status |
|-------------|--------|--------|
| TypeScript strict mode | All CF modules | In progress |
| Zod schema validation | All inputs | In progress |
| Structured logging | All functions | In progress |
| Pub/Sub event emission | All modules | In progress |
| Firestore writes only to own root | All modules | In progress |
| Simulation-first execution | M06, M07, M04 | Enforced by design |
| Risk gate before execution | M06, M07, M04 | M12 integration required |
| Audit logging | All financial ops | Firestore writes |
| Error telemetry | All functions | Cloud Error Reporting |
| Environment isolation | All configs | Secret Manager |

---

## DEFERRED FEATURES REGISTRY

The following features are PRESERVED in intent but deferred to a future implementation sprint:

| Feature | Source | Reason for Deferral | Target Module |
|---------|--------|--------------------|-|
| 3D Gaussian Splatting dashboard | code 2.txt | Requires specialized WebGPU/WebGL library | M15 |
| Post-quantum Dilithium signing (ML-DSA) | code 1.txt | liboqs bindings complex in Firebase | M12 |
| Rust NPU inference for mobile | rs/lib.rs | Android build pipeline separate | M17/M18 |
| Full RL self-evolution loop | code 2.txt | Requires live performance data history | M20 |
| HiMAP bargaining protocol | code 2.txt | Requires multi-agent runtime | M01 |
| ZK oracle / snarkjs | code 1.txt | High complexity, separate track | M08 |
| P2P libp2p mesh | code 1.txt | Separate infrastructure track | M14 |
| AR/VR dashboard (Sceneform) | code 1.txt | Separate Android track | M18 |
| Voice control (Vosk) | code 1.txt | Separate Android track | M18 |
| Quantum cloud connectors (IBM/AWS) | code 1.txt | External API dependency | M05 |
| Federated learning | code 1.txt | Requires distributed infra | M20 |
| Mempool intelligence | route spec | Requires MEV relay connection | M06 |
| Session-scoped agent wallets (Polygon Agent CLI) | code 2.txt | Requires Polygon integration | M13 |
| INFINIT Labs strategy creator integration | code 2.txt | Third-party platform | M07 |
| ElectricSQL offline sync | code 2.txt | Separate mobile track | M18 |

---

## FINANCIAL SAFETY GATES — NON-NEGOTIABLE

All execution modules (M06, M07, M04) MUST pass through these gates before any on-chain action:

```
EXECUTION REQUEST
      │
      ▼
[1] SIMULATION — simulate() returns expected_profit + expected_gas
      │
      │ simulation_result.profit < min_threshold? → ABORT
      ▼
[2] RISK SCORE — M12.riskScore() returns 0-100
      │
      │ risk_score > threshold? → ABORT (log reason)
      ▼
[3] AUDIT LOG — write to Firestore /executions/{id} with full context
      │
      ▼
[4] EXECUTE — submit transaction
      │
      ▼
[5] TELEMETRY — emit to Pub/Sub + Cloud Logging
      │
      ▼
[6] RESULT LOG — update Firestore with outcome
```

**Any step failure = ABORT. No silent failures.**

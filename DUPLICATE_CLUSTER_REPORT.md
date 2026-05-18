# DUPLICATE_CLUSTER_REPORT.md
# LENNIT_CRYPTOLYZER — Duplicate and Overlap Analysis
# Generated: Reconstruction Phase 04

---

## CLUSTER 01 — Android App Variants

**Status: MERGE → CANONICAL**

| File | Variant | Verdict |
|------|---------|---------|
| kt/MainActivity (1) (1).kt | cryptolyzer-app | AUTHORITATIVE |
| kt/MainActivity (2).kt | lennit-suite-mcp (stub) | DISCARD (2-line stub) |
| kt/DashboardScreen (3).kt | cryptolyzer-app (full) | AUTHORITATIVE |
| kt/DashboardScreen (1).kt | lennit-suite-mcp (stub) | DISCARD (stub) |
| kt/DashboardViewModel (4).kt | Full ViewModel | AUTHORITATIVE |
| kt/NotificationsViewModel (4).kt | Full ViewModel | AUTHORITATIVE |
| kt/NotificationsViewModel (1).kt | Stub (empty) | DISCARD |
| kt/SafeControlViewModel (2).kt | Full ViewModel | AUTHORITATIVE |
| kt/SafeControlViewModel (1).kt | Stub | DISCARD |
| kt/SettingsViewModel (4).kt | Full | AUTHORITATIVE |
| kt/SettingsViewModel (2).kt | Stub | DISCARD |
| kt/StrategiesViewModel (4).kt | Full | AUTHORITATIVE |
| kt/StrategiesViewModel (2).kt | Stub | DISCARD |
| kt/VaultViewModel (1).kt | Stub | DEFERRED (VaultScreen not yet implemented) |
| kt/TreasuryViewModel.kt | Stub | DEFERRED |
| kt/StrategiesScreen (2).kt | Stub | DISCARD |
| kt/SafeScreen (1).kt | Stub | DEFERRED |
| kt/SettingsScreen (2).kt | Stub | DISCARD |
| kt/SettingsScreen (3).kt | Full SettingsScreen | AUTHORITATIVE |
| kt/TreasuryScreen.kt | Stub | DEFERRED |
| kt/BrochureWebViewScreen.kt | Stub | KEEP (unique WebView concept) |

**Canonical Decision**: One Android app (cryptolyzer-app) is the authoritative target. The lennit-suite-mcp variant is the MCP landing page client. Both are preserved but separated clearly.

---

## CLUSTER 02 — NPU Accelerator Variants

**Status: MERGE → CANONICAL**

| File | Content | Verdict |
|------|---------|---------|
| rs/npu_accelerator (3).rs | Full NpuEngine struct | AUTHORITATIVE |
| rs/npu_accelerator (9).rs | Stripped stub | DISCARD |
| rs/rust_core_src_brain_npu_accelerator (1).rs | Hexagon DSP path variant | MERGE features in |
| rs/lib (1).rs | JNI bridge (uses npu_accelerator) | AUTHORITATIVE |

**Canonical Decision**: Merge `rust_core_src_brain_npu_accelerator` Hexagon path into primary `npu_accelerator.rs`.

---

## CLUSTER 03 — Backend Engine (Python vs TypeScript)

**Status: CANONICAL TARGET = TypeScript Cloud Functions**

| Python File | TypeScript Equivalent | Action |
|------------|----------------------|--------|
| event_bus.py | M14 event bus → Pub/Sub | REPLACE with Pub/Sub |
| state_manager.py | Firestore collections | REPLACE with Firestore |
| task_queue.py | Cloud Tasks / Pub/Sub | REPLACE with Cloud Tasks |
| airdrop_engine.py | functions/src/modules/m03-airdrop/ | REWRITE in TS |
| faucet_engine.py | functions/src/modules/m04-faucet/ | REWRITE in TS |
| yield_engine.py | functions/src/modules/m07-farming/ | REWRITE in TS |
| bridge_engine.py + bridge_*.py | functions/src/modules/m08-blockchain/ | REWRITE in TS |
| multi_chain.py | functions/src/modules/m08-blockchain/ | REWRITE (keep Web3 logic) |
| risk_manager.py | functions/src/modules/m12-security/ | REWRITE with real logic |
| live_trade.py | functions/src/modules/m06-mev/ | REWRITE in TS |
| routes (2) (1).py | functions/src/modules/m18-mobile/ (API gateway) | REWRITE as CF HTTP functions |

**Canonical Decision**: All Python stubs are preserved for intent. FastAPI routes provide the authoritative API contract. Target runtime is TypeScript Cloud Functions.

---

## CLUSTER 04 — MCP Server Variants

**Status: BOTH PRESERVED — Different Purposes**

| File | Purpose | Verdict |
|------|---------|---------|
| txt/code 3.txt | Memory MCP server (SQLite + BGE embeddings) | AUTHORITATIVE for M16 |
| txt/code 4.txt | Tool/App MCP server (Express + HTTP) | AUTHORITATIVE for M17 |

**Canonical Decision**: Both are unique and non-overlapping. M16 handles persistent memory. M17 handles tool execution. Both are reconstructed in `functions/src/modules/`.

---

## CLUSTER 05 — Deployment Config Variants

**Status: MERGE → Firebase-Native**

| File | Type | Status |
|------|------|--------|
| firebase.json | Firebase hosting | KEEP + EXTEND |
| docker-compose.yml | Docker | KEEP (dev only) |
| deployment.yaml | Kubernetes | KEEP (alt deployment) |
| service.yaml | K8s service | KEEP (alt deployment) |
| misc/nginx.conf | Nginx | KEEP (dev/proxy) |
| bootstrap-firebase.sh (EXPORT) | Firebase init script | EXTEND |

**Canonical Decision**: Firebase-native is the primary deployment target. Docker and K8s configs are preserved as alternative deployment paths in `infra/alternative-deployments/`.

---

## CLUSTER 06 — Event Bus Implementations

**Status: CONSOLIDATE → Pub/Sub**

| Implementation | Location | Verdict |
|---------------|----------|---------|
| Python EventBus class | event_bus.py | REPLACE |
| Code 3 (MCP) Redis channels | docker-compose.yml Redis | SUPERSEDED by Pub/Sub |
| Code 4 MCP Tool routes | server.ts routes | KEEP for MCP layer |
| Firebase Pub/Sub | TARGET | CANONICAL |

---

## CLUSTER 07 — State Management Variants

**Status: CONSOLIDATE → Firestore**

| Implementation | Location | Verdict |
|---------------|----------|---------|
| Python StateManager dict | state_manager.py | REPLACE |
| Android StateFlow repos | Repositories (2).kt | KEEP (client-side state) |
| Firebase Firestore | TARGET | CANONICAL (server state) |
| SQLite (MCP Memory) | code 3.txt | KEEP (agent memory, M16) |
| PostgreSQL (docker-compose) | docker-compose.yml | OPTIONAL (advanced analytics) |

---

## CLUSTER 08 — Frontend Variants

**Status: TWO DISTINCT SURFACES — BOTH KEPT**

| File | Surface | Verdict |
|------|---------|---------|
| js/App.js + package.json | React dashboard (SPA) | AUTHORITATIVE for M18 web |
| txt/code 5.txt | Landing/marketing page (HTML/CSS) | AUTHORITATIVE for M18 landing |
| html/chat.html | AI chat export | ARCHIVE ONLY (not production) |
| js/sw.js | PWA service worker | KEEP |
| js/webgpu_vis.js | WebGPU visualization | KEEP (unique M15 feature) |

---

## UNIQUE FEATURES PRESERVED (No Duplicates — Must Not Be Lost)

| Feature | Location | Module |
|---------|----------|--------|
| WebGPU 120Hz market visualization | webgpu_vis.js | M15 |
| Rust JNI NPU bridge (Snapdragon 865) | rs/lib (1).rs | M17 |
| BGE vector embedding memory | code 3.txt | M16 |
| HiMAP bargaining protocol (agent resource negotiation) | code 2.txt spec | M01 |
| Post-quantum Dilithium signing | code 1.txt spec | M12 |
| MCP tool registration protocol | code 4.txt | M17 |
| Foreground Guardian service (START_STICKY) | GuardianService.kt | M18 |
| BiometricGatekeeper for SAFE SHUTDOWN | BiometricGatekeeper.kt | M12 |
| Shadow Mode execution (paper trading) | routes + strategy spec | M06/M07 |
| FULL_AUTONOMY / SAFE_MODE / SHUTDOWN system modes | routes.py | M12 |
| Reinforcement learning self-evolution | code 1.txt + code 2.txt | M20 |
| Cross-chain bridge routing (Stargate, Across) | bridge_*.py | M08 |
| Agent bargaining loop for capital allocation | code 2.txt | M01 |
| 3D Gaussian splatting dashboard vision | code 2.txt spec | M15 |

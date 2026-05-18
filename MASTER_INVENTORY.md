# MASTER_INVENTORY.md
# LENNIT_CRYPTOLYZER — Full Repository Inventory
# Generated: Reconstruction Phase 02
# Source Archives: LENNIT_CRYPTOLYZER_EXPORT.zip + Lennits_Cryotolyzer_archives_zip.001–006

---

## ARCHIVE MANIFEST

| Archive | Size | Contents |
|---------|------|----------|
| LENNIT_CRYPTOLYZER_EXPORT.zip | 2.2 KB | 5 scaffold/directive files |
| Lennits_Cryotolyzer_archives_zip.001–006 | ~77 MB combined | Main codebase (161 files) |

---

## FILE INVENTORY BY CATEGORY

### PYTHON (py/) — Backend Engine Layer

| File | Size | Classification | Module |
|------|------|---------------|--------|
| routes (2) (1).py | 15.5 KB | AUTHORITATIVE — Full FastAPI REST API, all 20 modules exposed | M02/M03/M04/M05/M06/M07/M08 |
| event_bus.py | 274 B | CORE — In-memory pub/sub EventBus | M14 |
| state_manager.py | 163 B | CORE — In-memory state log | M01 |
| task_queue.py | 224 B | CORE — Deque-based task queue | M01 |
| airdrop_engine.py | 106 B | ENGINE STUB — AirdropEngine.score() | M03 |
| airdrop_multi.py | 124 B | ENGINE STUB — Multi-wallet airdrop variant | M03 |
| bounty_engine.py | 109 B | ENGINE STUB — BountyEngine.evaluate() | M03 |
| faucet_engine.py | 104 B | ENGINE STUB — FaucetEngine.claim() | M04 |
| yield_engine.py | 139 B | ENGINE STUB — YieldEngine.apy_score() | M07 |
| bridge_engine.py | 82 B | ENGINE STUB — decide_route() | M08 |
| bridge_orchestrator.py | 187 B | ENGINE STUB — execute_bridge() | M08 |
| bridge_executor.py | 90 B | ENGINE STUB | M08 |
| bridge_router.py | 77 B | ENGINE STUB | M08 |
| base_bridge.py | 77 B | ENGINE STUB | M08 |
| stargate.py | 105 B | ENGINE STUB — Stargate bridge adapter | M08 |
| multi_chain.py | 308 B | ENGINE — Web3 multi-chain balance | M08 |
| live_trade.py | 150 B | ENGINE STUB — buy()/sell() | M06 |
| risk_manager.py | 24 B | STUB — risk() returns 'OK' | M12 |
| route_selector.py | 139 B | STUB — select_best(routes) | M06 |
| liquidity_optimizer.py | 82 B | STUB — score_route() | M07 |
| strategy_engine.py | 24 B | STUB — StrategyEngine placeholder | M01 |
| engine.py | 39 B | STUB — minimal engine | M01 |
| converter.py | 84 B | UTIL — asset conversion | M02 |
| across.py | 101 B | ENGINE STUB — Across protocol bridge | M08 |
| state_manager.py | 163 B | CORE | M01 |
| __init__.py | 0 B | Module marker | — |

### TYPESCRIPT / JAVASCRIPT (js/, jsx/, txt/code 3.txt, txt/code 4.txt)

| File | Size | Classification | Module |
|------|------|---------------|--------|
| code 3.txt | 20 KB | AUTHORITATIVE — Full MCP Memory Server (SQLite + BGE embeddings) | M16 |
| code 4.txt | 20 KB | AUTHORITATIVE — LS.Technologies MCP Server (Express + StreamableHTTP) | M17 |
| App.js | 1.4 KB | FRONTEND — React SPA entry point | M18 |
| health-endpoints.js | 3.1 KB | OBSERVABILITY — Webpack dev server health API | M15 |
| charts.js | 2.8 KB | FRONTEND — Chart utilities | M15 |
| crypto_engine.js | 2 KB | ENGINE — JS crypto engine | M06 |
| sw.js | 3.1 KB | PWA — Service worker | M18 |
| use-toast.js | 3.4 KB | UI — Toast notification hook | M18 |
| utils.js | 137 B | UTIL | — |
| script.js | 51 B | FRONTEND stub | — |
| webgpu_vis.js | 600 B | FRONTEND — WebGPU visualization | M15 |
| webpack-health-plugin.js | 3.1 KB | DEVOPS — Webpack health plugin | M15 |
| tailwind.config.js | 2 KB | CONFIG — Tailwind config | — |
| postcss.config.js | 82 B | CONFIG | — |
| craco.config.js | 2.6 KB | CONFIG — CRACO build config | — |
| index.js | 255 B | ENTRY — App entry point | — |
| page (1).jsx | 232 B | PAGE STUB — Next.js page stub | M18 |

### KOTLIN (kt/) — Android Mobile Layer

| File | Size | Classification | Module |
|------|------|---------------|--------|
| Repositories (2).kt | 3.7 KB | AUTHORITATIVE — All 5 data repositories | M18 |
| BiometricGatekeeper (1).kt | 1.6 KB | SECURITY — Biometric auth gate | M12/M18 |
| KeystoreManager (1).kt | 1 KB | SECURITY — Android Keystore wrapper | M12 |
| GuardianService.kt | 1 KB | CORE — Foreground service (START_STICKY) | M18 |
| DashboardScreen (3).kt | 2.8 KB | UI — Dashboard Compose screen | M18 |
| NotificationsScreen (3).kt | 1.5 KB | UI — Notifications screen | M18 |
| SettingsScreen (3).kt | 1.6 KB | UI — Settings screen | M18 |
| StrategiesViewModel (4).kt | 856 B | VIEWMODEL | M18 |
| DashboardViewModel (4).kt | 599 B | VIEWMODEL | M18 |
| SettingsViewModel (4).kt | 640 B | VIEWMODEL | M18 |
| NotificationsViewModel (4).kt | 799 B | VIEWMODEL | M18 |
| SafeControlViewModel (2).kt | 586 B | VIEWMODEL — Emergency controls | M12 |
| Models (2).kt | 802 B | DOMAIN — Data models | M18 |
| AppModule (1).kt | 887 B | DI — Hilt module | M18 |
| AppConfig (1).kt | 91 B | CONFIG | M18 |
| BottomNavBar.kt | 2.2 KB | UI — Navigation bar | M18 |
| MainActivity (1) (1).kt | 823 B | ENTRY — App entry | M18 |
| Destinations (1).kt | 374 B | NAVIGATION — Route constants | M18 |
| build.gradle.kts | 2.7 KB | BUILD | — |
| settings.gradle (2).kts | 49 B | BUILD | — |
| Several STUB screens | ~40–50 B each | STUBS requiring implementation | M18 |

### RUST (rs/) — Native Compute Layer

| File | Size | Classification | Module |
|------|------|---------------|--------|
| lib (1).rs | 1.5 KB | AUTHORITATIVE — JNI bridge, NPU init + inference | M01/M17 |
| npu_accelerator (3).rs | 722 B | CORE — NpuEngine struct | M17 |
| npu_accelerator (9).rs | 279 B | VARIANT — stripped NPU stub | M17 |
| rust_core_src_brain_npu_accelerator (1).rs | 946 B | CORE — Full NPU with Hexagon DSP path | M17 |

### CONFIGURATION / INFRASTRUCTURE

| File | Classification | Notes |
|------|---------------|-------|
| firebase.json | FIREBASE — Hosting config | SPA hosting, cache headers |
| package.json | FRONTEND BUILD — React 19 + shadcn/ui + recharts | Full UI dependency tree |
| docker-compose.yml | INFRA — Basic docker-compose | Simple python:3.11 service |
| deployment.yaml | INFRA — Kubernetes manifest | Single replica, python:3.11 |
| service.yaml | INFRA — K8s service | Port routing |
| backend.env | SECRET — Ethereum RPC + private key | NEVER commit real values |
| nginx.conf | INFRA — Nginx reverse proxy config | — |
| pytest.ini | TEST — Pytest configuration | — |
| Cargo (1).toml | RUST BUILD — Cargo manifest | NIST FIPS 204, libp2p |
| gradle-wrapper.properties | ANDROID BUILD | — |
| jsconfig.json | JS CONFIG | — |
| components.json | SHADCN — Component registry | — |
| manifest.json | PWA — App manifest | — |

### DOCUMENTATION / SPECIFICATIONS (txt/, html/, json/, misc/)

| File | Size | Classification |
|------|------|---------------|
| txt/architecture structure tree.txt | 4.9 KB | CANONICAL — Full platform tree |
| txt/Lennit_Genesis_2026.txt | 1.7 KB | GENESIS — Android + Rust + WebGPU layer spec |
| txt/structure tree and ideas.txt | 192 KB | IDEAS — AI conversation with full platform spec |
| txt/code 1.txt | 120 KB | ARCHITECTURE — March 2026 production-ready stack |
| txt/code 2.txt | 20 KB | RESEARCH — 2026 tech alignment, HiMAP, INFINIT |
| txt/code 3.txt | 45 KB | MCP MEMORY — Full TS implementation |
| txt/code 4.txt | 20 KB | MCP SERVER — LS.Technologies server |
| txt/code 5.txt | 20 KB | LANDING PAGE — Full HTML/CSS frontend |
| txt/code 6.txt | 504 KB | BULK CODE — Large code dump |
| txt/code 7.txt | 4.2 KB | CODE — Additional fragments |
| txt/conversations with code.txt | 235 KB | CONVERSATIONS — AI session logs with code |
| txt/cryptoLyzer advanced code 2.txt | 6.4 KB | BLUEPRINT — Master platform spec |
| txt/cryptoLyzer advanced code 5.txt | 25 KB | CODE — Advanced platform code |
| txt/serial_number.txt | 10 B | IDENTIFIER |
| misc/code brainstorming | 2.7 MB | BINARY/MIXED — Appears to contain binary + text |
| json/export_manifest.json | 93 KB | MANIFEST — Export manifest |
| json/conversations-000.json | 25 MB | CONVERSATIONS — AI chat export (full session) |
| html/chat.html | 27 MB | CONVERSATION — Full chat UI export |

### IMAGE ASSETS (png/)

| File | Notes |
|------|-------|
| 01_alti_agent_governance.png | Agent governance diagram |
| file_*.png (10 files) | AI-generated architecture/UI mockups |

---

## LANGUAGE DISTRIBUTION

| Language | Files | Notes |
|----------|-------|-------|
| Python | 25 | FastAPI backend + engine stubs |
| Kotlin | 30 | Android apps (2 variants) |
| TypeScript/JavaScript | 20+ | Frontend + MCP servers |
| Rust | 4 | JNI/NPU native bridge |
| HTML/CSS | 2 major | Landing page, chat export |
| YAML/TOML/JSON | 12 | Config/build files |

---

## INGESTION STATUS

| Category | Status |
|----------|--------|
| All Python files | ✅ Ingested |
| All Kotlin files | ✅ Ingested |
| All JS/TS files | ✅ Ingested |
| All Rust files | ✅ Ingested |
| All config files | ✅ Ingested |
| Architecture docs | ✅ Ingested |
| Code text files (1-7) | ✅ Ingested (sampled) |
| Conversation exports | ✅ Catalogued (not fully parsed — 25MB+ binary-adjacent content) |
| Binary brainstorm file | ⚠️ Binary/corrupted — architecture intent recovered from text files |
| PNG assets | ✅ Catalogued |

---

## DEFERRED ASSETS

| Asset | Reason | Action |
|-------|--------|--------|
| json/conversations-000.json (25MB) | Too large for full parse | Key patterns extracted from code files |
| html/chat.html (27MB) | Duplicate of conversation content | Catalogued, not reconstructed |
| misc/code brainstorming | Binary corruption | Intent preserved in structure tree + code 1-7 |
| .git (1)/ objects | Git pack binary | Not applicable to reconstruction |
| .github.7z | Requires 7z tool | Catalogued, extraction deferred |

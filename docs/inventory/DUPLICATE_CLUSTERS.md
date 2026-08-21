# Duplicate and Near-Duplicate Clusters

**Scope.** `A`, `E`, and `R` identify the archive roots defined in `SOURCE_INVENTORY.md`; paths are relative to those roots and LOC is actual `wc -l`. “Authoritative reference” means the best legacy reference for migration, not code to copy unchanged into the Kotlin monorepo.

## 1. FastAPI API gateway copies

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| E `backend/app/api/routes.py` | 338 | Full typed FastAPI surface for dashboard, agents, vault, strategies, signals, trade, and emergency operations. | **Authoritative reference:** E copy. Preserve endpoint vocabulary and request/response intent only; replace the FastAPI/mock transport. |
| A `py/routes (2) (1).py` | 338 | Same title, route organization, DTO names, and mocked operation surface. | **Disposition:** OBSOLETE duplicate. Do not migrate a second route contract. |

## 2. Android application variants

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| E `android/app/src/main/java/com/lennit/cryptolyzer/MainActivity.kt` | 152 | Compose host plus WebView JavaScript bridge, safe controls, settings, device info, and biometric callback. | **Authoritative reference:** E activity for Android integration requirements, but refactor to a Compose-only `android:app` host. |
| A `kt/MainActivity (1) (1).kt` | 25 | Loads a local WebView admin console. | **Disposition:** OBSOLETE; WebView-first presentation is superseded. |
| A `kt/MainActivity (2).kt` | 7 | Empty `ComponentActivity`. | **Disposition:** OBSOLETE stub. |
| A `kt/BrochureWebViewScreen.kt` | 1 | Auto-generated placeholder. | **Disposition:** OBSOLETE stub. |

## 3. Compose dashboard, navigation, and view-model variants

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/DashboardScreen.kt` | 102 | Renders dashboard metric cards. | **Authoritative reference:** E screen architecture and visual intent. |
| E `android/app/src/main/java/com/lennit/cryptolyzer/navigation/NavGraph.kt` | 36 | Defines routes and Compose navigation host. | **Authoritative reference:** E navigation organization. |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/DashboardViewModel.kt` | 51 | Refreshes dashboard state. | **Authoritative reference:** E presentation state pattern. |
| A `kt/DashboardScreen (3).kt` | 72 | Older dashboard wired to in-memory view model. | **Disposition:** OBSOLETE duplicate; retain no implementation. |
| A `kt/DashboardViewModel (4).kt` | 18 | Exposes hard-coded dashboard values. | **Disposition:** OBSOLETE duplicate. |
| A `kt/DashboardScreen (1).kt` | 1 | Auto-generated placeholder. | **Disposition:** OBSOLETE stub. |
| A `kt/BottomNavBar.kt` / `kt/Destinations (1).kt` | 50 / 11 | Earlier bottom navigation and route enum. | **Disposition:** REFACTOR only where E navigation omits a reusable behavior. |

## 4. Notification, settings, strategy, and safe-control screen families

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/{Notifications,Settings,Strategies,Safe}Screen.kt` | 63 / 115 / 112 / 82 | Full Compose operational screens. | **Authoritative reference:** E screen families. |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/{Notifications,Settings,Strategies,SafeControl}ViewModel.kt` | 26 / 26 / 35 / 52 | Corresponding presentation actions. | **Authoritative reference:** E view-model responsibilities. |
| A `kt/NotificationsScreen (3).kt`; `SettingsScreen (3).kt` | 42 / 42 | Earlier Compose implementations. | **Disposition:** OBSOLETE duplicates. |
| A `kt/NotificationsViewModel (4).kt`; `SettingsViewModel (4).kt`; `StrategiesViewModel (4).kt`; `SafeControlViewModel (2).kt` | 21 / 21 / 22 / 17 | In-memory-state variants. | **Disposition:** OBSOLETE duplicates; discard mutable fake state. |
| A `kt/NotificationsViewModel (1).kt`; `SettingsViewModel (2).kt`; `StrategiesViewModel (2).kt`; `SafeControlViewModel (1).kt`; `SafeScreen (1).kt`; `SettingsScreen (2).kt`; `StrategiesScreen (2).kt` | 1 each | Auto-generated placeholders. | **Disposition:** OBSOLETE stubs. |

## 5. Domain data model copies

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/model/{Agent,AppConfig,NotificationItem,Strategy,SystemStatus,VaultPosition}.kt` | 15 / 12 / 11 / 14 / 12 / 10 | Kotlin types for agents, settings, notifications, strategies, system mode, and positions. | **Authoritative reference:** E Kotlin type vocabulary. Replace `Double` currency fields with fixed-point/decimal domain values. |
| R `src/types.ts` | 43 | TypeScript types for agent, wallet, treasury, and security level. | **Disposition:** MIGRATE-CONCEPT-ONLY; merge unique wallet/identity fields. |
| A `kt/Models (2).kt` | 39 | Earlier Kotlin versions of the same model family. | **Disposition:** OBSOLETE duplicate; do not carry its `Double` money fields forward. |

## 6. Repository and state-holder variants

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/repository/CryptolyzerRepository.kt` | 16 | Read and command contract for the Android presentation layer. | **Authoritative reference:** E interface shape; split commands and queries in the new modules. |
| E `android/app/src/main/java/com/lennit/cryptolyzer/data/repository/{MockRepository,RemoteRepository}.kt` | 88 / 107 | Fixture repository and FastAPI DTO mapper. | **Disposition:** REFACTOR mock fixture behavior; REWRITE remote coupling. |
| A `kt/Repositories (2).kt` | 100 | StateFlow in-memory agent, vault, strategy, notification, and system repositories. | **Disposition:** MIGRATE-CONCEPT-ONLY; preserve state-flow UX only, discard in-memory source of truth. |
| R `src/modules/m16-memory/MemoryStore.ts` | 120 | Firestore-backed memory write/read/listener store. | **Disposition:** MIGRATE-CONCEPT-ONLY; preserve memory schema ideas, replace Firestore. |

## 7. Event-bus implementations

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| R `src/lib/eventBus.ts` | 80 | Enumerates named system events, metadata, subscribe/unsubscribe, and synchronous publish. | **Authoritative reference:** R event taxonomy and envelope fields. Implement durable local Kotlin event contracts in `core:eventbus`. |
| A `py/event_bus.py` | 10 | Minimal event-to-callback-list dispatcher. | **Disposition:** OBSOLETE implementation; no durability, metadata, or unsubscribe. |
| E `backend/app/services/notification_service.py` | 37 | In-memory notification collection and broadcast behavior. | **Disposition:** MIGRATE-CONCEPT-ONLY; it is a consumer pattern, not the bus. |

## 8. Risk gate and trade-validation overlap

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| R `src/modules/m12-security/RiskEngine.ts` | 119 | Scores blacklisted destination, amount, proxy/liquidity, and MEV slippage; blocks unsafe operations. | **Authoritative reference:** R policy ordering, thresholds, audit-event intent, and gate shape. Reimplement with decimal amounts and explicit policy data. |
| E `backend/app/services/trade_validator.py` | 30 | Checks position size, slippage, and profitability. | **Disposition:** Merge its check categories into `core:policy`; discard Python. |
| A `py/risk_manager.py` | 1 | Always returns `OK`. | **Disposition:** OBSOLETE stub. |

## 9. Treasury, portfolio, and vault overlap

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| R `src/modules/m02-treasury/TreasuryEngine.ts` | 42 | Holds positions and emits a security-gated rebalance event. | **Authoritative reference:** R rebalance operation contract. |
| E `backend/app/services/portfolio_service.py` | 47 | Calculates portfolio total/allocation and rebalance. | **Disposition:** Merge valuation/allocation semantics into `core:domain` and `core:policy`. |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/VaultScreen.kt` | 73 | Operational vault position and rebalance UX. | **Disposition:** REFACTOR UX into `android:app`. |
| A `kt/Repositories (2).kt` | 100 | Includes static in-memory vault positions. | **Disposition:** OBSOLETE as a data source. |

## 10. Strategy, MEV, bridge, and chain-intelligence overlap

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| R `src/modules/m06-mev/MevEngine.ts` | 93 | Defines opportunity shape, quote-floor check, gas adjustment, and gated execution sequence. | **Authoritative reference:** R decision-flow outline, not its simulated data or `number` arithmetic. |
| E `backend/app/execution/dex_executor.py` | 73 | Models DEX swap execution. | **Disposition:** MIGRATE-CONCEPT-ONLY. |
| E `backend/app/blockchain/{web3_client,flashbots}.py` | 67 / 57 | Multi-chain RPC and private-bundle abstractions. | **Disposition:** MIGRATE-CONCEPT-ONLY. |
| E `backend/app/strategies/{cross_chain,mev_arbitrage,yield_farming}.py` | 39 / 34 / 32 | Strategy placeholders. | **Disposition:** MIGRATE-CONCEPT-ONLY only. |
| A `py/{bridge_engine,bridge_orchestrator,bridge_router,bridge_executor,base_bridge,across,stargate,multi_chain}.py` | 3 / 4 / 2 / 2 / 2 / 3 / 3 / 11 | Fragmented bridge/multi-chain sketches. | **Disposition:** Merge only adapter and routing concepts; discard all implementations. |
| R `src/modules/m08-blockchain/BlockchainIntel.ts` | 72 | Configures chains and simulates chain metrics/wallet lookup. | **Disposition:** MIGRATE-CONCEPT-ONLY. |

## 11. Agent orchestration variants

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| R `src/modules/m01-orchestration/AgentManager.ts` | 307 | Manages registration, heartbeat, commands, autonomous loops, and event/memory emissions. | **Authoritative reference:** R lifecycle events and state transitions. Discard Firestore/interval singleton mechanics. |
| E `backend/app/agents/alpha_grid.py` | 174 | Defines 20 agent roles, metrics, and collective controls. | **Disposition:** Merge role taxonomy and metrics only. |
| E `backend/app/agents/{execution_agent,orchestrator}.py` | 44 / 55 | Execution cycle and master loop/profit monitor. | **Disposition:** Merge control-loop concepts only. |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/AgentsScreen.kt` | 119 | Agent monitoring/control UX. | **Disposition:** REFACTOR into `android:app`. |

## 12. PWA utility copies and WebView presentation

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| E `web/js/{charts,crypto_engine}.js`; `web/sw.js` | 96 / 76 / 92 | Performance chart, RSI/ticker, and cache behavior. | **Authoritative reference:** E copies for UX behavior only. |
| A `js/{charts,crypto_engine,sw}.js` | 96 / 76 / 92 | Earlier copies with the same responsibilities and LOC. | **Disposition:** OBSOLETE duplicates. |
| E `web/js/app.js` | 578 | Mobile PWA control surface with FastAPI/WebSocket/Android bridge behavior. | **Disposition:** MIGRATE-CONCEPT-ONLY; consolidate presentation in Compose. |
| A `kt/MainActivity (1) (1).kt` | 25 | Embeds a WebView-based console. | **Disposition:** OBSOLETE; do not preserve the container. |

## 13. NPU and native-core variants

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| E `rust-core/src/{lib,npu_accelerator}.rs` | 130 / 65 | JNI boundary with mutex initialization and a CPU transform labeled as NPU acceleration. | **Authoritative reference:** E JNI safety boundary and target-device intent. |
| A `rs/lib (1).rs`; `rs/npu_accelerator (3).rs` | 51 / 23 | Earlier JNI and NPU sketches. | **Disposition:** OBSOLETE duplicates. |
| A `rs/rust_core_src_brain_npu_accelerator (1).rs` | 33 | Alternate Hexagon NPU sketch. | **Disposition:** Merge no code; record alternative hardware intent. |
| A `rs/npu_accelerator (9).rs` | 8 | Skeletal NPU type. | **Disposition:** OBSOLETE stub. |
| E `rust-core/src/{mesh_sync,hybrid_sign,zk_oracle}.rs` | 35 / 40 / 40 | Placeholder mesh, post-quantum signing, and ZK proofs. | **Disposition:** EXPERIMENTAL and deferred; none is security-grade. |

## 14. Firebase configuration and persistence duplication

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| R `src/lib/{firebase,firebaseAdmin,firestoreUtils}.ts` | 29 / 13 / 49 | Client/admin initialization and Firestore failure serialization. | **Authoritative reference:** None; Firebase is removed from core. |
| R `firebase-applet-config.json`; `firebase-blueprint.json` | 9 / 95 | Firebase applet/blueprint configuration. | **Disposition:** OBSOLETE. |
| E `infra/firebase/firebase.json` | 19 | Firebase deployment configuration. | **Disposition:** OBSOLETE. |
| A `json/{firebase.json,firebase_config (2).json}` | 19 / 2 | Firebase project/config fragments. | **Disposition:** OBSOLETE. |

## 15. Competing module maps

| Member | LOC | Evidence | Decision |
|---|---:|---|---|
| R `CANONICAL_MODULE_MAP.md` | 25 | Legacy M01–M20 names. | **Authoritative reference:** Module identities only. |
| Repository-root `CANONICAL_MODULE_MAP.md` | 233 | Firebase Cloud Functions, Firestore roots, Pub/Sub topics, and scheduler mappings. | **Disposition:** Superseded; no runtime authority. |
| Repository-root `00_MASTER_SYSTEM_CONTEXT.md` | 378 | Calls the architecture Firebase-native. | **Disposition:** Superseded. |
| This repository’s new architecture | — | Android-first, local-first Kotlin core modules and optional later sync. | **Disposition:** The migration map is authoritative for current target placement. |

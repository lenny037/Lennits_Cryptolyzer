# Source Inventory

**Inspection basis.** LOC is the actual `wc -l` result. `A` = `Lennits_Cryotolyzer code archives`, `E` = `LENNITS_CRYPTOLYZER_ENTERPRISE/LENNITS_CRYPTOLYZER`, and `R` = `Lennits_Cryptolyzer1-main/Lennits_Cryptolyzer1-main`; every path below is relative to that root. `STUB` means fewer than about 15 LOC. The classification is for the Android-first, local-first Kotlin direction: no Firebase/Firestore or Python runtime is retained in core.

## Python backend

| Root / path | Language | LOC | STUB | Actually does | Classification |
|---|---:|---:|---|---|---|
| E `backend/app/agents/alpha_grid.py` | Python | 174 | — | Defines 20-agent role metrics and collective start/stop loops. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/agents/execution_agent.py` | Python | 44 | — | Runs a validating execution-agent cycle. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/agents/orchestrator.py` | Python | 55 | — | Provides master agent loop, profit monitor, and shutdown. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/api/deps.py` | Python | 22 | — | Supplies API-key and rate-limit dependencies. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/api/routes.py` | Python | 338 | — | Declares FastAPI DTOs and mocked dashboard, agent, vault, strategy, signal, trade, and emergency routes. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/api/websocket.py` | Python | 64 | — | Manages metric WebSocket connections and broadcasts. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/blockchain/flashbots.py` | Python | 57 | — | Models and simulates private transaction bundles. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/blockchain/web3_client.py` | Python | 67 | — | Initializes multi-chain Web3 clients and exposes balance/gas checks. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/core/config.py` | Python | 49 | — | Loads application settings. | REWRITE |
| E `backend/app/core/logger.py` | Python | 19 | — | Creates structured Python loggers. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/core/rate_limiter.py` | Python | 33 | — | Implements an in-memory token bucket. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/core/security.py` | Python | 36 | — | Provides JWT/API-key and password helper functions. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/execution/dex_executor.py` | Python | 73 | — | Models DEX swaps and returns a trade result. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/main.py` | Python | 74 | — | Configures FastAPI lifespan, CORS, routers, and health/root endpoints. | OBSOLETE |
| E `backend/app/ml/backtest.py` | Python | 96 | — | Simulates strategy performance and returns backtest metrics. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/ml/feature_engineering.py` | Python | 65 | — | Adds technical indicators to market data. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/ml/ingest.py` | Python | 74 | — | Fetches or synthesizes OHLCV data through an async ingestor. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/ml/model.py` | Python | 74 | — | Trains, predicts with, saves, and loads sklearn model artifacts. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/ml/predictor.py` | Python | 42 | — | Loads a model and returns live predictions. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/ml/rl_agent.py` | Python | 85 | — | Supplies a Q-table experience/replay trading-agent sketch. | EXPERIMENTAL |
| E `backend/app/services/gas_service.py` | Python | 36 | — | Calculates multi-chain gas acceptance and USD estimates. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/services/notification_service.py` | Python | 37 | — | Keeps in-memory notifications and broadcasts them. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/services/portfolio_service.py` | Python | 47 | — | Holds assets and calculates allocations/rebalance. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/services/profit_service.py` | Python | 39 | — | Records profit entries and returns summaries. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/services/signal_service.py` | Python | 39 | — | Returns generated trading signals from on-chain data placeholders. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/services/trade_validator.py` | Python | 30 | — | Applies position-size, slippage, and profitability checks. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/strategies/base.py` | Python | 26 | — | Defines the strategy result and abstract strategy contract. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/strategies/airdrop_hunter.py` | Python | 37 | — | Evaluates and executes airdrop-hunter strategy placeholders. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/strategies/cross_chain.py` | Python | 39 | — | Evaluates and executes cross-chain opportunity placeholders. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/strategies/mev_arbitrage.py` | Python | 34 | — | Evaluates and executes MEV-arbitrage placeholders. | MIGRATE-CONCEPT-ONLY |
| E `backend/app/strategies/yield_farming.py` | Python | 32 | — | Evaluates and executes yield-farming placeholders. | MIGRATE-CONCEPT-ONLY |
| E `backend/tests/test_alpha_grid.py` | Python | 37 | — | Tests AlphaGrid initialization, halt, and role allocation. | MIGRATE-CONCEPT-ONLY |
| E `backend/tests/test_api.py` | Python | 56 | — | Exercises the mocked API endpoints. | MIGRATE-CONCEPT-ONLY |
| E `backend/tests/test_services.py` | Python | 38 | — | Tests validator, profit, and gas-service behavior. | MIGRATE-CONCEPT-ONLY |
| A `py/routes (2) (1).py` | Python | 338 | — | Earlier copy of the same FastAPI route surface with mock values. | OBSOLETE |
| A `py/event_bus.py` | Python | 10 | STUB | Implements in-process subscribe/publish callbacks. | MIGRATE-CONCEPT-ONLY |
| A `py/state_manager.py` | Python | 6 | STUB | Appends items to in-memory event/result state. | MIGRATE-CONCEPT-ONLY |
| A `py/task_queue.py` | Python | 11 | STUB | Wraps a deque with push/pop. | MIGRATE-CONCEPT-ONLY |
| A `py/multi_chain.py` | Python | 11 | STUB | Reads configured Ethereum and Base wallet balances. | MIGRATE-CONCEPT-ONLY |
| A `py/across.py` | Python | 3 | STUB | Returns an Across bridge success placeholder. | EXPERIMENTAL |
| A `py/airdrop_engine.py` | Python | 3 | STUB | Averages activity values as an airdrop score. | EXPERIMENTAL |
| A `py/airdrop_multi.py` | Python | 5 | STUB | Imports wallet config and returns no detected airdrops. | OBSOLETE |
| A `py/base_bridge.py` | Python | 2 | STUB | Declares an unimplemented bridge adapter method. | MIGRATE-CONCEPT-ONLY |
| A `py/bounty_engine.py` | Python | 3 | STUB | Scores a reward against difficulty. | EXPERIMENTAL |
| A `py/bridge_engine.py` | Python | 3 | STUB | Chooses a simple bridge/send route by amount and chain. | EXPERIMENTAL |
| A `py/bridge_executor.py` | Python | 2 | STUB | Echoes a requested bridge execution. | OBSOLETE |
| A `py/bridge_orchestrator.py` | Python | 4 | STUB | Selects an adapter and invokes its bridge method. | MIGRATE-CONCEPT-ONLY |
| A `py/bridge_router.py` | Python | 2 | STUB | Selects the first available bridge protocol. | EXPERIMENTAL |
| A `py/converter.py` | Python | 1 | STUB | Returns a converted flag without conversion. | OBSOLETE |
| A `py/engine.py` | Python | 2 | STUB | Returns a fixed “system running” string. | OBSOLETE |
| A `py/faucet_engine.py` | Python | 3 | STUB | Returns a claimed status for a wallet. | EXPERIMENTAL |
| A `py/liquidity_optimizer.py` | Python | 2 | STUB | Adds route gas, slippage, and time values. | MIGRATE-CONCEPT-ONLY |
| A `py/live_trade.py` | Python | 2 | STUB | Returns unexecuted buy/sell request echoes. | OBSOLETE |
| A `py/risk_manager.py` | Python | 1 | STUB | Always returns `OK` risk. | OBSOLETE |
| A `py/route_selector.py` | Python | 3 | STUB | Sorts routes using the simple liquidity score. | MIGRATE-CONCEPT-ONLY |
| A `py/stargate.py` | Python | 3 | STUB | Returns a Stargate bridge success placeholder. | EXPERIMENTAL |
| A `py/strategy_engine.py` | Python | 1 | STUB | Always emits `BUY`. | OBSOLETE |
| A `py/yield_engine.py` | Python | 3 | STUB | Divides APY by a risk floor. | MIGRATE-CONCEPT-ONLY |

## Android / Kotlin

| Root / path | Language | LOC | STUB | Actually does | Classification |
|---|---:|---:|---|---|---|
| E `android/app/src/main/java/com/lennit/cryptolyzer/MainActivity.kt` | Kotlin | 152 | — | Hosts Compose UI and exposes safe-mode, shutdown, settings, device, and biometric bridge calls to WebView JavaScript. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/CryptolyzerApp.kt` | Kotlin | 15 | — | Initializes the Hilt application. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/data/local/SecureStorage.kt` | Kotlin | 34 | — | Wraps encrypted shared preferences. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/data/local/SettingsDataStore.kt` | Kotlin | 59 | — | Persists app configuration with DataStore flows. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/data/remote/LennitApiService.kt` | Kotlin | 82 | — | Defines Retrofit calls for the legacy FastAPI route surface. | REWRITE |
| E `android/app/src/main/java/com/lennit/cryptolyzer/data/remote/dto/ApiDtos.kt` | Kotlin | 96 | — | Defines network DTOs for legacy dashboard and command routes. | REWRITE |
| E `android/app/src/main/java/com/lennit/cryptolyzer/data/repository/MockRepository.kt` | Kotlin | 88 | — | Supplies randomized/fixture dashboard, agent, vault, strategy, and notification data. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/data/repository/RemoteRepository.kt` | Kotlin | 107 | — | Maps Retrofit DTOs to domain models and dispatches commands. | REWRITE |
| E `android/app/src/main/java/com/lennit/cryptolyzer/data/websocket/MetricsWebSocket.kt` | Kotlin | 46 | — | Exposes metric WebSocket messages as a Flow. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/di/AppModule.kt` | Kotlin | 78 | — | Provides OkHttp, Retrofit, and named legacy repositories with Hilt. | REWRITE |
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/model/Agent.kt` | Kotlin | 15 | — | Defines agent identity, status, role, profit, and activity fields. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/model/AppConfig.kt` | Kotlin | 12 | STUB | Defines endpoint, demo, limit, display, and biometric configuration. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/model/NotificationItem.kt` | Kotlin | 11 | STUB | Defines notification message and severity. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/model/Strategy.kt` | Kotlin | 14 | STUB | Defines strategy type, mode, allocation, PnL, and win rate. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/model/SystemStatus.kt` | Kotlin | 12 | STUB | Defines operating mode and aggregate system state. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/model/VaultPosition.kt` | Kotlin | 10 | STUB | Defines an asset position with amount, value, chain, and APY. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/domain/repository/CryptolyzerRepository.kt` | Kotlin | 16 | — | Defines dashboard reads and operational command contract. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/navigation/NavGraph.kt` | Kotlin | 36 | — | Declares Compose routes and nav host. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/AgentsScreen.kt` | Kotlin | 119 | — | Renders agent roster, status chips, and controls. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/DashboardScreen.kt` | Kotlin | 102 | — | Renders dashboard metric cards. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/NotificationsScreen.kt` | Kotlin | 63 | — | Renders notification cards. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/SafeScreen.kt` | Kotlin | 82 | — | Renders safe-mode, shutdown, and resume controls. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/SettingsScreen.kt` | Kotlin | 115 | — | Renders connection and operational settings. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/StrategiesScreen.kt` | Kotlin | 112 | — | Renders strategy cards and mode controls. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/screens/VaultScreen.kt` | Kotlin | 73 | — | Renders vault positions and rebalance UI. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/security/BiometricGatekeeper.kt` | Kotlin | 55 | — | Maps Android biometric outcomes to sealed results. | RETAIN |
| E `android/app/src/main/java/com/lennit/cryptolyzer/security/KeystoreManager.kt` | Kotlin | 63 | — | Encrypts/decrypts strings with Android Keystore AES-GCM. | RETAIN |
| E `android/app/src/main/java/com/lennit/cryptolyzer/service/GuardianService.kt` | Kotlin | 65 | — | Runs a persistent foreground monitoring notification. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/ui/theme/Color.kt` | Kotlin | 25 | — | Defines AMOLED-oriented color values. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/ui/theme/Theme.kt` | Kotlin | 29 | — | Defines the Compose material theme. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/AgentsViewModel.kt` | Kotlin | 41 | — | Loads agents and sends controls. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/DashboardViewModel.kt` | Kotlin | 51 | — | Refreshes dashboard state. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/NotificationsViewModel.kt` | Kotlin | 26 | — | Loads notifications. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/SafeControlViewModel.kt` | Kotlin | 52 | — | Sends safe-mode, shutdown, and resume commands. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/SettingsViewModel.kt` | Kotlin | 26 | — | Saves settings through DataStore. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/StrategiesViewModel.kt` | Kotlin | 35 | — | Loads strategies and changes their mode. | REFACTOR |
| E `android/app/src/main/java/com/lennit/cryptolyzer/viewmodel/VaultViewModel.kt` | Kotlin | 42 | — | Loads vault state and triggers rebalance. | REFACTOR |
| A `kt/AppConfig (1).kt` | Kotlin | 5 | STUB | Holds mutable in-memory demo-mode flag. | OBSOLETE |
| A `kt/AppModule (1).kt` | Kotlin | 32 | — | Binds in-memory repositories with Hilt. | OBSOLETE |
| A `kt/BiometricGatekeeper (1).kt` | Kotlin | 43 | — | Older Boolean-only biometric shutdown prompt. | OBSOLETE |
| A `kt/BottomNavBar.kt` | Kotlin | 50 | — | Renders seven-item Compose bottom navigation. | REFACTOR |
| A `kt/DashboardScreen (3).kt` | Kotlin | 72 | — | Renders a compact dashboard and safe navigation button. | OBSOLETE |
| A `kt/DashboardViewModel (4).kt` | Kotlin | 18 | — | Exposes mock dashboard values and system status. | OBSOLETE |
| A `kt/Destinations (1).kt` | Kotlin | 11 | STUB | Defines route/label enum. | REFACTOR |
| A `kt/GuardianService.kt` | Kotlin | 27 | — | Earlier sticky foreground service. | OBSOLETE |
| A `kt/KeystoreManager (1).kt` | Kotlin | 28 | — | Creates/retrieves a Keystore AES key but does not encrypt. | OBSOLETE |
| A `kt/MainActivity (1) (1).kt` | Kotlin | 25 | — | Loads a local WebView admin console. | OBSOLETE |
| A `kt/MainActivity (2).kt` | Kotlin | 7 | STUB | Empty ComponentActivity variant. | OBSOLETE |
| A `kt/Models (2).kt` | Kotlin | 39 | — | Earlier agent, vault, strategy, notification, and system models using `Double` money. | OBSOLETE |
| A `kt/NotificationsScreen (3).kt` | Kotlin | 42 | — | Renders notifications from a view model. | OBSOLETE |
| A `kt/NotificationsViewModel (4).kt` | Kotlin | 21 | — | Streams and marks in-memory notifications read. | OBSOLETE |
| A `kt/Repositories (2).kt` | Kotlin | 100 | — | Defines in-memory StateFlow repositories for the legacy screens. | OBSOLETE |
| A `kt/SafeControlViewModel (2).kt` | Kotlin | 17 | — | Sets in-memory shutdown state. | OBSOLETE |
| A `kt/SettingsScreen (3).kt` | Kotlin | 42 | — | Renders demo setting and future wallet note. | OBSOLETE |
| A `kt/SettingsViewModel (4).kt` | Kotlin | 21 | — | Holds in-memory demo setting and version. | OBSOLETE |
| A `kt/StrategiesViewModel (4).kt` | Kotlin | 22 | — | Streams and changes in-memory strategy modes. | OBSOLETE |
| A `kt/BrochureWebViewScreen.kt`; `DashboardScreen (1).kt`; `NotificationsViewModel (1).kt`; `SafeControlViewModel (1).kt`; `SafeScreen (1).kt`; `SettingsScreen (2).kt`; `SettingsViewModel (2).kt`; `StrategiesScreen (2).kt`; `StrategiesViewModel (2).kt`; `TreasuryScreen.kt`; `TreasuryViewModel.kt`; `VaultViewModel (1).kt` | Kotlin | 1 each | STUB | Auto-generated filename placeholders with no implementation. | OBSOLETE |

## TypeScript modules

| Root / path | Language | LOC | STUB | Actually does | Classification |
|---|---:|---:|---|---|---|
| R `src/modules/m01-orchestration/AgentManager.ts` | TypeScript | 307 | — | Syncs Firestore agents, runs heartbeat/arbitrage loops, emits events, and logs memory. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m02-treasury/TreasuryEngine.ts` | TypeScript | 42 | — | Holds mock positions and security-gates rebalance events. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m03-airdrop/AirdropIntel.ts` | TypeScript | 70 | — | Publishes seeded airdrop opportunities and random eligibility. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m04-faucet/FaucetHarvester.ts` | TypeScript | 48 | — | Security-gates mock faucet claims and emits events. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m06-mev/MevEngine.ts` | TypeScript | 93 | — | Finds seeded arbitrage, applies a 5% quote floor, and simulates private execution. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m08-blockchain/BlockchainIntel.ts` | TypeScript | 72 | — | Tracks configured chains and simulates block/gas/wallet data. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m12-security/RiskEngine.ts` | TypeScript | 119 | — | Scores destination, amount, proxy/liquidity, and slippage risks; gates execution. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m15-analytics/AnalyticsEngine.ts` | TypeScript | 55 | — | Aggregates in-memory KPIs from all system events. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m16-memory/MemoryStore.ts` | TypeScript | 120 | — | Stores, observes, and retrieves Firestore-backed memories. | MIGRATE-CONCEPT-ONLY |
| R `src/modules/m17-execution/GeminiBridge.ts` | TypeScript | 37 | — | Calls Gemini 1.5 Flash for a response or system plan. | EXPERIMENTAL |
| R `src/lib/eventBus.ts` | TypeScript | 80 | — | Declares event names, metadata, subscriptions, and synchronous publish. | MIGRATE-CONCEPT-ONLY |
| R `src/lib/firebase.ts` | TypeScript | 29 | — | Initializes Firebase client app, auth, and Firestore. | OBSOLETE |
| R `src/lib/firebaseAdmin.ts` | TypeScript | 13 | STUB | Initializes Firebase Admin Firestore server handle. | OBSOLETE |
| R `src/lib/firestoreUtils.ts` | TypeScript | 49 | — | Serializes Firebase operation and auth failures. | OBSOLETE |
| R `src/lib/schemas.ts` | TypeScript | 11 | STUB | Defines Zod agent configuration bounds. | MIGRATE-CONCEPT-ONLY |
| R `src/lib/utils.ts` | TypeScript | 6 | STUB | Merges CSS class names. | OBSOLETE |
| R `src/types.ts` | TypeScript | 43 | — | Defines profile, wallet, agent, treasury, and security types. | MIGRATE-CONCEPT-ONLY |
| R `server.ts` | TypeScript | 140 | — | Serves Express API endpoints over in-memory module singleton state. | OBSOLETE |
| R `src/App.tsx` | TSX | 300 | — | Renders dashboard shell, polls KPI/log endpoints, and selects main tabs. | MIGRATE-CONCEPT-ONLY |
| R `src/components/AgentsPage.tsx` | TSX | 634 | — | Renders agent list, logs, controls, filters, and configuration UI. | MIGRATE-CONCEPT-ONLY |
| R `src/components/LennitLogo.tsx` | TSX | 290 | — | Renders adaptive branded SVG logo. | MIGRATE-CONCEPT-ONLY |
| R `src/main.tsx`; `src/index.css` | TSX/CSS | 10; 33 | STUB/— | Mounts the React app; supplies global visual styles. | OBSOLETE |

## Web PWA

| Root / path | Language | LOC | STUB | Actually does | Classification |
|---|---:|---:|---|---|---|
| E `web/index.html` | HTML | 92 | — | Defines splash shell, seven operational screens, and bottom navigation. | MIGRATE-CONCEPT-ONLY |
| E `web/js/app.js` | JavaScript | 578 | — | Handles routing, FastAPI calls, WebSocket reconnect, mock data, safe controls, and Android bridge integration. | MIGRATE-CONCEPT-ONLY |
| E `web/js/charts.js` | JavaScript | 96 | — | Draws a synthetic 30-day portfolio canvas chart. | MIGRATE-CONCEPT-ONLY |
| E `web/js/crypto_engine.js` | JavaScript | 76 | — | Computes RSI signals and simulates a price feed. | MIGRATE-CONCEPT-ONLY |
| E `web/sw.js` | JavaScript | 92 | — | Implements static cache-first and API network-first offline behavior. | MIGRATE-CONCEPT-ONLY |
| E `web/styles.css` | CSS | 359 | — | Styles the AMOLED PWA shell, cards, controls, and navigation. | MIGRATE-CONCEPT-ONLY |
| E `web/manifest.json` | JSON | 16 | — | Declares installable PWA metadata and icon references. | OBSOLETE |
| A `js/App.js` | JavaScript | 54 | — | Minimal React app that calls a backend hello endpoint. | OBSOLETE |
| A `js/charts.js` | JavaScript | 96 | — | Earlier copy of the canvas performance chart. | OBSOLETE |
| A `js/crypto_engine.js` | JavaScript | 76 | — | Earlier copy of simulated ticker and RSI helper. | OBSOLETE |
| A `js/sw.js` | JavaScript | 92 | — | Earlier copy of PWA cache worker. | OBSOLETE |
| A `js/health-endpoints.js` | JavaScript | 213 | — | Adds detailed health/readiness/metrics routes to a dev server. | MIGRATE-CONCEPT-ONLY |
| A `js/use-toast.js` | JavaScript | 165 | — | Implements a React toast reducer and hook. | MIGRATE-CONCEPT-ONLY |
| A `js/webgpu_vis.js` | JavaScript | 16 | — | Initializes a WebGPU shader module; no rendering pipeline. | EXPERIMENTAL |
| A `js/webpack-health-plugin.js` | JavaScript | 120 | — | Implements a Webpack health endpoint plugin. | MIGRATE-CONCEPT-ONLY |
| A `js/craco.config.js`; `js/tailwind.config.js`; `js/postcss.config.js`; `js/index.js`; `js/utils.js`; `js/script.js`; `jsx/page (1).jsx` | JavaScript/JSX | 90; 82; 6; 11; 6; 2; 9 | mixed | Build configuration, entry/utilities, and a minimal page placeholder. | OBSOLETE |

## Rust

| Root / path | Language | LOC | STUB | Actually does | Classification |
|---|---:|---:|---|---|---|
| E `rust-core/src/lib.rs` | Rust | 130 | — | Exports Android JNI initialization, inference, and solvency entry points. | EXPERIMENTAL |
| E `rust-core/src/npu_accelerator.rs` | Rust | 65 | — | Applies a CPU sigmoid transform with inference counters and nominal thermal check. | EXPERIMENTAL |
| E `rust-core/src/mesh_sync.rs` | Rust | 35 | — | Provides a disconnected mesh-node sketch with TODO libp2p behavior. | EXPERIMENTAL |
| E `rust-core/src/hybrid_sign.rs` | Rust | 40 | — | Returns placeholder hybrid signatures and non-empty verification. | EXPERIMENTAL |
| E `rust-core/src/zk_oracle.rs` | Rust | 40 | — | Panics in debug or returns placeholder solvency bytes in release. | EXPERIMENTAL |
| A `rs/lib (1).rs` | Rust | 51 | — | Older Android JNI bridge module. | OBSOLETE |
| A `rs/npu_accelerator (3).rs` | Rust | 23 | — | Defines a simple Snapdragon-targeted NPU inference transform. | EXPERIMENTAL |
| A `rs/npu_accelerator (9).rs` | Rust | 8 | STUB | Declares a skeletal NPU engine. | OBSOLETE |
| A `rs/rust_core_src_brain_npu_accelerator (1).rs` | Rust | 33 | — | Alternative Hexagon NPU engine sketch. | EXPERIMENTAL |

## Config / infrastructure

| Root / path | Language | LOC | STUB | Actually does | Classification |
|---|---:|---:|---|---|---|
| E `android/app/build.gradle.kts`; `android/build.gradle.kts`; `android/settings.gradle.kts`; `android/gradle/libs.versions.toml` | Gradle/TOML | 99; 7; 16; 48 | mixed | Configures the enterprise Android build and dependency versions. | REWRITE |
| E `android/app/src/main/AndroidManifest.xml`; `android/app/src/main/res/drawable/ic_guardian.xml`; `android/app/src/main/res/values/{colors,strings,themes}.xml` | XML | 60; 9; 10; 6; 10 | mixed | Declares Android components and minimal guardian/theme resources. | REFACTOR |
| E `backend/Dockerfile`; `backend/requirements.txt`; `backend/.env.example` | Docker/Python env | 15; 22; 36 | — | Builds and configures the discarded FastAPI service. | OBSOLETE |
| E `infra/docker/docker-compose.yml`; `infra/ci/.github-workflows-deploy.yml` | YAML | 84; 43 | — | Defines legacy Docker service topology and deploy workflow. | REWRITE |
| E `infra/firebase/firebase.json` | JSON | 19 | — | Firebase project configuration. | OBSOLETE |
| E `rust-core/Cargo.toml` | TOML | 21 | — | Declares Android JNI native-core crate dependencies. | EXPERIMENTAL |
| A `kt/build.gradle.kts`; `kt/settings.gradle (2).kts` | Gradle | 91; 2 | mixed | Earlier single-app Android Gradle setup. | OBSOLETE |
| A `toml/Cargo (1).toml`; `yml/docker-compose.yml`; `misc/{deployment.yaml,service.yaml,nginx.conf,pytest.ini,backend.env,gradle-wrapper.properties,gradlew}` | TOML/YAML/misc | 13; 8; 19; 11; 55; 6; 2; 5; 2 | mixed | Legacy JNI, Docker/Kubernetes, Nginx, test, and wrapper configuration. | OBSOLETE |
| A `json/{firebase.json,firebase_config (2).json,components.json,jsconfig.json,manifest.json,package.json}` | JSON | 19; 2; 21; 9; 16; 91 | mixed | Firebase and legacy web/PWA package metadata. | OBSOLETE |

| R `package.json`; `tsconfig.json`; `vite.config.ts`; `index.html` | JSON/TypeScript/HTML | 44; 26; 26; 13 | mixed | Declares React/Vite dependencies, compiler aliases, bundler setup, and the Vite entry shell. | OBSOLETE |
| R `firebase-applet-config.json`; `firebase-blueprint.json`; `metadata.json` | JSON | 9; 95; 8 | mixed | Firebase applet/blueprint and generated metadata configuration. | OBSOLETE |


## Non-code assets

Skipped from the tables: `A/json/conversations-000.json` (404,491 LOC), `A/json/export_manifest.json` (2,034 LOC), `A/json/message_feedback (2).json` (86 LOC), `A/json/shared_conversations (2).json` (80 LOC), `A/html/chat.html` (137 LOC), `A/misc/code brainstorming` (21,315 LOC), and `A/misc/yarn.lock` (11,106 LOC). They are exports, conversation material, a browser export, or generated lock data rather than source artifacts. The PWA manifests reference image assets; no image/binary asset is classified as code.

# LENNITS_CRYPTOLYZER
### A Mega Blockchain Infrastructure Platform by LENNIT_SUITE TECHNOLOGIES

---

## Architecture Overview

```
LENNITS_CRYPTOLYZER/              ← Enterprise Root
├── backend/                      ← MODULE 13-17: Python FastAPI Engine
│   ├── app/
│   │   ├── main.py               ← FastAPI app + lifespan
│   │   ├── core/                 ← Config, Logger, Security, Rate Limiter
│   │   ├── api/                  ← REST routes + WebSocket
│   │   ├── agents/               ← AlphaGrid (20 agents) + Orchestrator
│   │   ├── strategies/           ← MEV, Yield, Airdrop, Cross-chain
│   │   ├── blockchain/           ← Web3 multi-chain + Flashbots
│   │   ├── execution/            ← DEX executor (Uniswap V3 / PancakeSwap)
│   │   ├── services/             ← Signal, Gas, Profit, Portfolio, Notification
│   │   └── ml/                   ← Feature Eng, GBC/LR Model, RL Agent, Backtest
│   └── tests/
│
├── android/                      ← MODULE 01-12: Kotlin Native Shell
│   └── app/src/main/
│       ├── java/com/lennit/cryptolyzer/
│       │   ├── MainActivity.kt   ← WebView host + LennitBridge JS interface
│       │   ├── CryptolyzerApp.kt ← Hilt application entry
│       │   ├── navigation/       ← Compose NavGraph
│       │   ├── screens/          ← 7 Compose screens
│       │   ├── viewmodel/        ← 7 MVVM ViewModels
│       │   ├── domain/           ← Models + Repository interface
│       │   ├── data/             ← Remote/Mock repos + DTOs + DataStore + WS
│       │   ├── di/               ← Hilt modules
│       │   ├── security/         ← BiometricGatekeeper + KeystoreManager
│       │   ├── service/          ← GuardianService (foreground)
│       │   └── ui/theme/         ← AMOLED Copper/Black theme
│       └── res/                  ← XML resources
│
├── web/                          ← MODULE 18: PWA Dashboard
│   ├── index.html                ← App shell (7 screens)
│   ├── styles.css                ← AMOLED dark theme (optimised for S20 FE)
│   ├── sw.js                     ← Service Worker (cache-first static)
│   ├── manifest.json             ← PWA manifest
│   └── js/
│       ├── app.js                ← Main controller + routing + API
│       ├── charts.js             ← Canvas-based performance charts
│       └── crypto_engine.js      ← Client-side signal processing
│
├── rust-core/                    ← MODULE 19: Native JNI (NDK)
│   ├── Cargo.toml
│   └── src/
│       ├── lib.rs                ← JNI entry points (thread-safe, no panics)
│       ├── npu_accelerator.rs    ← Snapdragon 865 Hexagon DSP stub
│       ├── zk_oracle.rs          ← ZK solvency proof (arkworks TODO)
│       ├── hybrid_sign.rs        ← Quantum-resistant signing stub
│       └── mesh_sync.rs          ← P2P libp2p swarm stub
│
└── infra/                        ← MODULE 20: DevOps
    ├── docker/
    │   ├── docker-compose.yml    ← Backend + Postgres + Redis + Nginx
    │   └── nginx.conf            ← Reverse proxy + TLS + rate limiting
    ├── ci/
    │   └── .github-workflows-deploy.yml
    └── firebase/
        └── firebase.json         ← Firebase Hosting config
```

---

## 20 Modules

| # | Module              | Layer    | Technology                        |
|---|---------------------|----------|-----------------------------------|
| 01| Core Android Shell  | Android  | Kotlin, Hilt, WebView             |
| 02| Dashboard           | PWA+KT   | Compose + JS Canvas Charts        |
| 03| AlphaGrid Agents    | Backend  | asyncio, 20 concurrent agents     |
| 04| Treasury/Vault      | PWA+KT   | Multi-chain portfolio             |
| 05| Strategy Engine     | Backend  | MEV, Yield, Airdrop, Cross-chain  |
| 06| Notifications       | PWA+KT   | Real-time event feed              |
| 07| Settings            | PWA+KT   | DataStore, Bridge sync            |
| 08| Safe Control        | PWA+KT   | Emergency stop / biometric guard  |
| 09| Security            | Android  | BiometricGatekeeper, Keystore TEE |
| 10| Guardian Service    | Android  | Foreground service (AMOLED-safe)  |
| 11| Data Layer          | Android  | Retrofit, OkHttp, DataStore, Room |
| 12| Domain Models       | Android  | Kotlin data classes + enums       |
| 13| API Gateway         | Backend  | FastAPI + CORS + JWT + Rate limit |
| 14| Agent Orchestrator  | Backend  | AlphaGrid 3-role sovereign loop   |
| 15| Strategy Library    | Backend  | MEV arb, yield farm, airdrop hunt |
| 16| Blockchain Engine   | Backend  | Web3.py + Flashbots + DEX exec    |
| 17| ML/AI Engine        | Backend  | GBC/LR + RL Q-table + Backtest    |
| 18| PWA Dashboard       | Web      | Vanilla JS + Canvas + SW          |
| 19| Rust Native Core    | Native   | JNI, NPU, ZK oracle, ML-DSA       |
| 20| Infrastructure      | DevOps   | Docker, Nginx, CI/CD, Firebase    |

---

## Quick Start

### 1. Backend
```bash
cd backend
cp .env.example .env   # Fill in your secrets
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

### 2. PWA (Firebase or local)
```bash
cd web
npx serve .
# Or: firebase deploy --only hosting
```

### 3. Android (Android Studio Giraffe+)
```bash
cd android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Rust JNI (optional native acceleration)
```bash
cd rust-core
rustup target add aarch64-linux-android
cargo build --release --target aarch64-linux-android
cp target/aarch64-linux-android/release/liblennit_genesis_core.so \
   ../android/app/src/main/jniLibs/arm64-v8a/
```

### 5. Docker (VPS full stack)
```bash
cd infra/docker
cp ../../backend/.env.example .env  # Fill secrets
docker-compose up -d
```

---

## Samsung S20 FE Optimisations
- **AMOLED**: Pure black (`#000000`) backgrounds — zero power on off pixels
- **120Hz**: Smooth CSS transitions + native Compose animations
- **ARM64**: Rust compiled with `aarch64-linux-android` target
- **Snapdragon 865**: NPU stub ready for Qualcomm QNN SDK
- **Android 13**: Target SDK 35, foreground service type `dataSync`, `POST_NOTIFICATIONS`
- **Battery**: GuardianService with `PRIORITY_LOW` notification (no wakelock abuse)

---

*LENNIT_SUITE TECHNOLOGIES © 2026 — Sovereign Blockchain Intelligence*

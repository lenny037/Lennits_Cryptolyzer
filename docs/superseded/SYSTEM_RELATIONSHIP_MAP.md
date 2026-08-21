# SYSTEM_RELATIONSHIP_MAP.md
# LENNIT_CRYPTOLYZER — System Topology and Relationship Map
# Generated: Reconstruction Phase 03

---

## LAYER TOPOLOGY

```
╔══════════════════════════════════════════════════════════════════════════╗
║                     LENNIT_CRYPTOLYZER TOPOLOGY                         ║
╚══════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────────┐
│  INTERFACE LAYER                                                          │
│  ┌──────────────────────┐    ┌──────────────────────────────────────┐   │
│  │  Android App          │    │  Web Dashboard (React PWA)            │   │
│  │  (Cryptolyzer App)    │    │  Landing Page (HTML/CSS)              │   │
│  │  Kotlin + Compose     │    │  WebGPU Visualization                 │   │
│  └──────────┬───────────┘    └────────────────┬─────────────────────┘   │
└─────────────┼──────────────────────────────────┼────────────────────────┘
              │ REST + WebSocket                  │ REST + WS
              ▼                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  EXECUTION LAYER (Cloud Functions / FastAPI)                             │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌──────────────────────┐ │
│  │ M01        │ │ M06        │ │ M07        │ │ M02                  │ │
│  │ Orchestr.  │ │ MEV/Arb    │ │ Farming    │ │ Treasury             │ │
│  │ Core       │ │ Engine     │ │ Engine     │ │ Engine               │ │
│  └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └──────────────────────┘ │
│  ┌─────┴──────┐ ┌─────┴──────┐ ┌─────┴──────┐ ┌──────────────────────┐ │
│  │ M03        │ │ M04        │ │ M05        │ │ M12                  │ │
│  │ Airdrop    │ │ Faucet     │ │ Prediction │ │ Security             │ │
│  │ Intel      │ │ Harvester  │ │ Engine     │ │ & Threat             │ │
│  └────────────┘ └────────────┘ └────────────┘ └──────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
              │ Events                            │ Reads/Writes
              ▼                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  EVENT LAYER (Pub/Sub)                                                    │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ M14 Data Pipeline & Event Bus                                       │ │
│  │ Topics: agent.action | vault.update | strategy.signal |            │ │
│  │         airdrop.opportunity | tx.submitted | risk.alert |          │ │
│  │         faucet.claimed | yield.harvested | memory.stored           │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
              │                                   │
              ▼                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  STATE LAYER (Firestore)                                                  │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────────┐ │
│  │ /agents      │ │ /vault       │ │ /strategies  │ │ /notifications │ │
│  │ /agent_runs  │ │ /positions   │ │ /executions  │ │ /alerts        │ │
│  └──────────────┘ └──────────────┘ └──────────────┘ └────────────────┘ │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────────┐ │
│  │ /memories    │ │ /airdrops    │ │ /faucets     │ │ /analytics     │ │
│  │ /embeddings  │ │ /signals     │ │ /claims      │ │ /telemetry     │ │
│  └──────────────┘ └──────────────┘ └──────────────┘ └────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  INTELLIGENCE LAYER (AI Orchestration)                                   │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────────┐ │
│  │ M16 Memory   │ │ M17 Exec     │ │ M09 Social   │ │ M20 Self-Impr. │ │
│  │ (SQLite/vec) │ │ Bridge       │ │ Signals      │ │ Loop           │ │
│  └──────────────┘ └──────────────┘ └──────────────┘ └────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  AUTONOMY LAYER (Cloud Scheduler)                                        │
│  Agent heartbeats | Strategy evaluation | Yield harvest | Airdrop scan  │
│  Faucet claims | Treasury rebalance | Memory consolidation              │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## MODULE DEPENDENCY GRAPH

```
M01 (Orchestration Core)
    ├── depends on: M14 (event bus), M16 (memory), M17 (execution bridge)
    ├── consumed by: ALL modules
    └── owns: agent lifecycle, workflow routing, planning

M02 (Treasury Engine)
    ├── depends on: M08 (blockchain), M07 (farming), M14 (events)
    ├── consumed by: M01 (via agent commands), M18 (mobile API)
    └── owns: vault state, capital allocation, APY tracking

M03 (Airdrop Intelligence)
    ├── depends on: M08 (blockchain), M09 (social), M13 (identity)
    ├── consumed by: M01 (orchestration), M04 (faucet overlap)
    └── owns: airdrop discovery, qualification scoring

M04 (Faucet Harvester)
    ├── depends on: M13 (wallet identity), M08 (blockchain)
    ├── consumed by: M01, M02 (treasury accumulation)
    └── owns: claim schedules, batch execution

M05 (Prediction Engine)
    ├── depends on: M09 (social signals), M08 (blockchain data), M16 (memory)
    ├── consumed by: M06 (MEV timing), M07 (farming entry), M01 (routing)
    └── owns: signal generation, anomaly detection

M06 (MEV & Arbitrage)
    ├── depends on: M08 (blockchain/mempool), M05 (prediction), M12 (risk)
    ├── consumed by: M01 (orchestration), M02 (treasury)
    └── owns: DEX arbitrage, flash loan execution, MEV protection

M07 (DeFi Farming)
    ├── depends on: M08 (blockchain), M05 (prediction), M12 (risk)
    ├── consumed by: M02 (treasury), M01
    └── owns: LP positions, yield harvest, staking

M08 (Blockchain Intelligence)
    ├── depends on: external RPCs (ETH/BASE/SOL/BSC)
    ├── consumed by: M02/M03/M04/M06/M07 (all execution modules)
    └── owns: wallet data, contract intel, whale tracking, bridge routing

M09 (Social Signal Engine)
    ├── depends on: external social APIs
    ├── consumed by: M05 (prediction), M03 (airdrop discovery)
    └── owns: sentiment, narrative detection, virality scoring

M10 (Tokenomics & Rewards)
    ├── depends on: M08 (blockchain), M02 (treasury)
    ├── consumed by: M11 (governance), M04 (faucet rewards)
    └── owns: emission logic, staking infrastructure

M11 (Governance)
    ├── depends on: M08 (blockchain), M10 (tokenomics)
    ├── consumed by: M01 (strategy approval workflows)
    └── owns: proposal systems, voting logic

M12 (Security & Threat)
    ├── depends on: M14 (event monitoring), M08 (blockchain)
    ├── consumed by: M06 (pre-execution), M02 (treasury gate), ALL
    └── owns: risk scoring, anomaly detection, emergency shutdown

M13 (Identity & Profile)
    ├── depends on: M08 (wallet data), M16 (memory)
    ├── consumed by: M03 (airdrop), M04 (faucet), M19 (monetization)
    └── owns: wallet identity, reputation, profile graph

M14 (Data Pipeline & Event Bus)
    ├── depends on: Pub/Sub infrastructure
    ├── consumed by: ALL modules
    └── owns: event routing, stream ingestion, message backbone

M15 (Analytics Engine)
    ├── depends on: ALL modules (reads)
    ├── consumed by: M18 (mobile display), M20 (self-improvement)
    └── owns: dashboards, KPI calculations, telemetry

M16 (Agent Memory)
    ├── depends on: vector embedding service, SQLite/Firestore
    ├── consumed by: M01 (agent context), M05 (prediction history)
    └── owns: semantic memory, relationship graph, checkpoints

M17 (AI Execution Bridge)
    ├── depends on: LLM APIs, M16 (memory)
    ├── consumed by: M01 (orchestration), M20 (self-improvement)
    └── owns: prompt routing, tool execution, MCP protocol

M18 (Mobile Operations)
    ├── depends on: ALL modules via REST/WS API gateway
    ├── consumed by: end user
    └── owns: Android apps, push notifications, biometric auth

M19 (Monetization)
    ├── depends on: M13 (identity), M15 (analytics)
    ├── consumed by: M18 (mobile billing)
    └── owns: subscriptions, premium features, SaaS billing

M20 (Self-Improvement Loop)
    ├── depends on: M15 (analytics), M16 (memory), M17 (execution bridge)
    ├── consumed by: M01 (strategy updates)
    └── owns: RL scoring, feedback loops, strategy evolution
```

---

## EXISTING IMPLEMENTATION STATUS

| Component | Existing Code | Implementation Level |
|-----------|--------------|---------------------|
| FastAPI REST API | routes (2) (1).py | ✅ COMPLETE (mock data) |
| Event Bus | event_bus.py | ⚠️ STUB (in-memory only) |
| State Manager | state_manager.py | ⚠️ STUB (in-memory only) |
| Task Queue | task_queue.py | ⚠️ STUB (deque only) |
| Airdrop Engine | airdrop_engine.py | ⚠️ STUB (score formula only) |
| Faucet Engine | faucet_engine.py | ⚠️ STUB (mock claim) |
| Yield Engine | yield_engine.py | ⚠️ STUB (APY formula) |
| Bridge System | bridge_*.py + multi_chain.py | ⚠️ PARTIAL (Web3 structure) |
| Risk Manager | risk_manager.py | ❌ EMPTY STUB |
| MCP Memory Server | code 3.txt | ✅ COMPLETE (SQLite+vectors) |
| MCP Tool Server | code 4.txt | ✅ COMPLETE (Express+HTTP) |
| Android App | kt/ files | ✅ SUBSTANTIAL (needs wiring) |
| Web Frontend | js/ + html/ | ✅ SUBSTANTIAL (React 19 + shadcn) |
| Landing Page | code 5.txt | ✅ COMPLETE (HTML/CSS) |
| Rust NPU Bridge | rs/ files | ✅ COMPLETE (JNI bridge) |
| Firebase Config | firebase.json | ⚠️ HOSTING ONLY (no Functions) |
| CI/CD | MISSING | ❌ NOT PRESENT |
| Firestore Rules | MISSING | ❌ NOT PRESENT |
| Pub/Sub Topics | MISSING | ❌ NOT PRESENT |
| Secret Manager | MISSING | ❌ NOT PRESENT |

---

## DATA FLOW: AGENT EXECUTION LIFECYCLE

```
SCHEDULER trigger (cron)
    │
    ▼
M01 Orchestration ──reads──► M16 Memory (context)
    │                              │
    │ route decision               │ past lessons
    ▼                              ▼
M17 Execution Bridge ◄────── M05 Prediction Engine
    │                              │
    │ tool call                    │ signal
    ▼                              ▼
M06 MEV / M07 Farming / M03 Airdrop / M04 Faucet
    │
    │ execution event
    ▼
M12 Security Gate ──risk score──► PASS / BLOCK
    │
    │ PASS
    ▼
M08 Blockchain Layer ──tx──► Chain
    │
    │ result
    ▼
M14 Event Bus ──publish──► ALL subscribers
    │
    ▼
M02 Treasury (update position) + M15 Analytics (log) + M16 Memory (store)
    │
    ▼
M18 Mobile (push notification) + M20 Self-Improvement (score)
```

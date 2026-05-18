# CANONICAL_MODULE_MAP.md
# LENNIT_CRYPTOLYZER — Authoritative Module Boundaries
# Generated: Reconstruction Phase 05

---

## MODULE OWNERSHIP MATRIX

| Module | ID | Cloud Functions Path | Firestore Root | Pub/Sub Topics | Scheduler Jobs |
|--------|----|---------------------|----------------|----------------|----------------|
| AI Orchestration Core | M01 | modules/m01-orchestration/ | /agents, /workflows | agent.command, workflow.step | agent-heartbeat |
| Treasury Engine | M02 | modules/m02-treasury/ | /vault, /positions, /rebalances | vault.update, rebalance.trigger | treasury-rebalance |
| Airdrop Intelligence | M03 | modules/m03-airdrop/ | /airdrops, /airdrop_signals | airdrop.discovered, airdrop.qualified | airdrop-scan |
| Faucet Harvester | M04 | modules/m04-faucet/ | /faucets, /claims | faucet.claimed, faucet.failed | faucet-harvest |
| Prediction Engine | M05 | modules/m05-prediction/ | /predictions, /signals | signal.generated, anomaly.detected | prediction-run |
| MEV & Arbitrage | M06 | modules/m06-mev/ | /arbitrage_ops, /mev_events | arb.opportunity, trade.submitted | mev-scan |
| DeFi Farming | M07 | modules/m07-farming/ | /positions, /yields | yield.harvested, lp.updated | farming-harvest |
| Blockchain Intelligence | M08 | modules/m08-blockchain/ | /wallets, /contracts, /txs | tx.confirmed, whale.movement | chain-monitor |
| Social Signal Engine | M09 | modules/m09-social/ | /social_signals, /narratives | social.signal, narrative.shift | social-scan |
| Tokenomics & Rewards | M10 | modules/m10-tokenomics/ | /emissions, /stakes | reward.distributed, stake.updated | reward-emission |
| Governance Engine | M11 | modules/m11-governance/ | /proposals, /votes | proposal.created, vote.cast | governance-scan |
| Security & Threat | M12 | modules/m12-security/ | /risk_events, /threat_log | risk.alert, exploit.detected | security-audit |
| Identity & Profile | M13 | modules/m13-identity/ | /profiles, /reputation | profile.updated, rep.changed | — |
| Data Pipeline | M14 | modules/m14-pipeline/ | /events, /streams | (all topics routed here) | pipeline-flush |
| Analytics Engine | M15 | modules/m15-analytics/ | /analytics, /kpis, /telemetry | telemetry.batch | analytics-aggregate |
| Agent Memory | M16 | modules/m16-memory/ | /memories, /relationships | memory.stored, context.retrieved | memory-consolidate |
| AI Execution Bridge | M17 | modules/m17-execution/ | /executions, /tool_calls | execution.requested, tool.result | — |
| Mobile Operations | M18 | modules/m18-mobile/ | (reads all) | notification.push | — |
| Monetization Engine | M19 | modules/m19-monetization/ | /subscriptions, /billing | subscription.event | billing-check |
| Self-Improvement Loop | M20 | modules/m20-self-improvement/ | /feedback, /scores | improvement.cycle | evolution-run |

---

## CANONICAL FILE STRUCTURE

```
lennit_cryptolyzer/
│
├── functions/                           # Firebase Cloud Functions (TypeScript)
│   ├── src/
│   │   ├── core/                        # Cross-cutting infrastructure
│   │   │   ├── logger/
│   │   │   │   └── index.ts             # Structured logging (Cloud Logging)
│   │   │   ├── config/
│   │   │   │   └── index.ts             # Environment config + Secret Manager
│   │   │   ├── validation/
│   │   │   │   └── index.ts             # Zod schema validation
│   │   │   └── observability/
│   │   │       └── index.ts             # Telemetry + tracing
│   │   │
│   │   ├── shared/
│   │   │   ├── types/
│   │   │   │   └── index.ts             # Global TypeScript types
│   │   │   ├── schemas/
│   │   │   │   └── index.ts             # Firestore document schemas
│   │   │   └── events/
│   │   │       └── index.ts             # Pub/Sub event contracts
│   │   │
│   │   └── modules/
│   │       ├── m01-orchestration/
│   │       │   ├── index.ts             # Agent lifecycle manager
│   │       │   ├── bargaining.ts        # HiMAP resource bargaining protocol
│   │       │   ├── router.ts            # Task routing logic
│   │       │   └── types.ts
│   │       ├── m02-treasury/
│   │       │   ├── index.ts             # Vault + capital management
│   │       │   ├── rebalancer.ts        # Allocation rebalancer
│   │       │   ├── apy-optimizer.ts     # APY tracking
│   │       │   └── types.ts
│   │       ├── m03-airdrop/
│   │       │   ├── index.ts             # Airdrop scanner
│   │       │   ├── scorer.ts            # Qualification scoring
│   │       │   ├── qualifier.ts         # Eligibility analysis
│   │       │   └── types.ts
│   │       ├── m04-faucet/
│   │       │   ├── index.ts             # Faucet claim orchestrator
│   │       │   ├── scheduler.ts         # Claim timing
│   │       │   ├── batcher.ts           # Batch claim logic
│   │       │   └── types.ts
│   │       ├── m05-prediction/
│   │       │   ├── index.ts             # Signal generator
│   │       │   ├── anomaly.ts           # Anomaly detection
│   │       │   ├── forecaster.ts        # Price forecasting
│   │       │   └── types.ts
│   │       ├── m06-mev/
│   │       │   ├── index.ts             # MEV + arbitrage engine
│   │       │   ├── arbitrage.ts         # DEX arbitrage
│   │       │   ├── simulator.ts         # SIMULATION-FIRST execution
│   │       │   ├── executor.ts          # Live execution (simulation-gated)
│   │       │   └── types.ts
│   │       ├── m07-farming/
│   │       │   ├── index.ts             # Yield farming orchestrator
│   │       │   ├── lp-optimizer.ts      # LP optimization
│   │       │   ├── harvester.ts         # Yield harvest
│   │       │   ├── staking.ts           # Staking management
│   │       │   └── types.ts
│   │       ├── m08-blockchain/
│   │       │   ├── index.ts             # Multi-chain client
│   │       │   ├── bridge-router.ts     # Bridge selection (Stargate, Across)
│   │       │   ├── wallet-tracker.ts    # Wallet + whale monitoring
│   │       │   ├── contract-intel.ts    # Contract analysis
│   │       │   └── types.ts
│   │       ├── m09-social/
│   │       │   ├── index.ts             # Social signal aggregator
│   │       │   ├── sentiment.ts         # Sentiment analysis
│   │       │   ├── narrative.ts         # Narrative detection
│   │       │   └── types.ts
│   │       ├── m10-tokenomics/
│   │       │   ├── index.ts             # Token economics
│   │       │   ├── emission.ts          # Token emission
│   │       │   └── types.ts
│   │       ├── m11-governance/
│   │       │   ├── index.ts             # Governance scanner
│   │       │   ├── proposals.ts         # Proposal tracker
│   │       │   └── types.ts
│   │       ├── m12-security/
│   │       │   ├── index.ts             # Security + threat engine
│   │       │   ├── risk-scorer.ts       # Risk scoring (BLOCKS M06/M07)
│   │       │   ├── exploit-monitor.ts   # Exploit detection
│   │       │   ├── emergency.ts         # SAFE_MODE / SHUTDOWN logic
│   │       │   └── types.ts
│   │       ├── m13-identity/
│   │       │   ├── index.ts             # Identity management
│   │       │   ├── reputation.ts        # Reputation scoring
│   │       │   └── types.ts
│   │       ├── m14-pipeline/
│   │       │   ├── index.ts             # Event bus orchestrator
│   │       │   ├── pubsub-router.ts     # Topic routing
│   │       │   └── types.ts
│   │       ├── m15-analytics/
│   │       │   ├── index.ts             # Analytics aggregator
│   │       │   ├── kpi.ts               # KPI calculations
│   │       │   ├── telemetry.ts         # Structured telemetry
│   │       │   └── types.ts
│   │       ├── m16-memory/
│   │       │   ├── index.ts             # Memory server (MCP-compatible)
│   │       │   ├── store.ts             # Semantic storage
│   │       │   ├── retrieval.ts         # Vector similarity retrieval
│   │       │   ├── relationships.ts     # Memory relationship graph
│   │       │   └── types.ts
│   │       ├── m17-execution/
│   │       │   ├── index.ts             # AI execution bridge
│   │       │   ├── mcp-server.ts        # MCP tool server
│   │       │   ├── prompt-router.ts     # Prompt routing
│   │       │   └── types.ts
│   │       ├── m18-mobile/
│   │       │   ├── index.ts             # Mobile API gateway
│   │       │   ├── api-gateway.ts       # All REST endpoints
│   │       │   ├── websocket.ts         # WS metrics stream
│   │       │   ├── notifications.ts     # Push notification dispatch
│   │       │   └── types.ts
│   │       ├── m19-monetization/
│   │       │   ├── index.ts             # Monetization engine
│   │       │   ├── subscriptions.ts     # Subscription management
│   │       │   └── types.ts
│   │       └── m20-self-improvement/
│   │           ├── index.ts             # Self-improvement loop
│   │           ├── rl-scorer.ts         # Reinforcement scoring
│   │           ├── evolution.ts         # Strategy evolution
│   │           └── types.ts
│   │
│   ├── package.json                     # Functions dependencies
│   ├── tsconfig.json                    # TypeScript config (strict)
│   └── index.ts                         # Function exports (entry point)
│
├── infra/
│   ├── firestore/
│   │   ├── firestore.rules              # Security rules
│   │   └── firestore.indexes.json      # Index definitions
│   ├── pubsub/
│   │   └── topics.json                  # Topic registry
│   ├── scheduler/
│   │   └── jobs.json                    # Scheduler job definitions
│   └── secrets/
│       └── secret-registry.md           # Secret names (no values)
│
├── android/
│   ├── cryptolyzer-app/                 # Main autonomous agent dashboard
│   └── lennit-suite-mcp/               # MCP landing page client
│
├── web/
│   ├── dashboard/                       # React 19 + shadcn/ui dashboard
│   └── landing/                         # HTML/CSS landing page
│
├── rust-core/                           # Native Rust NPU bridge
│   ├── src/
│   │   ├── lib.rs                       # JNI bridge entry
│   │   ├── brain/
│   │   │   ├── mod.rs
│   │   │   └── npu_accelerator.rs       # Snapdragon 865 Hexagon bridge
│   │   ├── crypto/
│   │   │   └── hybrid_sign.rs           # Post-quantum signing (ML-DSA)
│   │   └── society/
│   │       └── mesh_sync.rs             # P2P swarm bootstrap
│   └── Cargo.toml
│
├── docs/
│   ├── inventory/                       # Phase 01-04 outputs
│   ├── architecture/                    # System design docs
│   ├── api/                             # API contracts
│   ├── events/                          # Event schema contracts
│   └── deployment/                      # Deployment runbooks
│
├── scripts/
│   ├── bootstrap-firebase.sh            # Firebase init script
│   ├── deploy.sh                        # Deployment script
│   └── seed-firestore.ts               # Firestore seed data
│
├── .github/
│   └── workflows/
│       ├── ci.yml                       # CI pipeline
│       └── deploy.yml                   # CD pipeline
│
├── firebase.json                        # Firebase hosting + functions config
├── .firebaserc                          # Firebase project config
└── README.md                            # Platform overview
```

---

## MODULE INTERFACE CONTRACTS

Each module exposes ONLY these interface types:

1. **HTTP Functions** — callable via Firebase Functions HTTPS triggers
2. **Pub/Sub Functions** — triggered by topic messages
3. **Scheduled Functions** — triggered by Cloud Scheduler
4. **Typed Events** — emitted to Pub/Sub, never directly mutate other modules
5. **Firestore writes** — only to own collection root

**Cross-module reads** are permitted via Firestore.
**Cross-module writes** MUST go through Pub/Sub events.
**No direct function-to-function calls** (except orchestration M01).

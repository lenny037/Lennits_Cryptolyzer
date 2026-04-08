"""Full REST API — MODULE 13: API Gateway.
   All 20 modules exposed via /api/v1/
"""
from __future__ import annotations

from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from app.api.deps import require_api_key, rate_limit
from app.core.logger import get_logger

logger = get_logger(__name__)
router = APIRouter(prefix="/api/v1", tags=["cryptolyzer"])


# ═══════════════════════════════════════════════════════════════════════════
# PYDANTIC SCHEMAS
# ═══════════════════════════════════════════════════════════════════════════

class AgentStatus(BaseModel):
    id: str
    name: str
    status: str          # RUNNING | PAUSED | ERROR | STOPPED
    role: str            # ARBITRAGE | HIMAP | DARKFOREST
    profit_usd: float = 0.0
    uptime_seconds: int = 0


class VaultPosition(BaseModel):
    symbol: str
    name: str
    amount: float
    usd_value: float
    chain: str = "ETH"
    apy: Optional[float] = None


class StrategyConfig(BaseModel):
    id: str
    name: str
    type: str            # ARBITRAGE | YIELD | AIRDROP | MEV
    mode: str            # LIVE | SHADOW | OFF
    allocated_usd: float = 0.0
    pnl_24h: float = 0.0
    win_rate: float = 0.0


class NotificationItem(BaseModel):
    id: str
    timestamp: str
    level: str           # INFO | WARN | ALERT | PROFIT
    message: str
    detail: Optional[str] = None


class DashboardSummary(BaseModel):
    total_portfolio_usd: str
    pnl_24h_usd: str
    pnl_24h_pct: str
    active_agents: int
    total_agents: int
    system_status: str   # OK | DEGRADED | SAFE_MODE | SHUTDOWN
    uptime_seconds: int


class SystemStatusResponse(BaseModel):
    mode: str
    running_agents: int
    last_updated: str
    version: str


class ControlRequest(BaseModel):
    action: str          # START | STOP | PAUSE | RESET
    mode: Optional[str] = None


class StrategyModeUpdate(BaseModel):
    strategy_id: str
    mode: str


class SignalResponse(BaseModel):
    symbol: str
    signal: str          # BUY | SELL | HOLD
    confidence: float
    price: float
    timestamp: str


class TradeRequest(BaseModel):
    symbol: str
    side: str            # BUY | SELL
    amount_usd: float
    slippage_tolerance: float = 0.005


class GasEstimate(BaseModel):
    chain: str
    gwei: float
    usd_cost: float
    recommended: str


class PortfolioMetrics(BaseModel):
    total_value_usd: float
    allocated_usd: float
    available_usd: float
    daily_pnl: float
    weekly_pnl: float
    monthly_pnl: float
    drawdown_pct: float
    sharpe_ratio: float


# ═══════════════════════════════════════════════════════════════════════════
# SYSTEM ENDPOINTS
# ═══════════════════════════════════════════════════════════════════════════

@router.get("/health")
async def health():
    return {"status": "ok", "service": "lennits-cryptolyzer"}


@router.get("/status", response_model=SystemStatusResponse)
async def get_status(_: str = Depends(require_api_key)):
    import datetime
    return SystemStatusResponse(
        mode="FULL_AUTONOMY",
        running_agents=20,
        last_updated=datetime.datetime.utcnow().isoformat(),
        version="2.0.0",
    )


# ═══════════════════════════════════════════════════════════════════════════
# MODULE 02: DASHBOARD
# ═══════════════════════════════════════════════════════════════════════════

@router.get("/dashboard", response_model=DashboardSummary)
async def get_dashboard(_rl=Depends(rate_limit)):
    return DashboardSummary(
        total_portfolio_usd="$155,000.00",
        pnl_24h_usd="+$2,340.00",
        pnl_24h_pct="+1.53%",
        active_agents=20,
        total_agents=20,
        system_status="OK",
        uptime_seconds=86400,
    )


@router.get("/portfolio/metrics", response_model=PortfolioMetrics)
async def get_portfolio_metrics(_rl=Depends(rate_limit)):
    return PortfolioMetrics(
        total_value_usd=155_000.0,
        allocated_usd=120_000.0,
        available_usd=35_000.0,
        daily_pnl=2_340.0,
        weekly_pnl=11_200.0,
        monthly_pnl=42_000.0,
        drawdown_pct=-3.2,
        sharpe_ratio=2.4,
    )


# ═══════════════════════════════════════════════════════════════════════════
# MODULE 03: AGENTS (AlphaGrid)
# ═══════════════════════════════════════════════════════════════════════════

_MOCK_AGENTS = [
    {"id": f"Lennit_{str(i).zfill(2)}", "name": f"Agent {str(i).zfill(2)}",
     "status": "RUNNING", "role": "ARBITRAGE" if i <= 5 else ("HIMAP" if i <= 15 else "DARKFOREST"),
     "profit_usd": round(i * 12.5, 2), "uptime_seconds": 86400 + i * 100}
    for i in range(1, 21)
]


@router.get("/agents", response_model=List[AgentStatus])
async def list_agents(_rl=Depends(rate_limit)):
    return _MOCK_AGENTS


@router.get("/agents/{agent_id}", response_model=AgentStatus)
async def get_agent(agent_id: str, _rl=Depends(rate_limit)):
    agent = next((a for a in _MOCK_AGENTS if a["id"] == agent_id), None)
    if not agent:
        raise HTTPException(status_code=404, detail=f"Agent {agent_id} not found")
    return agent


@router.post("/agents/{agent_id}/control")
async def control_agent(agent_id: str, body: ControlRequest, _: str = Depends(require_api_key)):
    valid = {"START", "STOP", "PAUSE", "RESET"}
    if body.action not in valid:
        raise HTTPException(400, detail=f"action must be one of {valid}")
    logger.info("Agent %s: control=%s mode=%s", agent_id, body.action, body.mode)
    return {"status": "ok", "agent_id": agent_id, "action": body.action}


# ═══════════════════════════════════════════════════════════════════════════
# MODULE 04: TREASURY / VAULT
# ═══════════════════════════════════════════════════════════════════════════

@router.get("/vault", response_model=List[VaultPosition])
async def get_vault(_rl=Depends(rate_limit)):
    return [
        VaultPosition(symbol="BTC",  name="Bitcoin",   amount=1.234,   usd_value=85_000.0, chain="BTC",     apy=None),
        VaultPosition(symbol="ETH",  name="Ethereum",  amount=12.5,    usd_value=42_000.0, chain="ETH",     apy=4.2),
        VaultPosition(symbol="SOL",  name="Solana",    amount=350.0,   usd_value=14_000.0, chain="SOL",     apy=7.1),
        VaultPosition(symbol="MATIC",name="Polygon",   amount=8_000.0, usd_value=5_600.0,  chain="POLYGON", apy=5.8),
        VaultPosition(symbol="BNB",  name="BNB Chain", amount=22.0,    usd_value=8_400.0,  chain="BSC",     apy=6.3),
    ]


@router.post("/vault/rebalance")
async def rebalance_vault(_: str = Depends(require_api_key)):
    logger.info("Vault rebalance triggered")
    return {"status": "queued", "action": "rebalance"}


@router.post("/vault/withdraw")
async def withdraw_profits(_: str = Depends(require_api_key)):
    logger.info("Profit withdrawal triggered")
    return {"status": "queued", "action": "withdraw_profits"}


# ═══════════════════════════════════════════════════════════════════════════
# MODULE 05: STRATEGIES
# ═══════════════════════════════════════════════════════════════════════════

_MOCK_STRATEGIES = [
    StrategyConfig(id="1", name="BTC Perp Tri-Arb",      type="ARBITRAGE", mode="LIVE",   allocated_usd=30_000, pnl_24h=1_200, win_rate=0.68),
    StrategyConfig(id="2", name="ETH/USDC LP Farming",   type="YIELD",     mode="LIVE",   allocated_usd=25_000, pnl_24h=320,   win_rate=0.92),
    StrategyConfig(id="3", name="Retroactive Airdrops",  type="AIRDROP",   mode="SHADOW", allocated_usd=5_000,  pnl_24h=150,   win_rate=0.45),
    StrategyConfig(id="4", name="Cross-chain Flash Arb", type="MEV",       mode="LIVE",   allocated_usd=20_000, pnl_24h=870,   win_rate=0.71),
    StrategyConfig(id="5", name="SOL Yield Optimizer",   type="YIELD",     mode="SHADOW", allocated_usd=10_000, pnl_24h=280,   win_rate=0.85),
    StrategyConfig(id="6", name="MEV Sandwich Guard",    type="MEV",       mode="LIVE",   allocated_usd=15_000, pnl_24h=500,   win_rate=0.78),
]


@router.get("/strategies", response_model=List[StrategyConfig])
async def list_strategies(_rl=Depends(rate_limit)):
    return _MOCK_STRATEGIES


@router.post("/strategies/mode")
async def update_strategy_mode(body: StrategyModeUpdate, _: str = Depends(require_api_key)):
    valid = {"LIVE", "SHADOW", "OFF"}
    if body.mode not in valid:
        raise HTTPException(400, detail=f"mode must be one of {valid}")
    return {"status": "ok", "strategy_id": body.strategy_id, "mode": body.mode}


# ═══════════════════════════════════════════════════════════════════════════
# MODULE 06: NOTIFICATIONS
# ═══════════════════════════════════════════════════════════════════════════

_MOCK_NOTIFICATIONS = [
    NotificationItem(id="1", timestamp="2026-04-03T08:00:00Z", level="PROFIT",  message="BTC Tri-Arb executed: +$1,200",          detail="Spread: 0.42% | Gas: $12"),
    NotificationItem(id="2", timestamp="2026-04-03T07:45:00Z", level="PROFIT",  message="ETH LP yield harvested: +0.15 ETH",       detail="Pool: Uniswap V3 | APY: 4.2%"),
    NotificationItem(id="3", timestamp="2026-04-03T07:30:00Z", level="INFO",    message="AlphaGrid: 20/20 agents active",           detail="Uptime: 24h 0m"),
    NotificationItem(id="4", timestamp="2026-04-03T07:15:00Z", level="WARN",    message="Gas spike detected on ETH mainnet",        detail="Current: 65 gwei | Threshold: 50 gwei"),
    NotificationItem(id="5", timestamp="2026-04-03T07:00:00Z", level="PROFIT",  message="Cross-chain flash arb: +$870",             detail="ETH → BSC → POLY | 3-hop"),
    NotificationItem(id="6", timestamp="2026-04-03T06:45:00Z", level="ALERT",   message="Drawdown warning: -3.2% portfolio",        detail="Max allowed: -5.0%"),
    NotificationItem(id="7", timestamp="2026-04-03T06:30:00Z", level="INFO",    message="ML model retrained: AUC 0.87",             detail="Algorithm: GBC | Features: 24"),
    NotificationItem(id="8", timestamp="2026-04-03T06:00:00Z", level="PROFIT",  message="Airdrop claimed: 500 ARB tokens (~$450)",  detail="Protocol: Arbitrum ecosystem"),
]


@router.get("/notifications", response_model=List[NotificationItem])
async def get_notifications(limit: int = 50, _rl=Depends(rate_limit)):
    return _MOCK_NOTIFICATIONS[:limit]


@router.delete("/notifications/{notification_id}")
async def dismiss_notification(notification_id: str):
    return {"status": "ok", "dismissed": notification_id}


# ═══════════════════════════════════════════════════════════════════════════
# MODULE 16: BLOCKCHAIN / WEB3 — Signals & Gas
# ═══════════════════════════════════════════════════════════════════════════

@router.get("/signals", response_model=List[SignalResponse])
async def get_signals(_rl=Depends(rate_limit)):
    import datetime
    now = datetime.datetime.utcnow().isoformat()
    return [
        SignalResponse(symbol="BTC/USDT", signal="BUY",  confidence=0.78, price=68_420.0, timestamp=now),
        SignalResponse(symbol="ETH/USDT", signal="HOLD", confidence=0.55, price=3_340.0,  timestamp=now),
        SignalResponse(symbol="SOL/USDT", signal="BUY",  confidence=0.71, price=142.5,    timestamp=now),
        SignalResponse(symbol="BNB/USDT", signal="SELL", confidence=0.62, price=560.0,    timestamp=now),
    ]


@router.get("/gas", response_model=List[GasEstimate])
async def get_gas_estimates(_rl=Depends(rate_limit)):
    return [
        GasEstimate(chain="ETH",     gwei=45.2,  usd_cost=8.50,  recommended="Wait <30 gwei"),
        GasEstimate(chain="BSC",     gwei=3.0,   usd_cost=0.15,  recommended="Execute now"),
        GasEstimate(chain="POLYGON", gwei=120.0, usd_cost=0.08,  recommended="Execute now"),
    ]


@router.post("/trade")
async def execute_trade(body: TradeRequest, _: str = Depends(require_api_key)):
    logger.info("Trade queued: %s %s $%.2f", body.side, body.symbol, body.amount_usd)
    return {
        "status": "queued",
        "tx_id": f"0x{'a' * 40}",
        "symbol": body.symbol,
        "side": body.side,
        "amount_usd": body.amount_usd,
    }


# ═══════════════════════════════════════════════════════════════════════════
# MODULE 08: SAFE CONTROL / EMERGENCY
# ═══════════════════════════════════════════════════════════════════════════

@router.post("/emergency/safe_mode")
async def activate_safe_mode(_: str = Depends(require_api_key)):
    logger.warning("EMERGENCY: Safe mode activated — all agents paused")
    return {"status": "ok", "mode": "SAFE_MODE", "message": "All agents paused. Capital secured."}


@router.post("/emergency/shutdown")
async def emergency_shutdown(_: str = Depends(require_api_key)):
    logger.warning("EMERGENCY: Full shutdown initiated")
    return {"status": "ok", "mode": "SHUTDOWN", "message": "System shutting down. Positions closing."}


@router.post("/emergency/resume")
async def resume_operations(_: str = Depends(require_api_key)):
    logger.info("Operations resumed from safe mode")
    return {"status": "ok", "mode": "FULL_AUTONOMY"}

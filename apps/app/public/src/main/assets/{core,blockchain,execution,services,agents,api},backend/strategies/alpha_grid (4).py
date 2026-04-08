"""MODULE 14: AlphaGrid — 20-Agent Sovereign Collective.

Agent Roles:
  01-05  → Arbitrage Hunters  (high-frequency spread scanning)
  06-15  → HiMAP Negotiators  (cross-chain liquidity routing)
  16-20  → Dark-Forest Guards (MEV protection + ZK-proof generation)
"""
from __future__ import annotations

import asyncio
import random
from dataclasses import dataclass, field
from typing import List, Optional

from app.core.config import settings
from app.core.logger import get_logger

logger = get_logger(__name__)


@dataclass
class AgentMetrics:
    agent_id: str
    role: str
    profit_usd: float = 0.0
    trades_executed: int = 0
    uptime_seconds: int = 0
    last_action: str = "idle"
    errors: int = 0


class AlphaGrid:
    """Manages 20 concurrent sovereign agents across 3 specialized roles."""

    def __init__(self) -> None:
        self.agents = [f"Lennit_{str(i).zfill(2)}" for i in range(1, 21)]
        self._running = False
        self._tasks: List[asyncio.Task] = []
        self._metrics: dict[str, AgentMetrics] = {}
        self._init_metrics()
        logger.info("AlphaGrid initialized — %d agents ready", len(self.agents))

    def _init_metrics(self) -> None:
        for i, agent in enumerate(self.agents):
            role = "ARBITRAGE" if i < 5 else ("HIMAP" if i < 15 else "DARKFOREST")
            self._metrics[agent] = AgentMetrics(agent_id=agent, role=role)

    # ── Agent Coroutines ───────────────────────────────────────────────────

    async def _arbitrage_hunter(self, agent_id: str) -> None:
        """Agents 01-05: High-frequency CEX/DEX spread scanning."""
        m = self._metrics[agent_id]
        while self._running:
            try:
                interval = random.uniform(
                    max(0.1, settings.poll_interval_seconds - 0.5),
                    settings.poll_interval_seconds + 0.5,
                )
                await asyncio.sleep(interval)

                # Simulate spread detection & execution
                spread = random.uniform(0.0, 1.5)
                if spread > settings.deviation_threshold / 10:
                    profit = round(spread * random.uniform(50, 500), 2)
                    m.profit_usd += profit
                    m.trades_executed += 1
                    m.last_action = f"arb_executed:+${profit}"
                    logger.debug("%s [ARB] spread=%.3f%% profit=$%.2f", agent_id, spread, profit)
                else:
                    m.last_action = "scanning_spreads"
                    logger.debug("%s [ARB] scanning — spread=%.3f%% (below threshold)", agent_id, spread)

                m.uptime_seconds += int(interval)

            except asyncio.CancelledError:
                logger.info("%s [ARB] clean cancel", agent_id)
                return
            except Exception:
                m.errors += 1
                logger.exception("%s [ARB] unhandled error (total=%d)", agent_id, m.errors)

    async def _himap_negotiator(self, agent_id: str) -> None:
        """Agents 06-15: Cross-chain liquidity routing and yield bargaining."""
        m = self._metrics[agent_id]
        while self._running:
            try:
                await asyncio.sleep(settings.poll_interval_seconds * 1.5)

                # Simulate yield opportunity analysis
                yield_rate = random.uniform(0.0, 12.0)
                if yield_rate > 4.0:
                    profit = round(yield_rate * random.uniform(10, 100), 2)
                    m.profit_usd += profit
                    m.trades_executed += 1
                    m.last_action = f"liquidity_deployed:apy={yield_rate:.1f}%"
                    logger.debug("%s [HiMAP] deployed liquidity APY=%.2f%%", agent_id, yield_rate)
                else:
                    m.last_action = "routing_cross_chain"
                    logger.debug("%s [HiMAP] routing — APY=%.2f%% (scanning)", agent_id, yield_rate)

                m.uptime_seconds += int(settings.poll_interval_seconds * 1.5)

            except asyncio.CancelledError:
                logger.info("%s [HiMAP] clean cancel", agent_id)
                return
            except Exception:
                m.errors += 1
                logger.exception("%s [HiMAP] unhandled error", agent_id)

    async def _darkforest_guard(self, agent_id: str) -> None:
        """Agents 16-20: MEV protection, sandwich attack detection, ZK generation."""
        m = self._metrics[agent_id]
        while self._running:
            try:
                await asyncio.sleep(1.0)

                # Simulate mempool scanning for sandwich attacks
                threat_level = random.uniform(0.0, 1.0)
                if threat_level > 0.7:
                    m.last_action = f"mev_blocked:threat={threat_level:.2f}"
                    logger.debug("%s [GUARD] MEV threat blocked level=%.2f", agent_id, threat_level)
                else:
                    m.last_action = "mempool_clear"
                    logger.debug("%s [GUARD] mempool monitoring — threat=%.2f", agent_id, threat_level)

                m.uptime_seconds += 1

            except asyncio.CancelledError:
                logger.info("%s [GUARD] clean cancel", agent_id)
                return
            except Exception:
                m.errors += 1
                logger.exception("%s [GUARD] unhandled error", agent_id)

    # ── Lifecycle ──────────────────────────────────────────────────────────

    async def ignite_collective(self) -> None:
        """Launch all 20 agents concurrently."""
        self._running = True
        logger.info("AlphaGrid: igniting sovereign bargaining loop — %d agents", len(self.agents))

        self._tasks = []
        for i, agent in enumerate(self.agents):
            if i < 5:
                coro = self._arbitrage_hunter(agent)
            elif i < 15:
                coro = self._himap_negotiator(agent)
            else:
                coro = self._darkforest_guard(agent)
            self._tasks.append(asyncio.create_task(coro, name=agent))

        await asyncio.gather(*self._tasks, return_exceptions=True)

    def halt_collective(self) -> None:
        """Cancel all agent tasks immediately — no hanging tasks."""
        logger.info("AlphaGrid: halt signal — cancelling %d tasks", len(self._tasks))
        self._running = False
        for task in self._tasks:
            if not task.done():
                task.cancel()

    def get_metrics(self) -> list[dict]:
        return [
            {
                "id": m.agent_id,
                "role": m.role,
                "profit_usd": round(m.profit_usd, 2),
                "trades": m.trades_executed,
                "uptime": m.uptime_seconds,
                "last_action": m.last_action,
                "errors": m.errors,
            }
            for m in self._metrics.values()
        ]

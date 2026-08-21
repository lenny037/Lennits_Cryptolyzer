"""MODULE 14: Orchestrator — Master control loop."""
from __future__ import annotations

import asyncio

from app.agents.alpha_grid import AlphaGrid
from app.agents.execution_agent import ExecutionAgent
from app.core.logger import get_logger
from app.services.signal_service import SignalService
from app.services.profit_service import ProfitService

logger = get_logger(__name__)


class Orchestrator:
    def __init__(self) -> None:
        self.alpha_grid = AlphaGrid()
        self.execution_agent = ExecutionAgent()
        self.signal_service = SignalService()
        self.profit_service = ProfitService()
        self._shutdown_event = asyncio.Event()

    async def run(self) -> None:
        """Main event loop — runs until shutdown() is called."""
        logger.info("Orchestrator: sovereign bargaining loop starting")
        try:
            await asyncio.gather(
                self.alpha_grid.ignite_collective(),
                self._profit_monitor(),
                return_exceptions=True,
            )
        except asyncio.CancelledError:
            logger.info("Orchestrator: cancelled")
        finally:
            logger.info("Orchestrator: clean exit")

    async def _profit_monitor(self) -> None:
        """Log aggregate profit every 30 seconds."""
        while not self._shutdown_event.is_set():
            try:
                await asyncio.sleep(30.0)
                metrics = self.alpha_grid.get_metrics()
                total_profit = sum(m["profit_usd"] for m in metrics)
                total_trades = sum(m["trades"] for m in metrics)
                logger.info(
                    "Orchestrator: aggregate profit=$%.2f trades=%d",
                    total_profit, total_trades,
                )
            except asyncio.CancelledError:
                return

    def shutdown(self) -> None:
        self._shutdown_event.set()
        self.alpha_grid.halt_collective()
        logger.info("Orchestrator: shutdown complete")

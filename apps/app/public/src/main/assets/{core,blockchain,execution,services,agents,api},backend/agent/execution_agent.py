"""MODULE 14: Execution Agent — validates and executes signals."""
from __future__ import annotations

from app.core.config import settings
from app.core.logger import get_logger
from app.services.signal_service import SignalService
from app.services.trade_validator import TradeValidator

logger = get_logger(__name__)


class ExecutionAgent:
    def __init__(self) -> None:
        self.signal_service = SignalService()
        self.validator = TradeValidator()
        self._active = False

    async def run_cycle(self) -> None:
        """One execution cycle: fetch signal → validate → execute."""
        self._active = True
        sig = await self.signal_service.get_latest_signal("BTC/USDT")
        if sig is None:
            return

        input_amount = sig.get("input_amount", 0.0)
        output_amount = sig.get("output_amount", 0.0)

        # FIX: Previously hardcoded validate(1, 1.02) — now uses real signal values
        if self.validator.validate(input_amount, output_amount):
            logger.info(
                "ExecutionAgent: signal validated (%s → %s) — executing",
                input_amount, output_amount,
            )
            # TODO: Wire to real DEX executor
        else:
            logger.debug(
                "ExecutionAgent: signal rejected (in=%s out=%s threshold=%.1f%%)",
                input_amount, output_amount, settings.deviation_threshold,
            )
        self._active = False

    @property
    def is_active(self) -> bool:
        return self._active

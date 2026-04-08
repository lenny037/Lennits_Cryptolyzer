"""MODULE 16: Trade Validator — pre-trade safety checks."""
from __future__ import annotations

from app.core.config import settings
from app.core.logger import get_logger

logger = get_logger(__name__)


class TradeValidator:
    def validate(self, input_amount: float, output_amount: float) -> bool:
        if input_amount <= 0 or output_amount <= 0:
            logger.debug("TradeValidator: rejected — zero amount (in=%.6f out=%.6f)", input_amount, output_amount)
            return False
        deviation = ((output_amount - input_amount) / input_amount) * 100
        ok = deviation >= settings.deviation_threshold / 10
        logger.debug(
            "TradeValidator: deviation=%.3f%% threshold=%.3f%% -> %s",
            deviation, settings.deviation_threshold / 10, "OK" if ok else "REJECT"
        )
        return ok

    def check_slippage(self, expected: float, actual: float, max_slippage: float = 0.005) -> bool:
        if expected <= 0:
            return False
        slippage = abs(actual - expected) / expected
        return slippage <= max_slippage

    def check_position_size(self, usd_size: float) -> bool:
        return 0 < usd_size <= settings.max_position_size_usd

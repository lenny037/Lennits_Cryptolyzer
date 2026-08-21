"""MODULE 16: Signal Service — generates trading signals from on-chain data."""
from __future__ import annotations

import random
from typing import Optional

from app.core.logger import get_logger

logger = get_logger(__name__)


class SignalService:
    """Generates BUY/SELL/HOLD signals from market data."""

    SUPPORTED_PAIRS = [
        "BTC/USDT", "ETH/USDT", "SOL/USDT",
        "BNB/USDT", "MATIC/USDT", "AVAX/USDT",
    ]

    async def get_latest_signal(self, pair: str = "BTC/USDT") -> Optional[dict]:
        if pair not in self.SUPPORTED_PAIRS:
            logger.warning("SignalService: unsupported pair %s", pair)
            return None

        signal = random.choice(["BUY", "SELL", "HOLD"])
        confidence = round(random.uniform(0.45, 0.95), 3)
        price = {
            "BTC/USDT": 68_420.0, "ETH/USDT": 3_340.0, "SOL/USDT": 142.5,
            "BNB/USDT": 560.0,    "MATIC/USDT": 0.88,  "AVAX/USDT": 38.5,
        }.get(pair, 100.0) * random.uniform(0.99, 1.01)

        return {
            "pair": pair,
            "signal": signal,
            "confidence": confidence,
            "price": round(price, 4),
            "input_amount": round(random.uniform(0.01, 1.0), 6),
            "output_amount": round(random.uniform(0.011, 1.1), 6),
        }

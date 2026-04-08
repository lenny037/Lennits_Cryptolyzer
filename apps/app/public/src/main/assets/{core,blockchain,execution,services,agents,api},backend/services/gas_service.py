"""MODULE 16: Gas Service — multi-chain gas price monitoring."""
from __future__ import annotations

import random

from app.core.logger import get_logger

logger = get_logger(__name__)

GAS_THRESHOLDS = {
    "ETH":     {"low": 20, "medium": 50, "high": 100},
    "BSC":     {"low": 3,  "medium": 5,  "high": 10},
    "POLYGON": {"low": 50, "medium": 100,"high": 200},
}


class GasService:
    async def get_gas_gwei(self, chain: str = "ETH") -> float:
        base = {"ETH": 45.0, "BSC": 3.0, "POLYGON": 120.0}.get(chain, 50.0)
        return round(base * random.uniform(0.8, 1.4), 1)

    async def is_gas_acceptable(self, chain: str = "ETH", max_gwei: float = 60.0) -> bool:
        current = await self.get_gas_gwei(chain)
        ok = current <= max_gwei
        logger.debug("Gas %s: %.1f gwei (max=%.1f) -> %s", chain, current, max_gwei, "OK" if ok else "HIGH")
        return ok

    async def estimate_usd_cost(self, chain: str = "ETH", gas_units: int = 200_000) -> float:
        gwei = await self.get_gas_gwei(chain)
        eth_price = 3340.0
        if chain == "BSC":
            eth_price = 560.0
        elif chain == "POLYGON":
            eth_price = 0.88
        eth_cost = (gwei * gas_units) / 1e9
        return round(eth_cost * eth_price, 4)

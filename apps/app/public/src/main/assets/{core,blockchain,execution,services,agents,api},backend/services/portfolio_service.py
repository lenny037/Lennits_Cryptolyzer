"""MODULE 04/16: Portfolio Service — multi-chain portfolio management."""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Dict, List


@dataclass
class Asset:
    symbol: str
    chain: str
    amount: float
    usd_value: float
    apy: float = 0.0


class PortfolioService:
    def __init__(self) -> None:
        self._assets: List[Asset] = [
            Asset("BTC",   "BTC",     1.234,   85_000.0),
            Asset("ETH",   "ETH",     12.5,    42_000.0, apy=4.2),
            Asset("SOL",   "SOL",     350.0,   14_000.0, apy=7.1),
            Asset("MATIC", "POLYGON", 8_000.0, 5_600.0,  apy=5.8),
            Asset("BNB",   "BSC",     22.0,    8_400.0,  apy=6.3),
        ]

    def get_total_value_usd(self) -> float:
        return sum(a.usd_value for a in self._assets)

    def get_allocation(self) -> Dict[str, float]:
        total = self.get_total_value_usd()
        return {a.symbol: round(a.usd_value / total * 100, 2) for a in self._assets}

    def rebalance(self, target_allocation: Dict[str, float]) -> List[dict]:
        """Returns list of rebalancing trades required."""
        trades = []
        current = self.get_allocation()
        for symbol, target_pct in target_allocation.items():
            current_pct = current.get(symbol, 0.0)
            delta_pct = target_pct - current_pct
            if abs(delta_pct) > 1.0:  # 1% threshold
                trades.append({
                    "symbol": symbol,
                    "action": "BUY" if delta_pct > 0 else "SELL",
                    "delta_pct": round(delta_pct, 2),
                })
        return trades

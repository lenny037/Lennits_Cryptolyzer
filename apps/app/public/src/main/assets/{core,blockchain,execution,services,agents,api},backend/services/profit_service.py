"""MODULE 16: Profit Service — P&L tracking and reporting."""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import List


@dataclass
class ProfitRecord:
    strategy: str
    profit_usd: float
    timestamp: str
    tx_hash: str = ""


class ProfitService:
    def __init__(self) -> None:
        self._records: List[ProfitRecord] = []
        self._total_usd: float = 0.0

    def record_profit(self, strategy: str, profit_usd: float, tx_hash: str = "") -> None:
        import datetime
        self._records.append(ProfitRecord(
            strategy=strategy,
            profit_usd=profit_usd,
            timestamp=datetime.datetime.utcnow().isoformat(),
            tx_hash=tx_hash,
        ))
        self._total_usd += profit_usd

    @property
    def total_profit_usd(self) -> float:
        return round(self._total_usd, 4)

    def get_summary(self) -> dict:
        by_strategy: dict[str, float] = {}
        for r in self._records:
            by_strategy[r.strategy] = round(by_strategy.get(r.strategy, 0.0) + r.profit_usd, 4)
        return {"total_usd": self.total_profit_usd, "by_strategy": by_strategy, "trade_count": len(self._records)}

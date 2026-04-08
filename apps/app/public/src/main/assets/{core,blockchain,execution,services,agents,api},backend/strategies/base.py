"""MODULE 15: Base Strategy interface."""
from __future__ import annotations
from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Optional


@dataclass
class StrategyResult:
    strategy_name: str
    action: str           # EXECUTE | SKIP | ERROR
    profit_estimate_usd: float = 0.0
    confidence: float = 0.0
    detail: str = ""


class BaseStrategy(ABC):
    name: str = "base"

    @abstractmethod
    async def evaluate(self, market_data: dict) -> StrategyResult:
        ...

    @abstractmethod
    async def execute(self, result: StrategyResult) -> bool:
        ...

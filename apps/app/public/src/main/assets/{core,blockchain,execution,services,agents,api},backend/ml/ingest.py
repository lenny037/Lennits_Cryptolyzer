"""MODULE 17: Data Ingest — market data collection pipeline."""
from __future__ import annotations

import asyncio
from dataclasses import dataclass, field
from typing import List, Optional

import aiohttp
import pandas as pd

from app.core.logger import get_logger

logger = get_logger(__name__)


def _make_session() -> Optional[aiohttp.ClientSession]:
    return None  # Lazy-init to avoid event loop issues at import time


@dataclass
class DataIngestor:
    """Fetches OHLCV data from CoinGecko / Binance API."""
    _session: Optional[aiohttp.ClientSession] = field(default_factory=_make_session, init=False, repr=False)

    async def _get_session(self) -> aiohttp.ClientSession:
        if self._session is None or self._session.closed:
            self._session = aiohttp.ClientSession(timeout=aiohttp.ClientTimeout(total=10))
        return self._session

    async def fetch_ohlcv(
        self,
        symbol: str = "bitcoin",
        days: int = 90,
        currency: str = "usd",
    ) -> pd.DataFrame:
        """Fetch daily OHLCV from CoinGecko."""
        session = await self._get_session()
        url = f"https://api.coingecko.com/api/v3/coins/{symbol}/ohlc"
        params = {"vs_currency": currency, "days": str(days)}
        try:
            async with session.get(url, params=params) as resp:
                if resp.status != 200:
                    logger.warning("CoinGecko %s: HTTP %d — using synthetic data", symbol, resp.status)
                    return self._synthetic_ohlcv(days)
                data = await resp.json()
                df = pd.DataFrame(data, columns=["timestamp", "open", "high", "low", "close"])
                df["volume"] = df["close"] * 1000  # CoinGecko OHLC has no volume — estimate
                df["timestamp"] = pd.to_datetime(df["timestamp"], unit="ms")
                df.set_index("timestamp", inplace=True)
                return df
        except Exception as e:
            logger.error("Ingest error for %s: %s — using synthetic", symbol, e)
            return self._synthetic_ohlcv(days)

    def _synthetic_ohlcv(self, days: int = 90) -> pd.DataFrame:
        """Generate synthetic OHLCV for testing when API is unavailable."""
        import numpy as np, datetime
        dates = pd.date_range(end=datetime.datetime.utcnow(), periods=days, freq="D")
        price = 50_000.0
        rows = []
        for d in dates:
            change = np.random.normal(0, 0.02)
            open_  = price
            close_ = price * (1 + change)
            high_  = max(open_, close_) * (1 + abs(np.random.normal(0, 0.005)))
            low_   = min(open_, close_) * (1 - abs(np.random.normal(0, 0.005)))
            vol_   = np.random.uniform(1e8, 5e8)
            rows.append([open_, high_, low_, close_, vol_])
            price  = close_
        return pd.DataFrame(rows, index=dates, columns=["open", "high", "low", "close", "volume"])

    async def close(self) -> None:
        if self._session and not self._session.closed:
            await self._session.close()

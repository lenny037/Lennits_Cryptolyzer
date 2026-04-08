"""Data ingestion — fetches and normalises sports and odds data."""
from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
from typing import Any, Dict, Optional
import logging

import pandas as pd
import requests
from requests.adapters import HTTPAdapter, Retry

logger = logging.getLogger(__name__)


def _make_session() -> requests.Session:
    """Creates a session with automatic retry on transient failures."""
    session = requests.Session()
    retry = Retry(total=3, backoff_factor=0.5, status_forcelist=[500, 502, 503, 504])
    session.mount("https://", HTTPAdapter(max_retries=retry))
    session.mount("http://", HTTPAdapter(max_retries=retry))
    return session


@dataclass
class DataIngestor:
    """Ingests sports and betting data from external APIs or local CSVs.

    FIX: requests.Session() was used as a dataclass default — mutable defaults
    are shared across instances. Now uses field(default_factory=...) so each
    instance gets its own session with retry logic baked in.
    """
    api_key: Optional[str] = None
    base_url: str = "https://api.example.com"
    # Each instance gets a fresh session — no shared mutable state
    session: requests.Session = field(default_factory=_make_session)

    def _request(self, endpoint: str, params: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        url = f"{self.base_url}/{endpoint.lstrip('/')}"
        headers: Dict[str, str] = {}
        if self.api_key:
            headers["Authorization"] = f"Bearer {self.api_key}"
        logger.debug("GET %s params=%s", url, params)
        response = self.session.get(url, params=params, headers=headers, timeout=30)
        response.raise_for_status()
        return response.json()

    def fetch_games(self, sport: str, start_date: datetime, end_date: datetime) -> pd.DataFrame:
        params = {
            "sport": sport,
            "startDate": start_date.strftime("%Y-%m-%d"),
            "endDate": end_date.strftime("%Y-%m-%d"),
        }
        data = self._request("/games", params=params)
        games = pd.DataFrame(data.get("games", []))
        logger.info("Fetched %d games for %s", len(games), sport)
        return games

    def fetch_odds(self, sport: str, market: str) -> pd.DataFrame:
        data = self._request("/odds", params={"sport": sport, "market": market})
        odds = pd.DataFrame(data.get("odds", []))
        logger.info("Fetched %d odds rows for %s/%s", len(odds), sport, market)
        return odds

    @staticmethod
    def load_local_csv(path: str) -> pd.DataFrame:
        logger.debug("Loading CSV: %s", path)
        return pd.read_csv(path)

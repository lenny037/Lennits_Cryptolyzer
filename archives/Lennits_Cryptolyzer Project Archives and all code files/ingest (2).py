"""Data ingestion module for the Lennit Bet Analyzer.

This module provides classes and functions to fetch and normalize sports
statistics and betting odds from external APIs or local CSV sources.

The design emphasizes testability and separation of concerns: API calls
are encapsulated in the `DataIngestor` class, and data normalization
functions operate on pandas DataFrames.
"""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Any, Dict, Optional
import logging
import pandas as pd
import requests

logger = logging.getLogger(__name__)

@dataclass
class DataIngestor:
    """Ingests sports and betting data from various sources.

    Attributes:
        api_key (Optional[str]): API key for external data providers.
        base_url (str): Base URL for the sports data API.
        session (requests.Session): Reusable HTTP session.
    """
    api_key: Optional[str] = None
    base_url: str = "https://api.example.com"
    session: requests.Session = requests.Session()

    def _request(self, endpoint: str, params: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """Internal helper to perform an HTTP GET request.

        Args:
            endpoint (str): API endpoint.
            params (Optional[Dict[str, Any]]): Query parameters.

        Returns:
            Dict[str, Any]: JSON-decoded response.
        """
        url = f"{self.base_url}/{endpoint.lstrip('/')}"
        headers: Dict[str, str] = {}
        if self.api_key:
            headers["Authorization"] = f"Bearer {self.api_key}"
        logger.debug("Requesting URL %s with params %s", url, params)
        response = self.session.get(url, params=params, headers=headers, timeout=30)
        response.raise_for_status()
        return response.json()

    def fetch_games(self, sport: str, start_date: datetime, end_date: datetime) -> pd.DataFrame:
        """Fetches historical game results for a sport between two dates.

        Args:
            sport (str): Sport identifier (e.g. "NFL", "NBA").
            start_date (datetime): Start of the date range.
            end_date (datetime): End of the date range.

        Returns:
            pd.DataFrame: Normalized game results data.
        """
        params = {
            "sport": sport,
            "startDate": start_date.strftime("%Y-%m-%d"),
            "endDate": end_date.strftime("%Y-%m-%d"),
        }
        data = self._request("/games", params=params)
        games = pd.DataFrame(data.get("games", []))
        logger.info("Fetched %d games for %s between %s and %s", len(games), sport, start_date, end_date)
        return games

    def fetch_odds(self, sport: str, market: str) -> pd.DataFrame:
        """Fetches current betting odds for a sport and market.

        Args:
            sport (str): Sport identifier.
            market (str): Betting market identifier (e.g. "spread", "moneyline").

        Returns:
            pd.DataFrame: Odds data normalized across sportsbooks.
        """
        params = {"sport": sport, "market": market}
        data = self._request("/odds", params=params)
        odds = pd.DataFrame(data.get("odds", []))
        logger.info("Fetched %d odds rows for %s market %s", len(odds), sport, market)
        return odds

    @staticmethod
    def load_local_csv(path: str) -> pd.DataFrame:
        """Loads a local CSV file into a DataFrame.

        Args:
            path (str): Path to the CSV file.

        Returns:
            pd.DataFrame: Loaded DataFrame.
        """
        logger.debug("Loading CSV from %s", path)
        return pd.read_csv(path)

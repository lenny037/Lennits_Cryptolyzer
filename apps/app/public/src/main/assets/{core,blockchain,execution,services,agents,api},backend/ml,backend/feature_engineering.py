"""Feature engineering for the Lennit CryptoLyzer ML pipeline."""
from __future__ import annotations

from typing import Set
import pandas as pd


DROP_COLS: Set[str] = {"game_id", "team_id", "team_score", "opponent_score", "home_away"}


def build_features(games: pd.DataFrame, odds: pd.DataFrame) -> pd.DataFrame:
    """Merges game stats with odds and computes derived features.

    Args:
        games: Game-level statistics keyed on game_id + team_id.
        odds:  Corresponding betting odds.

    Returns:
        Feature matrix ready for model training or inference.
    """
    merged = pd.merge(games, odds, on=["game_id", "team_id"], how="inner")

    if {"team_score", "opponent_score"}.issubset(merged.columns):
        merged["scoring_margin"] = merged["team_score"] - merged["opponent_score"]

    if "home_away" in merged.columns:
        merged["is_home"] = (merged["home_away"] == "home").astype(int)

    if "market" in merged.columns:
        merged = pd.get_dummies(merged, columns=["market"], drop_first=True)

    feature_cols = [c for c in merged.columns if c not in DROP_COLS]
    return merged[feature_cols].copy()

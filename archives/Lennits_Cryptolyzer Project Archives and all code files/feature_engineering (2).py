"""Feature engineering routines for the Lennit Bet Analyzer.

Transforms raw game statistics and betting odds into model-ready features.
"""
from __future__ import annotations

from typing import List
import pandas as pd

def build_features(games: pd.DataFrame, odds: pd.DataFrame) -> pd.DataFrame:
    """Constructs a feature DataFrame from raw games and odds data.

    This function merges the games and odds data on common identifiers and
    computes derived statistics (e.g. win/loss indicators, scoring margins).

    Args:
        games (pd.DataFrame): Game-level statistics.
        odds (pd.DataFrame): Corresponding betting odds.

    Returns:
        pd.DataFrame: Feature matrix for model training or inference.
    """
    # Example merge on game_id and team_id; adjust as necessary for your API schema.
    merged = pd.merge(games, odds, on=["game_id", "team_id"], how="inner")

    # Example derived features: scoring margin, implied win probability, etc.
    if "team_score" in merged.columns and "opponent_score" in merged.columns:
        merged["scoring_margin"] = merged["team_score"] - merged["opponent_score"]
    if "home_away" in merged.columns:
        merged["is_home"] = merged["home_away"] == "home"

    # Convert categorical variables to dummy variables.
    if "market" in merged.columns:
        merged = pd.get_dummies(merged, columns=["market"], drop_first=True)

    # Drop non-feature columns (e.g. text identifiers)
    drop_cols = {"game_id", "team_id", "team_score", "opponent_score", "home_away"}
    feature_cols = [col for col in merged.columns if col not in drop_cols]
    return merged[feature_cols].copy()

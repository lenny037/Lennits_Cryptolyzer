"""Backtesting and ROI simulation."""
from __future__ import annotations

import numpy as np
import pandas as pd


def simulate_bets(
    pred_probs: np.ndarray,
    outcomes: np.ndarray,
    threshold: float = 0.55,
    stake: float = 1.0,
) -> pd.DataFrame:
    """Simulates placing bets based on model probabilities and actual outcomes.

    Args:
        pred_probs: Predicted win probabilities (0-1).
        outcomes:   Actual binary outcomes (1 = win, 0 = loss).
        threshold:  Probability cutoff above which a bet is placed.
        stake:      Fixed stake amount per bet.

    Returns:
        DataFrame with columns: pred_prob, outcome, bet_placed, return,
        cumulative_return, cumulative_roi.
    """
    if len(pred_probs) != len(outcomes):
        raise ValueError("pred_probs and outcomes must have the same length")

    place_bet = pred_probs >= threshold
    returns = np.where(place_bet, np.where(outcomes == 1, stake, -stake), 0.0)

    results = pd.DataFrame({
        "pred_prob": pred_probs,
        "outcome": outcomes,
        "bet_placed": place_bet,
        "return": returns,
    })

    results["cumulative_return"] = results["return"].cumsum()

    # Total staked so far (only counts rounds where a bet was placed)
    total_staked = (results["bet_placed"].astype(float) * stake).cumsum()
    # Avoid division by zero on rows where no bet has been placed yet
    results["cumulative_roi"] = results["cumulative_return"] / total_staked.where(total_staked > 0)

    return results

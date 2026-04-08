"""Backtesting and ROI simulation for Lennit Bet Analyzer.

Provides functions to evaluate model predictions in a simulated betting
environment using historical data.
"""
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
        pred_probs (np.ndarray): Predicted win probabilities.
        outcomes (np.ndarray): Actual binary outcomes (1 if bet wins, 0 otherwise).
        threshold (float): Probability threshold above which a bet is placed.
        stake (float): Stake amount per bet.

    Returns:
        pd.DataFrame: Detailed results including returns and cumulative ROI.
    """
    if len(pred_probs) != len(outcomes):
        raise ValueError("pred_probs and outcomes must have the same length")

    place_bet = pred_probs >= threshold
    # Payoff: +stake on a win, -stake on a loss, 0 if no bet
    returns = np.where(place_bet, np.where(outcomes == 1, stake, -stake), 0.0)
    results = pd.DataFrame(
        {
            "pred_prob": pred_probs,
            "outcome": outcomes,
            "bet_placed": place_bet,
            "return": returns,
        }
    )
    results["cumulative_return"] = results["return"].cumsum()
    bet_amounts = (results["bet_placed"] * stake).cumsum().replace(0, np.nan)
    results["cumulative_roi"] = results["cumulative_return"] / bet_amounts
    return results

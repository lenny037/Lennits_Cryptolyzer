from pathlib import Path

import numpy as np
import pandas as pd

from lennits_cryptolyzer.main import _simulate_roi, _calc_calibration_bins  # type: ignore


def test_roi_simulation_basic():
    probs = np.array([0.6, 0.4, 0.7, 0.3])
    y_true = np.array([1, 0, 0, 1])
    odds = np.array([2.0, 2.0, 2.5, 1.8])
    edge_threshold = 0.02

    stats = _simulate_roi(probs, y_true, odds, edge_threshold=edge_threshold)
    assert "total_bets" in stats
    assert "roi_pct" in stats
    assert "hit_rate" in stats
    assert "max_drawdown_pct" in stats
    assert stats["total_bets"] >= 0


def test_calibration_bins_shape():
    probs = np.linspace(0.1, 0.9, 20)
    y_true = (probs > 0.5).astype(int)
    bins = _calc_calibration_bins(probs, y_true, n_bins=5)
    assert len(bins) <= 5
    for b in bins:
        assert "mean_pred" in b
        assert "empirical_rate" in b
        assert "count" in b
        assert b["count"] > 0
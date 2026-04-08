"""MODULE 17: Backtesting engine — strategy performance simulation."""
from __future__ import annotations

from dataclasses import dataclass
from typing import List

import numpy as np
import pandas as pd

from app.core.logger import get_logger

logger = get_logger(__name__)


@dataclass
class BacktestResult:
    total_return_pct: float
    annualized_return_pct: float
    sharpe_ratio: float
    max_drawdown_pct: float
    win_rate: float
    total_trades: int
    profit_factor: float


def run_backtest(
    df: pd.DataFrame,
    signals: pd.Series,
    initial_capital: float = 10_000.0,
    position_size_pct: float = 0.1,
    commission_pct: float = 0.001,
) -> BacktestResult:
    """Simulate strategy returns given entry/exit signals."""
    capital = initial_capital
    position = 0.0
    entry_price = 0.0
    trades: List[float] = []
    equity: List[float] = [capital]

    for i in range(1, len(df)):
        price  = df["close"].iloc[i]
        signal = signals.iloc[i - 1] if i - 1 < len(signals) else 0

        if signal == 1 and position == 0:
            # BUY
            size = capital * position_size_pct
            position = size / price * (1 - commission_pct)
            entry_price = price
            capital -= size

        elif signal == -1 and position > 0:
            # SELL
            proceeds = position * price * (1 - commission_pct)
            pnl = proceeds - (position * entry_price)
            trades.append(pnl)
            capital += proceeds
            position = 0.0

        equity.append(capital + position * price)

    # Close any open position at end
    if position > 0:
        final_price = df["close"].iloc[-1]
        proceeds = position * final_price * (1 - commission_pct)
        trades.append(proceeds - position * entry_price)
        capital += proceeds
        position = 0.0

    equity_series = np.array(equity)
    returns = np.diff(equity_series) / equity_series[:-1]
    peak = np.maximum.accumulate(equity_series)
    drawdowns = (equity_series - peak) / peak

    total_return    = (equity_series[-1] - initial_capital) / initial_capital * 100
    annual_return   = total_return * (252 / max(len(df), 1))
    sharpe          = (returns.mean() / (returns.std() + 1e-8)) * np.sqrt(252)
    max_dd          = drawdowns.min() * 100
    win_rate        = sum(1 for t in trades if t > 0) / max(len(trades), 1)
    gross_profit    = sum(t for t in trades if t > 0) or 1e-8
    gross_loss      = abs(sum(t for t in trades if t < 0)) or 1e-8
    profit_factor   = gross_profit / gross_loss

    logger.info(
        "Backtest: return=%.2f%% sharpe=%.2f max_dd=%.2f%% win_rate=%.1f%%",
        total_return, sharpe, max_dd, win_rate * 100,
    )

    return BacktestResult(
        total_return_pct=round(total_return, 4),
        annualized_return_pct=round(annual_return, 4),
        sharpe_ratio=round(sharpe, 4),
        max_drawdown_pct=round(max_dd, 4),
        win_rate=round(win_rate, 4),
        total_trades=len(trades),
        profit_factor=round(profit_factor, 4),
    )

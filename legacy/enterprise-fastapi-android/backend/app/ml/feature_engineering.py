"""MODULE 17: Feature Engineering — technical indicator pipeline."""
from __future__ import annotations

import numpy as np
import pandas as pd
from app.core.logger import get_logger

logger = get_logger(__name__)


def add_technical_indicators(df: pd.DataFrame, window: int = 14) -> pd.DataFrame:
    """Add RSI, MACD, Bollinger Bands, ATR, OBV to price data."""
    df = df.copy()

    # ── RSI ────────────────────────────────────────────────────────────────
    delta = df["close"].diff()
    gain  = delta.clip(lower=0).rolling(window).mean()
    loss  = (-delta.clip(upper=0)).rolling(window).mean()
    rs    = gain / loss.replace(0, np.nan)
    df["rsi"] = 100 - (100 / (1 + rs))

    # ── MACD ───────────────────────────────────────────────────────────────
    ema12 = df["close"].ewm(span=12, adjust=False).mean()
    ema26 = df["close"].ewm(span=26, adjust=False).mean()
    df["macd"]        = ema12 - ema26
    df["macd_signal"] = df["macd"].ewm(span=9, adjust=False).mean()
    df["macd_hist"]   = df["macd"] - df["macd_signal"]

    # ── Bollinger Bands ────────────────────────────────────────────────────
    sma20 = df["close"].rolling(20).mean()
    std20 = df["close"].rolling(20).std()
    df["bb_upper"] = sma20 + 2 * std20
    df["bb_lower"] = sma20 - 2 * std20
    df["bb_pct"]   = (df["close"] - df["bb_lower"]) / (df["bb_upper"] - df["bb_lower"] + 1e-8)

    # ── ATR ────────────────────────────────────────────────────────────────
    hl   = df["high"] - df["low"]
    hpc  = (df["high"] - df["close"].shift()).abs()
    lpc  = (df["low"]  - df["close"].shift()).abs()
    tr   = pd.concat([hl, hpc, lpc], axis=1).max(axis=1)
    df["atr"] = tr.rolling(window).mean()

    # ── OBV ────────────────────────────────────────────────────────────────
    obv = np.where(df["close"] > df["close"].shift(), df["volume"],
          np.where(df["close"] < df["close"].shift(), -df["volume"], 0))
    df["obv"] = obv.cumsum() if hasattr(obv, "cumsum") else pd.Series(obv).cumsum().values

    # ── Volume ratios ──────────────────────────────────────────────────────
    df["volume_ma"]    = df["volume"].rolling(20).mean()
    df["volume_ratio"] = df["volume"] / df["volume_ma"].replace(0, np.nan)

    # ── Price returns ──────────────────────────────────────────────────────
    df["return_1"]  = df["close"].pct_change(1)
    df["return_5"]  = df["close"].pct_change(5)
    df["return_20"] = df["close"].pct_change(20)

    logger.debug("Feature engineering: added %d indicators", 12)
    return df.dropna()


FEATURE_COLS = [
    "rsi", "macd", "macd_signal", "macd_hist",
    "bb_pct", "atr", "obv", "volume_ratio",
    "return_1", "return_5", "return_20",
]

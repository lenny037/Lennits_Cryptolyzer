"""MODULE 17: Live Predictor — real-time inference engine."""
from __future__ import annotations

from typing import Optional

import pandas as pd

from app.core.logger import get_logger
from app.ml.feature_engineering import add_technical_indicators, FEATURE_COLS

logger = get_logger(__name__)


class LivePredictor:
    """Wraps a trained ModelArtifacts for real-time signal generation."""

    def __init__(self, artifacts=None) -> None:
        self._artifacts = artifacts
        logger.info("LivePredictor initialized (model_loaded=%s)", artifacts is not None)

    def load(self, path: str) -> None:
        from app.ml.model import load_model
        self._artifacts = load_model(path)
        logger.info("LivePredictor: model loaded from %s metrics=%s", path, self._artifacts.metrics)

    def predict(self, df: pd.DataFrame) -> Optional[dict]:
        """Return prediction dict for latest bar."""
        if self._artifacts is None:
            return None

        try:
            features = add_technical_indicators(df)
            X = features[FEATURE_COLS].iloc[[-1]]
            prob = self._artifacts.pipeline.predict_proba(X)[0, 1]
            signal = "BUY" if prob > 0.60 else ("SELL" if prob < 0.40 else "HOLD")
            return {"signal": signal, "probability": round(float(prob), 4)}
        except Exception as e:
            logger.error("LivePredictor.predict error: %s", e)
            return None


predictor = LivePredictor()

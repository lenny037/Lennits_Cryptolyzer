"""MODULE 17: ML Model — GradientBoosting + Logistic Regression pipeline."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Literal

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import brier_score_loss, log_loss, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

from app.core.logger import get_logger

logger = get_logger(__name__)

Algorithm = Literal["gbc", "lr"]


@dataclass
class ModelArtifacts:
    """Trained sklearn Pipeline (scaler + classifier) + validation metrics."""
    pipeline: Pipeline
    metrics: Dict[str, float]


def train_model(
    X: pd.DataFrame,
    y: pd.Series,
    algorithm: Algorithm = "gbc",
    test_size: float = 0.2,
    random_state: int = 42,
) -> ModelArtifacts:
    """Train and validate a classification pipeline."""
    if algorithm == "lr":
        clf = LogisticRegression(max_iter=1000, solver="liblinear")
    elif algorithm == "gbc":
        clf = GradientBoostingClassifier(n_estimators=200, learning_rate=0.05, max_depth=4)
    else:
        raise ValueError(f"Unknown algorithm '{algorithm}'. Choose 'gbc' or 'lr'.")

    X_train, X_val, y_train, y_val = train_test_split(
        X, y, test_size=test_size, random_state=random_state, stratify=y
    )

    pipe = Pipeline([("scaler", StandardScaler()), ("model", clf)])
    pipe.fit(X_train, y_train)

    y_prob = pipe.predict_proba(X_val)[:, 1]
    metrics = {
        "log_loss":    round(log_loss(y_val, y_prob), 6),
        "roc_auc":     round(roc_auc_score(y_val, y_prob), 6),
        "brier_score": round(brier_score_loss(y_val, y_prob), 6),
    }
    logger.info("Model trained: algo=%s roc_auc=%.4f", algorithm, metrics["roc_auc"])
    return ModelArtifacts(pipeline=pipe, metrics=metrics)


def predict(artifacts: ModelArtifacts, X: pd.DataFrame) -> np.ndarray:
    return artifacts.pipeline.predict_proba(X)[:, 1]


def save_model(artifacts: ModelArtifacts, path: str) -> None:
    joblib.dump({"pipeline": artifacts.pipeline, "metrics": artifacts.metrics}, path)
    logger.info("Model saved to %s", path)


def load_model(path: str) -> ModelArtifacts:
    obj = joblib.load(path)
    return ModelArtifacts(pipeline=obj["pipeline"], metrics=obj["metrics"])

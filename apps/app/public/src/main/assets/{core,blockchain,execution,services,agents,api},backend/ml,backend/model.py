"""Model training and prediction for Lennit CryptoLyzer.

Supports GradientBoostingClassifier ('gbc') and LogisticRegression ('lr').
The scaler is baked into the sklearn Pipeline — ModelArtifacts.pipeline is
the single source of truth for inference; no separate scaler needed.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Dict, Literal

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import brier_score_loss, log_loss, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

Algorithm = Literal["gbc", "lr"]


@dataclass
class ModelArtifacts:
    """Encapsulates a trained sklearn Pipeline and its validation metrics.

    The pipeline includes preprocessing (StandardScaler) + classifier,
    so you only ever need to call pipeline.predict_proba() — no separate
    scaler step required.
    """
    pipeline: Pipeline
    metrics: Dict[str, float]


def train_model(
    X: pd.DataFrame,
    y: pd.Series,
    algorithm: Algorithm = "gbc",
    test_size: float = 0.2,
    random_state: int = 42,
) -> ModelArtifacts:
    """Trains a classification pipeline on the provided dataset.

    Args:
        X: Feature matrix.
        y: Binary target vector.
        algorithm: 'gbc' for GradientBoostingClassifier, 'lr' for Logistic Regression.
        test_size: Fraction reserved for validation.
        random_state: Reproducibility seed.

    Returns:
        ModelArtifacts with fitted pipeline and validation metrics.

    Raises:
        ValueError: If algorithm is not 'gbc' or 'lr'.
    """
    # FIX: previously fell through to GBC silently for unknown algorithms
    if algorithm == "lr":
        classifier = LogisticRegression(max_iter=1000, solver="liblinear")
    elif algorithm == "gbc":
        classifier = GradientBoostingClassifier()
    else:
        raise ValueError(f"Unknown algorithm '{algorithm}'. Choose 'gbc' or 'lr'.")

    X_train, X_val, y_train, y_val = train_test_split(
        X, y, test_size=test_size, random_state=random_state, stratify=y
    )

    pipeline = Pipeline([("scaler", StandardScaler()), ("model", classifier)])
    pipeline.fit(X_train, y_train)

    y_pred_proba = pipeline.predict_proba(X_val)[:, 1]
    metrics = {
        "log_loss": log_loss(y_val, y_pred_proba),
        "roc_auc": roc_auc_score(y_val, y_pred_proba),
        "brier_score": brier_score_loss(y_val, y_pred_proba),
    }

    return ModelArtifacts(pipeline=pipeline, metrics=metrics)


def predict(artifacts: ModelArtifacts, X: pd.DataFrame) -> np.ndarray:
    """Returns predicted probabilities of the positive class."""
    return artifacts.pipeline.predict_proba(X)[:, 1]


def save_model(artifacts: ModelArtifacts, path: str) -> None:
    """Serialises model artifacts to disk."""
    joblib.dump({"pipeline": artifacts.pipeline, "metrics": artifacts.metrics}, path)


def load_model(path: str) -> ModelArtifacts:
    """Loads model artifacts from disk."""
    obj = joblib.load(path)
    return ModelArtifacts(pipeline=obj["pipeline"], metrics=obj["metrics"])

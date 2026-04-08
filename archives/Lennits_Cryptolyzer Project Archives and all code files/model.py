"""Model training and prediction for Lennit Bet Analyzer.

Defines functions to train machine learning models and perform inference.
Supports multiple algorithms including gradient boosting and logistic regression.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Dict, Tuple
import joblib
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.metrics import log_loss, roc_auc_score, brier_score_loss
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import GradientBoostingClassifier

@dataclass
class ModelArtifacts:
    """Encapsulates model and preprocessing artifacts."""

    model: Any
    scaler: StandardScaler
    metrics: Dict[str, float]

def train_model(
    X: pd.DataFrame,
    y: pd.Series,
    algorithm: str = "gbc",
    test_size: float = 0.2,
    random_state: int = 42,
) -> ModelArtifacts:
    """Trains a classification model on the provided dataset.

    Args:
        X (pd.DataFrame): Feature matrix.
        y (pd.Series): Target vector.
        algorithm (str): Model algorithm ("gbc" for GradientBoostingClassifier, "lr" for Logistic Regression).
        test_size (float): Fraction of data to reserve for validation.
        random_state (int): Random seed.

    Returns:
        ModelArtifacts: Trained model, scaler, and evaluation metrics.
    """
    X_train, X_val, y_train, y_val = train_test_split(
        X,
        y,
        test_size=test_size,
        random_state=random_state,
        stratify=y,
    )

    scaler = StandardScaler()

    if algorithm == "lr":
        model = LogisticRegression(max_iter=1000, solver="liblinear")
    else:
        model = GradientBoostingClassifier()

    pipeline = Pipeline([("scaler", scaler), ("model", model)])

    pipeline.fit(X_train, y_train)

    y_pred_proba = pipeline.predict_proba(X_val)[:, 1]
    metrics = {
        "log_loss": log_loss(y_val, y_pred_proba),
        "roc_auc": roc_auc_score(y_val, y_pred_proba),
        "brier_score": brier_score_loss(y_val, y_pred_proba),
    }

    return ModelArtifacts(model=pipeline, scaler=scaler, metrics=metrics)

def predict(model_artifacts: ModelArtifacts, X: pd.DataFrame) -> np.ndarray:
    """Generates probability predictions using the trained model.

    Args:
        model_artifacts (ModelArtifacts): Trained model and scaler.
        X (pd.DataFrame): Feature matrix for inference.

    Returns:
        np.ndarray: Predicted probabilities of the positive class.
    """
    return model_artifacts.model.predict_proba(X)[:, 1]

def save_model(artifacts: ModelArtifacts, path: str) -> None:
    """Serializes trained model artifacts to disk.

    Args:
        artifacts (ModelArtifacts): Model and scaler to persist.
        path (str): Destination file path (e.g. "models/latest.pkl").
    """
    joblib.dump(
        {"model": artifacts.model, "scaler": artifacts.scaler, "metrics": artifacts.metrics},
        path,
    )

def load_model(path: str) -> ModelArtifacts:
    """Loads model artifacts from disk.

    Args:
        path (str): Path to a serialized model file.

    Returns:
        ModelArtifacts: Loaded model, scaler, and metrics.
    """
    obj = joblib.load(path)
    return ModelArtifacts(model=obj["model"], scaler=obj["scaler"], metrics=obj["metrics"])

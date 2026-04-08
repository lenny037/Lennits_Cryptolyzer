import os
import json
from pathlib import Path

import numpy as np
import pandas as pd
from fastapi.testclient import TestClient

from main import app  # root app

ROOT = Path(__file__).resolve().parents[1]
CRYPT = ROOT / "lennits_cryptolyzer"
BUFFER = CRYPT / "buffer"


client = TestClient(app)


def _make_dummy_csv(path: Path):
    df = pd.DataFrame(
        {
            "target": [0, 1, 0, 1, 1, 0],
            "decimal_odds": [1.9, 2.1, 1.8, 2.3, 2.0, 1.7],
            "feature_a": [0.1, 0.2, -0.1, 0.3, 0.0, 0.05],
            "feature_b": [5, 6, 4, 7, 5, 6],
        }
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(path, index=False)


def test_health_endpoints():
    r_root = client.get("/health")
    assert r_root.status_code == 200
    assert r_root.json().get("status") == "ok"

    r_crypt = client.get("/cryptolyzer/health")
    assert r_crypt.status_code == 200
    assert r_crypt.json().get("status") == "ok"


def test_ingest_train_predict_dummy(tmp_path):
    # Prepare dummy CSV file on disk
    csv_path = tmp_path / "train.csv"
    _make_dummy_csv(csv_path)

    # Ingest via API
    with open(csv_path, "rb") as f:
        files = {"file": ("train.csv", f, "text/csv")}
        r_ing = client.post("/cryptolyzer/ingest", files=files)
    assert r_ing.status_code == 200
    ing = r_ing.json()
    assert ing["rows"] == 6
    assert "target" in ing["columns"]
    data_path = ing["path"]
    assert os.path.exists(data_path)

    # Train dummy model
    payload = {
        "model": "Dummy",
        "data_path": data_path,
        "target_col": "target",
        "odds_col": "decimal_odds",
        "test_size": 0.3,
        "edge_threshold": 0.02,
    }
    r_tr = client.post("/cryptolyzer/train", json=payload)
    assert r_tr.status_code == 200
    train = r_tr.json()
    assert train["status"] == "ok"
    assert os.path.exists(train["model_path"])
    metrics = train["metrics"]
    assert "log_loss" in metrics
    assert "brier" in metrics
    assert "roi" in metrics

    # Predict using returned model_path
    records = [
        {"feature_a": 0.1, "feature_b": 5, "decimal_odds": 2.0},
        {"feature_a": -0.2, "feature_b": 7, "decimal_odds": 1.9},
    ]
    r_pr = client.post(
        "/cryptolyzer/predict",
        json={"model_path": train["model_path"], "records": records},
    )
    assert r_pr.status_code == 200
    pred = r_pr.json()
    assert pred["status"] == "ok"
    assert pred["rows"] == 2
    assert len(pred["probs"]) == 2
    assert all(0.0 <= p <= 1.0 for p in pred["probs"])
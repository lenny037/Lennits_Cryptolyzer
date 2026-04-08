"""API endpoint tests — MODULE 13."""
import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "ok"


def test_dashboard():
    r = client.get("/api/v1/dashboard")
    assert r.status_code == 200
    data = r.json()
    assert "total_portfolio_usd" in data
    assert "active_agents" in data


def test_agents():
    r = client.get("/api/v1/agents")
    assert r.status_code == 200
    agents = r.json()
    assert isinstance(agents, list)
    assert len(agents) == 20


def test_vault():
    r = client.get("/api/v1/vault")
    assert r.status_code == 200
    positions = r.json()
    assert isinstance(positions, list)
    assert len(positions) >= 1


def test_strategies():
    r = client.get("/api/v1/strategies")
    assert r.status_code == 200


def test_notifications():
    r = client.get("/api/v1/notifications")
    assert r.status_code == 200


def test_signals():
    r = client.get("/api/v1/signals")
    assert r.status_code == 200


def test_gas():
    r = client.get("/api/v1/gas")
    assert r.status_code == 200

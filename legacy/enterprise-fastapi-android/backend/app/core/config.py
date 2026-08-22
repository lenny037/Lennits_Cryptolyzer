"""Central configuration — MODULE 13: API Gateway config."""
from __future__ import annotations
from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    # ── Blockchain ─────────────────────────────────────────────────────────
    ethereum_rpc_url: str = "https://mainnet.infura.io/v3/demo"
    bsc_rpc_url: str = "https://bsc-dataseed.binance.org/"
    polygon_rpc_url: str = "https://polygon-rpc.com"
    solana_rpc_url: str = "https://api.mainnet-beta.solana.com"
    chain_id: int = 1

    # ── API Auth ───────────────────────────────────────────────────────────
    lennit_api_key: str = ""
    jwt_secret_key: str = "changeme-256-bit-secret"
    jwt_algorithm: str = "HS256"
    access_token_expire_minutes: int = 1440

    # ── Trading ────────────────────────────────────────────────────────────
    deviation_threshold: float = 5.0
    poll_interval_seconds: float = 2.0
    max_position_size_usd: float = 10_000.0
    min_profit_threshold_usd: float = 5.0

    # ── ML ─────────────────────────────────────────────────────────────────
    model_dir: str = "models/"
    feature_window: int = 50

    # ── Database ───────────────────────────────────────────────────────────
    database_url: str = "sqlite+aiosqlite:///./lennit.db"

    # ── Redis ──────────────────────────────────────────────────────────────
    redis_url: str = "redis://localhost:6379/0"

    # ── App ────────────────────────────────────────────────────────────────
    debug: bool = False
    log_level: str = "INFO"


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()

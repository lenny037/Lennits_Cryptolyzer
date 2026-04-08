"""FastAPI dependency injection — MODULE 13: API Gateway."""
from __future__ import annotations

from fastapi import Depends, Header, HTTPException, status

from app.core.config import settings
from app.core.rate_limiter import rate_limiter
from app.core.security import verify_token


async def require_api_key(x_api_key: str = Header(default="")) -> str:
    """Validate X-API-Key header."""
    if settings.lennit_api_key and x_api_key != settings.lennit_api_key:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid API key")
    return x_api_key


async def rate_limit(x_forwarded_for: str = Header(default="local")) -> None:
    """Per-IP rate limiting (60 req/min)."""
    client = x_forwarded_for.split(",")[0].strip()
    if not rate_limiter.consume(client):
        raise HTTPException(status_code=429, detail="Rate limit exceeded — 60 req/min")

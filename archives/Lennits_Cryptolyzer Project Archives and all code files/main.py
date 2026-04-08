"""LENNITS_CRYPTOLYZER — FastAPI application entry point.
   MODULE 13: API Gateway
"""
from __future__ import annotations

import asyncio
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware

from app.agents.orchestrator import Orchestrator
from app.api import routes, websocket as ws_router
from app.core.logger import get_logger

logger = get_logger(__name__)
_orchestrator: Orchestrator | None = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global _orchestrator
    logger.info("LENNITS_CRYPTOLYZER: Sovereign Bargaining Loop starting...")
    _orchestrator = Orchestrator()
    task = asyncio.create_task(_orchestrator.run(), name="orchestrator")
    logger.info("AlphaGrid: ignited — 20 agents deployed")
    yield
    if _orchestrator:
        _orchestrator.shutdown()
    task.cancel()
    try:
        await task
    except asyncio.CancelledError:
        pass
    logger.info("LENNITS_CRYPTOLYZER: System safe-stopped.")


app = FastAPI(
    title="LENNITS_CRYPTOLYZER API",
    description="Sovereign Blockchain Intelligence Platform by LENNIT_SUITE TECHNOLOGIES",
    version="2.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# ── Middleware ─────────────────────────────────────────────────────────────
app.add_middleware(GZipMiddleware, minimum_size=1000)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],           # Tighten in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Routers ────────────────────────────────────────────────────────────────
app.include_router(routes.router)
app.include_router(ws_router.router)


@app.get("/health", tags=["system"])
async def health():
    return {"status": "ok", "service": "lennits-cryptolyzer", "version": "2.0.0"}


@app.get("/", tags=["system"])
async def root():
    return {
        "name": "LENNITS_CRYPTOLYZER",
        "company": "LENNIT_SUITE TECHNOLOGIES",
        "docs": "/docs",
    }

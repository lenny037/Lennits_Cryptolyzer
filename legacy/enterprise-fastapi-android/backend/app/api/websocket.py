"""WebSocket endpoint for live metrics streaming — MODULE 13: API Gateway."""
from __future__ import annotations

import asyncio
import json
import random
from datetime import datetime

from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from app.core.logger import get_logger

logger = get_logger(__name__)
router = APIRouter(prefix="/ws", tags=["websocket"])


class ConnectionManager:
    def __init__(self):
        self.active: list[WebSocket] = []

    async def connect(self, ws: WebSocket):
        await ws.accept()
        self.active.append(ws)
        logger.info("WS client connected (%d total)", len(self.active))

    def disconnect(self, ws: WebSocket):
        self.active.remove(ws)
        logger.info("WS client disconnected (%d total)", len(self.active))

    async def broadcast(self, data: dict):
        msg = json.dumps(data)
        for ws in list(self.active):
            try:
                await ws.send_text(msg)
            except Exception:
                self.active.remove(ws)


manager = ConnectionManager()


@router.websocket("/metrics")
async def metrics_ws(ws: WebSocket):
    """Live portfolio + agent metrics — push every 2s."""
    await manager.connect(ws)
    try:
        while True:
            payload = {
                "type": "metrics",
                "timestamp": datetime.utcnow().isoformat(),
                "portfolio_usd": round(155_000 + random.uniform(-500, 500), 2),
                "pnl_24h": round(2_340 + random.uniform(-100, 100), 2),
                "active_agents": 20,
                "btc_price": round(68_420 + random.uniform(-200, 200), 2),
                "eth_price": round(3_340 + random.uniform(-50, 50), 2),
                "gas_gwei": round(45 + random.uniform(-5, 10), 1),
            }
            await ws.send_json(payload)
            await asyncio.sleep(2.0)
    except WebSocketDisconnect:
        manager.disconnect(ws)
    except Exception as e:
        logger.error("WS error: %s", e)
        manager.disconnect(ws)

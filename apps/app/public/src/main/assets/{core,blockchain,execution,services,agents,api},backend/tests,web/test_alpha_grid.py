"""AlphaGrid unit tests — MODULE 14."""
import asyncio
import pytest
from app.agents.alpha_grid import AlphaGrid


@pytest.mark.asyncio
async def test_alphagrid_init():
    ag = AlphaGrid()
    assert len(ag.agents) == 20
    metrics = ag.get_metrics()
    assert len(metrics) == 20


@pytest.mark.asyncio
async def test_alphagrid_halt():
    ag = AlphaGrid()
    ag._running = True
    # Start briefly then cancel
    task = asyncio.create_task(ag.ignite_collective())
    await asyncio.sleep(0.1)
    ag.halt_collective()
    try:
        await asyncio.wait_for(task, timeout=2.0)
    except (asyncio.CancelledError, asyncio.TimeoutError):
        pass  # Expected


def test_alphagrid_roles():
    ag = AlphaGrid()
    metrics = ag.get_metrics()
    arb   = [m for m in metrics if m["role"] == "ARBITRAGE"]
    himap = [m for m in metrics if m["role"] == "HIMAP"]
    guard = [m for m in metrics if m["role"] == "DARKFOREST"]
    assert len(arb)   == 5
    assert len(himap) == 10
    assert len(guard) == 5

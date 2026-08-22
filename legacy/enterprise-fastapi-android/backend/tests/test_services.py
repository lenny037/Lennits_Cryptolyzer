"""Service unit tests — MODULE 16."""
import pytest
from app.services.trade_validator import TradeValidator
from app.services.profit_service import ProfitService
from app.services.gas_service import GasService


def test_trade_validator_approve():
    v = TradeValidator()
    assert v.validate(1.0, 1.1) is True


def test_trade_validator_reject_zero():
    v = TradeValidator()
    assert v.validate(0, 1.0) is False


def test_trade_validator_reject_loss():
    v = TradeValidator()
    assert v.validate(1.0, 0.5) is False


def test_profit_service():
    p = ProfitService()
    p.record_profit("mev_arb", 100.0, "0x123")
    p.record_profit("yield", 50.0)
    assert p.total_profit_usd == 150.0
    summary = p.get_summary()
    assert summary["trade_count"] == 2


@pytest.mark.asyncio
async def test_gas_service():
    g = GasService()
    gwei = await g.get_gas_gwei("ETH")
    assert gwei > 0
    ok = await g.is_gas_acceptable("BSC", max_gwei=10.0)
    assert isinstance(ok, bool)

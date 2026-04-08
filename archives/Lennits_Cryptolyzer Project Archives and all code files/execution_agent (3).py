import asyncio
from app.execution.dex_executor import DexExecutor
from app.services.trade_validator import TradeValidator
from app.services.signal_service import SignalService

class ExecutionAgent:
    def __init__(self):
        self.exec = DexExecutor()
        self.validator = TradeValidator()
        self.signal = SignalService()

    async def run(self):
        while True:
            sig = self.signal.get_signal()
            if sig["deviation"] > 5:
                if self.validator.validate(1, 1.02):
                    self.exec.execute_trade(sig)
            await asyncio.sleep(2)

from app.services.gas_service import GasService
from app.services.profit_service import ProfitService

class TradeValidator:
    def __init__(self):
        self.gas = GasService()
        self.profit = ProfitService()

    def validate(self, inp, out):
        gas = self.gas.estimate()
        return self.profit.calculate(out, inp, gas) > 0

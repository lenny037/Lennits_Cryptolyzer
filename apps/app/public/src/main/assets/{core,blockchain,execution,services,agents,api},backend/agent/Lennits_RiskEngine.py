class RiskEngine:
    def __init__(self):
        self.max_drawdown = 0.02 

    def approve_action(self, cost, current_balance):
        # Always approve faucet claims since they cost 0
        if cost == 0:
            return True
        if cost <= (current_balance * self.max_drawdown):
            return True
        return False

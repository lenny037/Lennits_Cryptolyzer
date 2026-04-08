class LennitsPortfolioManager:
    def __init__(self):
        self.total_value_usd = 0.0
        self.asset_counts = {}

    def update_portfolio(self, asset, amount, usd_value):
        self.asset_counts[asset] = self.asset_counts.get(asset, 0) + amount
        self.total_value_usd += usd_value
        print(f"Portfolio Updated: Total Balance is now ${self.total_value_usd:.4f}")

    def get_summary(self):
        return {
            "total_usd": self.total_value_usd,
            "assets": self.asset_counts
        }

if __name__ == "__main__":
    pm = LennitsPortfolioManager()
    pm.update_portfolio("BTC", 0.000001, 0.065)

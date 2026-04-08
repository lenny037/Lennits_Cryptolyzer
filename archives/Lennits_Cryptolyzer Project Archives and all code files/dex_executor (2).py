from app.core.logger import logger

class DexExecutor:
    def execute_trade(self, data):
        logger.info(f"Executing trade: {data}")
        return True

from app.blockchain.web3_client import Web3Client

class GasService:
    def __init__(self):
        self.client = Web3Client()

    def estimate(self):
        return self.client.w3.eth.gas_price

from web3 import Web3
from app.core.config import settings

class Web3Client:
    def __init__(self):
        self.w3 = Web3(Web3.HTTPProvider(settings.ethereum_rpc))
        if not self.w3.is_connected():
            raise Exception("Web3 connection failed")

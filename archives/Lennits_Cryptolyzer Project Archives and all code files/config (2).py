from pydantic import BaseSettings

class Settings(BaseSettings):
    ethereum_rpc: str = "https://rpc.ankr.com/eth"
    arbitrum_rpc: str = "https://arb1.arbitrum.io/rpc"
    base_rpc: str = "https://mainnet.base.org"
    private_key: str = "YOUR_PRIVATE_KEY"

settings = Settings()

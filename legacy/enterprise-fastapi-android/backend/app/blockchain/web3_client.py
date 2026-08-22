"""MODULE 16: Web3 client — multi-chain blockchain interface."""
from __future__ import annotations

from typing import Optional

from app.core.config import settings
from app.core.logger import get_logger

logger = get_logger(__name__)


class Web3Client:
    """Manages Web3 connections to Ethereum, BSC, Polygon, Solana."""

    def __init__(self) -> None:
        self._connections: dict[str, object] = {}
        self._initialized = False

    def initialize(self) -> None:
        """Lazy-initialize Web3 connections."""
        try:
            from web3 import Web3
            self._connections["ETH"] = Web3(Web3.HTTPProvider(settings.ethereum_rpc_url))
            self._connections["BSC"] = Web3(Web3.HTTPProvider(settings.bsc_rpc_url))
            self._connections["POLYGON"] = Web3(Web3.HTTPProvider(settings.polygon_rpc_url))
            self._initialized = True
            logger.info("Web3Client: connected to %d chains", len(self._connections))
        except ImportError:
            logger.warning("web3 not installed — running in simulation mode")
        except Exception as e:
            logger.error("Web3Client init failed: %s", e)

    def get_eth_balance(self, address: str, chain: str = "ETH") -> Optional[float]:
        if not self._initialized:
            return None
        try:
            w3 = self._connections.get(chain)
            if w3:
                wei = w3.eth.get_balance(address)
                return float(w3.from_wei(wei, "ether"))
        except Exception as e:
            logger.error("get_eth_balance error: %s", e)
        return None

    def get_gas_price_gwei(self, chain: str = "ETH") -> float:
        if not self._initialized:
            return 45.0  # Mock
        try:
            from web3 import Web3
            w3 = self._connections.get(chain)
            if w3:
                return float(Web3.from_wei(w3.eth.gas_price, "gwei"))
        except Exception:
            pass
        return 45.0

    def is_connected(self, chain: str = "ETH") -> bool:
        w3 = self._connections.get(chain)
        if w3 is None:
            return False
        try:
            return w3.is_connected()
        except Exception:
            return False


web3_client = Web3Client()

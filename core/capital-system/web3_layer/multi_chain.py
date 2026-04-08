from web3 import Web3
from config.config import ETH_RPC, BASE_RPC, WALLET_ADDRESS

eth = Web3(Web3.HTTPProvider(ETH_RPC))
base = Web3(Web3.HTTPProvider(BASE_RPC))

def get_balances():
    return {
        'ETH': eth.eth.get_balance(WALLET_ADDRESS),
        'BASE': base.eth.get_balance(WALLET_ADDRESS)
    }

def decide_route(asset,amount,chain):
    if amount<5: return None
    return 'BRIDGE_TO_ETH' if chain=='BASE' else 'SEND'

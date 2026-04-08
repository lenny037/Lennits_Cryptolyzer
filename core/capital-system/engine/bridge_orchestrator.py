from core.bridge_router import select_bridge
def execute_bridge(asset,amount,fc,tc,adapters):
    p=select_bridge(list(adapters.keys()))
    return adapters[p].bridge(asset,amount,fc,tc)

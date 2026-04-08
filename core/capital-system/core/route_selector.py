from core.liquidity_optimizer import score_route
def select_best(routes):
    return sorted(routes,key=score_route)[0] if routes else None

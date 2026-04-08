def score_route(r):
    return r.get('gas',1)+r.get('slippage',1)+r.get('time',1)

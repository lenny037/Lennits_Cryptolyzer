
import numpy as np

def predict_price(prices):
    prices = np.array(prices)
    trend = np.polyfit(range(len(prices)), prices, 1)
    prediction = prices[-1] + trend[0]
    return float(prediction)

if __name__ == "__main__":
    data=[10,11,12,13,14]
    print("Next price prediction:",predict_price(data))

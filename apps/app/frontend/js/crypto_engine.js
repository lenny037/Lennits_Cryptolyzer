/**
 * MODULE 18: CryptoEngine — Client-side signal processing and price feeds.
 * Real-time price ticker and signal strength calculation.
 */

"use strict";

window.CryptoEngine = {
  _prices: {},
  _callbacks: [],

  /**
   * Subscribe to price updates.
   * @param {function} cb - Callback receiving { symbol, price, change24h }
   */
  onPriceUpdate(cb) {
    this._callbacks.push(cb);
  },

  /**
   * Compute RSI from a price array.
   * @param {number[]} prices
   * @param {number}   period
   * @returns {number} RSI 0-100
   */
  computeRSI(prices, period = 14) {
    if (prices.length < period + 1) return 50;
    let gains = 0, losses = 0;
    for (let i = prices.length - period; i < prices.length; i++) {
      const delta = prices[i] - prices[i - 1];
      if (delta > 0) gains  += delta;
      else           losses -= delta;
    }
    const rs  = gains / (losses || 0.001);
    return 100 - 100 / (1 + rs);
  },

  /**
   * Simple signal from RSI.
   * @returns {"BUY"|"SELL"|"HOLD"}
   */
  signalFromRSI(rsi) {
    if (rsi < 30)  return "BUY";
    if (rsi > 70)  return "SELL";
    return "HOLD";
  },

  /**
   * Simulate live price feed (replace with CoinGecko / Binance WS in production).
   */
  startPriceFeed() {
    const basePrices = {
      BTC: 68420, ETH: 3340, SOL: 142.5, BNB: 560, MATIC: 0.88,
    };
    setInterval(() => {
      Object.entries(basePrices).forEach(([sym, base]) => {
        const price = base * (1 + (Math.random() - 0.5) * 0.004);
        const change24h = (Math.random() - 0.45) * 5;
        this._prices[sym] = { price, change24h };
        this._callbacks.forEach(cb => cb({ symbol: sym, price, change24h }));
      });
    }, 3000);
  },

  /**
   * Format currency value.
   */
  formatUSD(val) {
    return new Intl.NumberFormat("en-US", {
      style: "currency", currency: "USD", minimumFractionDigits: 2,
    }).format(val);
  },
};

// Auto-start price feed
window.addEventListener("load", () => window.CryptoEngine.startPriceFeed());

"""In-memory token-bucket rate limiter — MODULE 13: API Gateway."""
from __future__ import annotations

import time
from collections import defaultdict
from threading import Lock


class TokenBucket:
    """Thread-safe token bucket per client key."""

    def __init__(self, capacity: float = 100.0, refill_rate: float = 10.0) -> None:
        self._capacity = capacity
        self._refill_rate = refill_rate  # tokens / second
        self._buckets: dict[str, tuple[float, float]] = defaultdict(
            lambda: (capacity, time.monotonic())
        )
        self._lock = Lock()

    def consume(self, key: str, tokens: float = 1.0) -> bool:
        with self._lock:
            amount, last = self._buckets[key]
            now = time.monotonic()
            amount = min(self._capacity, amount + (now - last) * self._refill_rate)
            if amount >= tokens:
                self._buckets[key] = (amount - tokens, now)
                return True
            self._buckets[key] = (amount, now)
            return False


# Singleton
rate_limiter = TokenBucket(capacity=60.0, refill_rate=1.0)

"""MODULE 06: Notification Service — event broadcasting."""
from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import List


@dataclass
class Notification:
    level: str       # INFO | WARN | ALERT | PROFIT
    message: str
    detail: str = ""
    timestamp: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    id: str = field(default_factory=lambda: str(int(datetime.now().timestamp() * 1000)))


class NotificationService:
    def __init__(self, max_queue: int = 500) -> None:
        self._queue: List[Notification] = []
        self._max = max_queue

    def push(self, level: str, message: str, detail: str = "") -> Notification:
        n = Notification(level=level, message=message, detail=detail)
        self._queue.insert(0, n)
        if len(self._queue) > self._max:
            self._queue.pop()
        return n

    def get_all(self, limit: int = 50) -> List[Notification]:
        return self._queue[:limit]

    def clear(self) -> None:
        self._queue.clear()


notification_service = NotificationService()

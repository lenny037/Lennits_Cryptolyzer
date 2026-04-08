"""Utility functions for Lennit Bet Analyzer."""
from __future__ import annotations

from datetime import datetime
from typing import Any


def json_serializer(obj: Any):
    """JSON serializer for objects not serializable by default.

    Args:
        obj (Any): Object to serialize.

    Returns:
        A JSON-serializable representation of the object.

    Raises:
        TypeError: If the object cannot be serialized.
    """
    if isinstance(obj, datetime):
        return obj.isoformat()
    raise TypeError(f"Type {type(obj)} not serializable")

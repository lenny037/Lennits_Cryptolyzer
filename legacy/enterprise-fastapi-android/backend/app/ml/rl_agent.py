"""MODULE 17: RL Trading Agent — Q-learning portfolio optimizer."""
from __future__ import annotations

import random
from collections import deque
from dataclasses import dataclass, field
from typing import List, Tuple

import numpy as np
from app.core.logger import get_logger

logger = get_logger(__name__)

ACTION_HOLD = 0
ACTION_BUY  = 1
ACTION_SELL = 2


@dataclass
class Experience:
    state: np.ndarray
    action: int
    reward: float
    next_state: np.ndarray
    done: bool


class QTable:
    """Tabular Q-learning — suitable for discretized state spaces."""

    def __init__(
        self,
        state_size: int = 10,
        action_size: int = 3,
        learning_rate: float = 0.001,
        discount: float = 0.95,
        epsilon: float = 1.0,
        epsilon_decay: float = 0.995,
        epsilon_min: float = 0.01,
    ) -> None:
        self.state_size    = state_size
        self.action_size   = action_size
        self.lr            = learning_rate
        self.gamma         = discount
        self.epsilon       = epsilon
        self.epsilon_decay = epsilon_decay
        self.epsilon_min   = epsilon_min
        self.q_table       = np.zeros((2 ** state_size, action_size))
        self._memory: deque = deque(maxlen=10_000)
        logger.info("RL Agent: Q-table (%dx%d) initialized", 2**state_size, action_size)

    def _discretize(self, state: np.ndarray) -> int:
        """Map continuous state to discrete bucket index."""
        binary = (state > 0).astype(int)
        return int("".join(map(str, binary[-self.state_size:])), 2)

    def act(self, state: np.ndarray) -> int:
        if random.random() <= self.epsilon:
            return random.randrange(self.action_size)
        idx = self._discretize(state)
        return int(np.argmax(self.q_table[idx]))

    def remember(self, exp: Experience) -> None:
        self._memory.append(exp)

    def replay(self, batch_size: int = 64) -> float:
        if len(self._memory) < batch_size:
            return 0.0
        batch = random.sample(list(self._memory), batch_size)
        total_loss = 0.0
        for exp in batch:
            idx  = self._discretize(exp.state)
            nidx = self._discretize(exp.next_state)
            target = exp.reward
            if not exp.done:
                target += self.gamma * np.max(self.q_table[nidx])
            error = target - self.q_table[idx, exp.action]
            self.q_table[idx, exp.action] += self.lr * error
            total_loss += error ** 2
        if self.epsilon > self.epsilon_min:
            self.epsilon *= self.epsilon_decay
        return total_loss / batch_size


rl_agent = QTable()

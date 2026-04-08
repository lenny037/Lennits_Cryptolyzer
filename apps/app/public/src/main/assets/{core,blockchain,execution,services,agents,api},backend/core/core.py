
import time
from modules.memory import Memory
from modules.orchestrator import Orchestrator

def run():
    mem = Memory()
    orch = Orchestrator(mem)
    print("[SYSTEM] Lennit Suite v8 starting")

    while True:
        task = mem.next()
        if not task:
            time.sleep(1)
            continue

        result = orch.execute(task)
        mem.save(task, result)
        time.sleep(0.5)

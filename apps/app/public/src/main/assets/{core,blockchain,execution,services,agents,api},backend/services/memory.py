
import json
from pathlib import Path

class Memory:
    def __init__(self):
        self.f = Path("memory.json")
        if not self.f.exists():
            self.f.write_text(json.dumps({"tasks":["status"], "log":[]}, indent=2))

    def next(self):
        data = json.loads(self.f.read_text())
        if data["tasks"]:
            return data["tasks"].pop(0)
        return None

    def save(self, task, result):
        data = json.loads(self.f.read_text())
        data["log"].append({"task":task,"result":result})
        self.f.write_text(json.dumps(data, indent=2))

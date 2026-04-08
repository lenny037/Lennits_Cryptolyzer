
from fastapi import FastAPI
from modules.queue import QueueBackend

app = FastAPI()
q = QueueBackend()

@app.get("/health")
def health():
    return {"status":"ok","runtime":"prod-v1"}

@app.post("/task")
def add_task(task: str):
    data = q.f.read_text()
    import json
    obj = json.loads(data)
    obj["tasks"].append(task)
    q.f.write_text(json.dumps(obj, indent=2))
    return {"queued": task}

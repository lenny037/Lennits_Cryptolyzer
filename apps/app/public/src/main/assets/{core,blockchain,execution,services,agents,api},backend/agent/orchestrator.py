
from modules.planner import plan
from modules.tools import tool_registry

class Orchestrator:
    def __init__(self, memory):
        self.memory = memory

    def execute(self, task):
        plan_steps = plan(task)

        results = []
        for step in plan_steps:
            tool = step.get("tool")
            payload = step.get("payload")
            fn = tool_registry().get(tool)
            if fn:
                results.append(fn(payload))
            else:
                results.append({"error":"unknown tool", "tool":tool})

        return {"task": task, "steps": results}

import asyncio
from app.agents.execution_agent import ExecutionAgent

class Orchestrator:
    async def run(self):
        await asyncio.gather(ExecutionAgent().run())

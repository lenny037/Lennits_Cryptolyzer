
import subprocess
import json
import requests

class LocalLLM:
    def __init__(self, mode="auto"):
        self.mode = mode

    def generate(self, prompt):
        # Try Ollama first
        try:
            r = requests.post(
                "http://localhost:11434/api/generate",
                json={"model":"llama3","prompt":prompt,"stream":False},
                timeout=5
            )
            if r.status_code == 200:
                return r.json().get("response","")
        except:
            pass

        # fallback deterministic brain
        return f"[LLM-FALLBACK] {prompt[:200]}"

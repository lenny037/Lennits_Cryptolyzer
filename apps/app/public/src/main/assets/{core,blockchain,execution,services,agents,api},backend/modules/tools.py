
import subprocess
import requests

def status(_):
    return {"status":"ok","layer":"v8"}

def echo(payload):
    return {"echo": payload.get("msg")}

def http_get(payload):
    try:
        r = requests.get(payload["url"], timeout=5)
        return {"code": r.status_code, "len": len(r.text)}
    except Exception as e:
        return {"error": str(e)}

def shell(payload):
    cmd = payload.get("cmd")
    try:
        out = subprocess.check_output(cmd, shell=True, timeout=5).decode()
        return {"output": out[:200]}
    except Exception as e:
        return {"error": str(e)}

def tool_registry():
    return {
        "status": status,
        "echo": echo,
        "http_get": http_get,
        "shell": shell
    }

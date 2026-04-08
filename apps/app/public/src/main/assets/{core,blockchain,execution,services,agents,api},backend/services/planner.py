
def plan(task):
    t = task.lower()
    if "status" in t:
        return [{"tool":"status","payload":{}}]
    if "http" in t:
        return [{"tool":"http_get","payload":{"url":"https://example.com"}}]
    return [{"tool":"echo","payload":{"msg":task}}]

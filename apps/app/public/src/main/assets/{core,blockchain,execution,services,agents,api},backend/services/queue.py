
class Queue:
    def __init__(self):
        self.items = []

    def push(self, x):
        self.items.append(x)

    def pop(self):
        if self.items:
            return self.items.pop(0)
        return None

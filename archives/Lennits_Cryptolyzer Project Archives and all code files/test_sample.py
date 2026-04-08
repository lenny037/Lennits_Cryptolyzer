cat > test_sample.py << 'EOF'
def func(x):
    return x + 1

def test_answer():
    assert func(3) == 4
EOF
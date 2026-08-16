from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[3]
WRAPPER = ROOT / "cpf-tools/verification/nxt3/cpf_nxt3_korean_comment_gate.py"
FULL = ROOT / "cpf-tools/verification/nxt3/verify_nxt3_korean_comment.py"

def run(path: Path):
    return subprocess.run([sys.executable, str(path), "--root", str(ROOT), "--self-test"], text=True, capture_output=True, check=False)

def test_wrapper_delegates_to_full_canonical_verifier():
    text = WRAPPER.read_text(encoding="utf-8")
    assert "from verify_nxt3_korean_comment import main" in text
    wrapper = run(WRAPPER)
    full = run(FULL)
    assert wrapper.returncode == full.returncode == 0
    assert wrapper.stdout == full.stdout

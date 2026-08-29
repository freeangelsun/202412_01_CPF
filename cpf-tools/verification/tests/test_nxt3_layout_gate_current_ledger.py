from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
GATE = ROOT / "cpf-tools/verification/nxt3/cpf_nxt3_layout_gate.py"
SCOPE = ROOT / "cpf-tools/verification/tools/verify-cpf-execution-scope-exhaustive.py"


def test_nxt3_layout_gate_uses_current_garbage_decisions_as_default():
    text = GATE.read_text(encoding="utf-8")
    assert "default='cpf-docs/governance/development-harness/current/CURRENT_GARBAGE_DECISIONS.csv'" in text
    assert "--garbage-ledger',default='cpf-docs/governance/development-harness/current/CURRENT_DEVELOPMENT_STATUS.csv'" not in text


def test_repository_hygiene_scope_includes_status_garbage_and_delete_manifest():
    text = SCOPE.read_text(encoding="utf-8")
    line = next(line for line in text.splitlines() if '"REPOSITORY HYGIENE"' in line)
    assert "CURRENT_DEVELOPMENT_STATUS.csv" in line
    assert "CURRENT_GARBAGE_DECISIONS.csv" in line
    assert "DELETE_MANIFEST.csv" in line

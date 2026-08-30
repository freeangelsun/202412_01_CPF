"""Development Harness Python cache hygiene regression contract."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
AUTHORITY_GATE = (
    ROOT
    / "cpf-docs/governance/development-harness/validators/validate_harness_authority.py"
)
RUN_ALL_GATES = ROOT / "cpf-docs/governance/development-harness/validators/run_all_gates.py"


def test_authority_gate_cannot_create_the_python_cache_it_rejects():
    text = AUTHORITY_GATE.read_text(encoding="utf-8")
    assert "child_env['PYTHONDONTWRITEBYTECODE']='1'" in text
    assert "[sys.executable,'-B',str(source_state)" in text
    assert "env=child_env" in text


def test_all_harness_gate_children_disable_bytecode_generation():
    text = RUN_ALL_GATES.read_text(encoding="utf-8")
    assert "subprocess.run([py,'-B',str(H/s)]" in text
    assert "subprocess.Popen([py,'-B',str(H/s)]" in text

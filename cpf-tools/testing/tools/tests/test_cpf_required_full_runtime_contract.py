from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/verification/tools/run-cpf-required-full-runtime-validation.ps1"

def test_required_runtime_wrapper_enforces_maximum_contract():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "'-FullLocal','-IncludePerformanceLoad','-AllowDestructiveDbRollback','-StrictExit'" in text
    assert "Node >=22.18.0 <25" in text
    assert "npm 10.9.2" in text
    assert "[Console]::OutputEncoding" in text
    assert "PYTHONIOENCODING='utf-8'" in text
    assert "verify-cpf-vscode-problems.py" in text
    assert "Invoke-RequiredPass 'FRESH_REPLAY'" in text
    assert "CPF_OPEN_GIT_REMOTE" not in text  # owned by FullLocal stage

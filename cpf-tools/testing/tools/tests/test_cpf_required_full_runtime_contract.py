from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/verification/tools/run-cpf-required-full-runtime-validation.ps1"

def test_required_runtime_wrapper_enforces_maximum_contract():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "FullLocal=$true" in text
    assert "IncludePerformanceLoad=$true" in text
    assert "AllowDestructiveDbRollback=$true" in text
    assert "StrictExit=$true" in text
    assert "Node >=22.18.0 <25" in text
    assert "npm 10.9.2" in text
    assert "[Console]::OutputEncoding" in text
    assert "PYTHONIOENCODING='utf-8'" in text
    assert "-Dstdout.encoding=UTF-8" in text
    assert "& $runner @runnerArgs *>&1" in text
    assert "& pwsh @baseArgs" not in text
    assert "verify-cpf-vscode-problems.py" in text
    assert "Invoke-RequiredPass 'FRESH_REPLAY'" in text
    assert "function Assert-NoMojibake" in text
    assert "UTF-8 mojibake detected after" in text
    assert "[char]0xFFFD" in text
    assert "CPF_OPEN_GIT_REMOTE" not in text  # owned by FullLocal stage

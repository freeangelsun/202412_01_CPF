from __future__ import annotations

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/verification/tools/verify-event-dlq-contract.py"


def _run(*args: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        [sys.executable, str(SCRIPT), *args],
        cwd=ROOT,
        text=True,
        capture_output=True,
        check=False,
    )


def test_long_root_option_is_supported() -> None:
    result = _run("--root", str(ROOT))
    assert result.returncode == 0, result.stdout + result.stderr
    assert "PASS EVENT-DLQ" in result.stdout


def test_positional_root_remains_supported() -> None:
    result = _run(str(ROOT))
    assert result.returncode == 0, result.stdout + result.stderr
    assert "PASS EVENT-DLQ" in result.stdout


def test_wrong_orval_http_method_fails_closed(tmp_path: Path) -> None:
    required = [
        "cpf-starters/messaging/reliability/jdbc/src/main/java/com/cpf/messaging/reliability/api/jdbc/CpfBrokerReliabilityOperations.java",
        "cpf-starters/messaging/reliability/jdbc/src/main/java/com/cpf/messaging/reliability/api/jdbc/internal/JdbcCpfBrokerReliabilityRepository.java",
        "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmReliabilityController.java",
        "cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmReliabilityActionRequest.java",
        "cpf-admin/frontend/openapi/cpf-openapi.json",
        "cpf-admin/frontend/src/generated/cpf-api.ts",
        "cpf-admin/frontend/src/generated/orval/cpf-api.ts",
    ]
    for rel in required:
        src = ROOT / rel
        dst = tmp_path / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_bytes(src.read_bytes())
    orval = tmp_path / "cpf-admin/frontend/src/generated/orval/cpf-api.ts"
    text = orval.read_text(encoding="utf-8")
    start = text.index("export const requestAdmBrokerDlqReplay")
    end = text.index("// CPF PRE-RUNTIME FALLBACK END requestAdmBrokerDlqReplay", start)
    block = text[start:end].replace("method: 'POST'", "method: 'GET'")
    orval.write_text(text[:start] + block + text[end:], encoding="utf-8")
    result = _run("--root", str(tmp_path))
    assert result.returncode != 0

import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

import pytest

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/verification/tools/verify-cpf-java21-substitute-validation.py"


def _java_major() -> int | None:
    java = shutil.which("java")
    javac = shutil.which("javac")
    if not java or not javac:
        return None
    completed = subprocess.run([javac, "-version"], text=True, capture_output=True, check=False)
    text = completed.stdout + completed.stderr
    for token in text.split():
        if token and token[0].isdigit():
            return int(token.split(".", 1)[0])
    return None


def test_java21_substitute_compiles_current_batch_runtime_controller_contract():
    if _java_major() != 21:
        pytest.skip("Java 21 substitute contract is executed only when Java 21 is available")
    with tempfile.TemporaryDirectory(prefix="cpf-java21-contract-test-") as temp:
        output = Path(temp) / "result.json"
        completed = subprocess.run(
            [sys.executable, str(SCRIPT), "--repository-root", str(ROOT), "--output", str(output)],
            text=True,
            capture_output=True,
            check=False,
        )
        assert completed.returncode == 0, completed.stdout + completed.stderr
        payload = json.loads(output.read_text(encoding="utf-8"))
        assert payload["status"] == "PASS"
        assert payload["classFileMajor"] == 65
        assert "JAVA21_UNIT_TEST_PASS count=2" in payload["unitTest"]["stdout"]
        assert "JAVA21_RUNTIME_HARNESS_PASS" in payload["runtimeHarness"]["stdout"]

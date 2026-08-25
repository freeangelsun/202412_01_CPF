#!/usr/bin/env python3
from __future__ import annotations

import json
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-backoffice-web/frontend/scripts/verify-runtime-openapi-parity.mjs"
SOURCE = ROOT / "cpf-backoffice-web/frontend/openapi/cpf-openapi.json"


def test_missing_runtime_openapi_fails_closed_without_directory_read_error() -> None:
    cp = subprocess.run(["node", str(SCRIPT), str(SOURCE)], cwd=ROOT, text=True, capture_output=True, check=False)
    assert cp.returncode != 0
    combined = cp.stdout + cp.stderr
    assert "runtime OpenAPI missing: path was not provided" in combined
    assert "EISDIR" not in combined


def test_backoffice_uppercase_module_runtime_parity_passes_for_identical_runtime_contract() -> None:
    source = json.loads(SOURCE.read_text(encoding="utf-8"))
    runtime = json.loads(json.dumps(source))
    runtime["x-cpf-export-origin"] = "BACKEND_RUNTIME"
    runtime["x-cpf-release-eligible"] = True
    with tempfile.TemporaryDirectory(prefix="cpf-mbw-openapi-") as td:
        runtime_path = Path(td) / "runtime-openapi.json"
        runtime_path.write_text(json.dumps(runtime), encoding="utf-8")
        cp = subprocess.run(
            ["node", str(SCRIPT), str(SOURCE), str(runtime_path)],
            cwd=ROOT,
            text=True,
            capture_output=True,
            check=False,
        )
    assert cp.returncode == 0, cp.stdout + cp.stderr
    assert "[CPF][BACKOFFICE][OPENAPI][RUNTIME-PARITY][PASS]" in cp.stdout

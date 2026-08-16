from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/contracts/openapi/export-cpf-backend-openapi.ps1"


def test_runtime_export_is_git_independent_and_never_overwrites_source_snapshot():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "git -C" not in text
    assert "status --porcelain" not in text
    assert "BASE_SHA.txt" in text
    assert "PACKAGE_MANIFEST.json" in text
    assert "Runtime OpenAPI output cannot overwrite tracked CONTROLLER_SOURCE_PRE_RUNTIME OpenAPI" in text
    assert "UpdateSnapshot" in text and "must not be replaced by BACKEND_RUNTIME" in text


def test_runtime_export_enforces_release_parity_and_controller_coverage():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "canonicalize-cpf-openapi.py" in text
    assert "--scope=release" in text
    assert "verify-runtime-openapi-parity.mjs" in text
    assert "verify-cpf-openapi-controller-coverage.py" in text
    assert "runtimeSourceParity = $true" in text
    assert "controllerCoverage = $true" in text

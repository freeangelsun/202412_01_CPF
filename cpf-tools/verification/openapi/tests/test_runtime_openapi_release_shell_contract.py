from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/contracts/openapi/verify-cpf-runtime-openapi-release.ps1"


def test_runtime_openapi_release_is_git_independent_and_read_only():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "cpf-source-state.py" in text
    assert "GIT_INDEPENDENT_CONTENT_SHA1" in text
    assert "git rev-parse" not in text
    assert "git status" not in text
    assert "mutated product source bytes" in text


def test_runtime_openapi_release_covers_export_release_validation_and_parity():
    text = SCRIPT.read_text(encoding="utf-8")
    for token in (
        "/v3/api-docs",
        "canonicalize-cpf-openapi.py",
        "--release",
        "--scope=release",
        "verify-cpf-openapi-controller-coverage.py",
        "verify-runtime-openapi-parity.mjs",
        "x-cpf-canonical-schema-version",
        "x-cpf-release-eligible",
    ):
        assert token in text

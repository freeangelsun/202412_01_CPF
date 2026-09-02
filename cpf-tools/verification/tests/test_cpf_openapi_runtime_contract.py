from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/verification/openapi/smoke-openapi.ps1"


def text() -> str:
    return SCRIPT.read_text(encoding="utf-8-sig")


def test_openapi_verifier_accepts_standard_operation_level_tags_without_weakening_contract_checks():
    """Top-level OpenAPI tags are optional; operation tags remain mandatory evidence when required."""
    source = text()
    assert "function Get-JsonProperty" in source
    assert "function Get-OpenApiTagNames" in source
    assert 'Get-JsonProperty -Value $ApiDocs -Name "tags" -Default @()' in source
    assert 'Get-JsonProperty -Value $operation -Name "tags" -Default @()' in source
    assert '$tagNames = @(Get-OpenApiTagNames -ApiDocs $apiDocs)' in source
    assert "Required OpenAPI tags or paths are missing." in source
    assert "$apiDocs.tags" not in source
    assert "$apiDocs.openapi" not in source


def test_openapi_verifier_handles_missing_optional_json_fields_under_strict_mode():
    source = text()
    assert "Set-StrictMode -Version Latest" in source
    assert '$Value.PSObject.Properties[$Name]' in source
    assert '$paths = Get-JsonProperty -Value $ApiDocs -Name "paths"' in source
    assert "$serviceResult.pathCount = if ($null -eq $paths) { 0 }" in source

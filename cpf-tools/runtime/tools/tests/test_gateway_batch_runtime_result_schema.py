from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/"cpf-tools/runtime/tools/smoke-gateway-bat-runtime.ps1"
def test_gateway_batch_runtime_harvests_optional_failure_fields_without_strictmode_crash():
    text=SCRIPT.read_text(encoding="utf-8")
    assert "function Get-CpfOptionalRuntimeProperty" in text
    for field in ("stdout","stderr","stdoutTail","stderrTail","failureRootCause","failureClassification"):
        assert f'Get-CpfOptionalRuntimeProperty $_ "{field}"' in text
        assert f'$_.{field}' not in text

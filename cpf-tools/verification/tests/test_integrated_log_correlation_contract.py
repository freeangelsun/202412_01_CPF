from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / 'cpf-tools/runtime/tools/smoke-integrated-log-correlation.ps1'


def test_integrated_log_correlation_is_fail_closed_and_secret_safe():
    text = SCRIPT.read_text(encoding='utf-8')
    for token in (
        'X-Transaction-Id', 'X-Trace-Id', '/api/education/query/headers',
        '/adm/api/logs?transactionId=', '/adm/api/observability/transactions/',
        '/adm/api/transaction-groups/', '/adm/api/observability/file-log-recovery',
        'rawSecretLeakCount', 'terminalLoss', 'quarantined', 'transactionLogs',
    ):
        assert token in text
    assert "CPF_ADM_SMOKE_PASSWORD" in text
    assert "password is read" not in text.lower()  # no user-facing echo of credential
    assert '$admPassword)' not in text.split('Write-Host')[-1]
    assert "status='FAIL'" in text


def test_integrated_log_correlation_uses_safe_property_access_for_optional_evidence():
    text = SCRIPT.read_text(encoding='utf-8')
    assert 'function Get-SafeProperty' in text
    assert 'Read-JsonIfPresent' in text
    assert "Get-SafeProperty $fileEvidence 'error'" in text

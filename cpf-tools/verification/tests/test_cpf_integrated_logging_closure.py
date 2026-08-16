from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[3]
FULL = ROOT / 'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1'
CORRELATION = ROOT / 'cpf-tools/runtime/tools/smoke-integrated-log-correlation.ps1'
VERIFY = ROOT / 'cpf-tools/verification/tools/verify-cpf-integrated-logging-closure.py'


def test_integrated_logging_static_closure_passes():
    completed = subprocess.run(
        [sys.executable, str(VERIFY), '--root', str(ROOT)],
        cwd=ROOT,
        text=True,
        capture_output=True,
        check=False,
    )
    assert completed.returncode == 0, completed.stdout + completed.stderr
    assert 'CPF_INTEGRATED_LOGGING_CLOSURE=PASS' in completed.stdout


def test_full_local_executes_logging_runtime_after_one_was_start_and_before_stop():
    source = FULL.read_text(encoding='utf-8-sig')
    stages = (
        'CODEX_INTEGRATED_LOGGING_CLOSURE',
        'LOCAL_FILE_LOG_STANDARD',
        'LOCAL_DB_LOG_POLICY_RUNTIME',
        'LOCAL_INTEGRATED_LOG_CORRELATION',
    )
    for stage in stages:
        assert stage in source
    assert source.index('LOCAL_ONE_WAS_START') < source.index('LOCAL_FILE_LOG_STANDARD')
    assert source.index('LOCAL_FILE_LOG_STANDARD') < source.index('LOCAL_DB_LOG_POLICY_RUNTIME')
    assert source.index('LOCAL_DB_LOG_POLICY_RUNTIME') < source.index('LOCAL_INTEGRATED_LOG_CORRELATION')
    assert source.index('LOCAL_INTEGRATED_LOG_CORRELATION') < source.index('LOCAL_ONE_WAS_STOP')


def test_full_local_does_not_put_adm_password_in_logged_command_arguments():
    source = FULL.read_text(encoding='utf-8-sig')
    assert "'-AdmPassword',$admSmokePassword" not in source
    assert "[Environment]::SetEnvironmentVariable('CPF_ADM_SMOKE_PASSWORD',$adminPassword,'Process')" in source
    assert "[Environment]::SetEnvironmentVariable('CPF_ADM_APPROVAL_PROOF_KEY_BASE64'" in source


def test_integrated_runtime_correlation_is_transaction_and_secret_fail_closed():
    source = CORRELATION.read_text(encoding='utf-8-sig')
    for token in (
        'fileLogDbCorrelation', 'fileLogRecovery', 'processRuntimeLog', 'secretLeakScan',
        'transactionId', 'traceId', 'terminalLoss', 'quarantined', 'pending', 'writeFailureCount',
        'AdmPassword', 'accessToken', 'CPF_ADM_APPROVAL_PROOF_KEY_BASE64', 'Raw credential/token found',
        'APPLICATION FAILED TO START', 'OutOfMemoryError', 'BeanCreationException',
    ):
        assert token in source, token

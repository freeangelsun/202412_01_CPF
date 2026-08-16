from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1"


def test_two_worker_runtime_supports_docker_mariadb_client_without_secret_in_argv():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "ValidateSet('Auto','Host','Docker')" in text
    assert "MariaDbContainer = 'cpf-mariadb'" in text
    assert "MARIADB_ROOT_PASSWORD" in text
    assert "docker" in text.lower()
    assert "--password=" not in text
    assert "CPF_DB_ROOT_PASSWORD or CPF_ADMIN_PASSWORD is required" in text


def test_two_worker_runtime_still_proves_crash_unknown_and_no_blind_retry():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "UNKNOWN_RESULT" in text
    assert "UNKNOWN_RESULT was blindly retried" in text
    assert "blindRetryCount=0" in text
    assert "distinctWorkers=2" in text

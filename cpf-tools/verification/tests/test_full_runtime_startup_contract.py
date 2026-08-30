"""Full Runtime 기동/환경탐지 fail-closed 계약.

실제 FullLocal 실행에서 다음 두 결함이 Mandatory 16건(FAIL 12 + NOT_EXECUTED 4)을 만들었다.

1) Java 25 capability 탐지가 자기모순이었다.
   `run-cpf-local-full-validation.ps1`은 자식 JVM의 한글 인코딩을 보장하려고
   `JAVA_TOOL_OPTIONS`를 설정한다. 그런데 JVM launcher는 그 변수가 있으면 stderr에
   `Picked up JAVA_TOOL_OPTIONS: ...` 알림을 낸다. capability probe가 `2>&1`로 합친 출력을
   결과 문자열과 그대로 비교해, javac/java가 정상(exit 0)인데도 항상 NOT_FOUND가 되었고
   CLI 빌드·TESTING_TOOLS·Runtime·Browser 단계까지 연쇄로 무너졌다.

2) 로컬 Runtime이 readiness를 기다리지 않았다.
   `cpf_local_runtime.py`의 start는 `Popen` 직후 곧바로 `CPF_LOCAL_RUNTIME=STARTED`를
   출력하고 0을 반환했다. 그래서 (a) 뒤따르는 Runtime 검증이 아직 기동 중인 앱에 접속해
   연쇄 실패하고, (b) 앱이 기동에 실패해도 START 단계가 PASS로 남아 fail-closed가 깨졌다.
"""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
FULL_VALIDATION = ROOT / "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"
LOCAL_RUNTIME = ROOT / "cpf-tools/runtime/tools/cpf_local_runtime.py"


def _text(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig")


def test_java_capability_probe_ignores_jvm_launcher_option_notices():
    text = _text(FULL_VALIDATION)
    # 검증기 자신이 UTF-8 강제를 위해 JAVA_TOOL_OPTIONS를 설정한다는 전제가 유지되어야 한다.
    assert "$env:JAVA_TOOL_OPTIONS" in text
    # probe는 그 알림을 결과로 오인하면 안 된다.
    assert "Picked up (JAVA_TOOL_OPTIONS|_JAVA_OPTIONS|JDK_JAVA_OPTIONS)" in text
    assert "CPF_JAVA25_CAPABILITY=PASS" in text
    # exit code는 출력 필터링 이전 값으로 판정해야 한다.
    assert "$runExit=$LASTEXITCODE" in text


def test_local_runtime_start_waits_for_readiness_and_fails_closed():
    text = _text(LOCAL_RUNTIME)
    assert "def wait_until_ready(" in text
    assert "def port_listening(" in text
    assert "readiness=wait_until_ready(" in text
    # 준비되지 않으면 STARTED를 선언하지 않고 실패로 끝나야 한다.
    ready_at = text.index("readiness=wait_until_ready(")
    started_at = text.index("CPF_LOCAL_RUNTIME=STARTED pid=")
    assert ready_at < started_at, "readiness 확인이 STARTED 선언보다 뒤에 있다"
    assert "did not become ready" in text
    # 기동 중 죽은 프로세스를 성공으로 처리하면 안 된다.
    assert "EXITED:" in text


def test_local_runtime_ready_timeout_is_policy_owned_not_hardcoded():
    text = _text(LOCAL_RUNTIME)
    assert "CPF_LOCAL_RUNTIME_READY_TIMEOUT_SECONDS" in text
    assert "runtime.startup.readyTimeoutSeconds" in text

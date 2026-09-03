"""`70. cpf 오픈깃 릴리즈` 사용자 Task 계약을 검증한다(Harness §29).

Open Git Release 는 최종 사용자가 직접 수행하는 공식 lifecycle 이다. 다음을 고정한다.

1. 생성 / 검증 / 준비 / Commit / Push 의 **책임 경계**가 Task 로 분리되어 있다.
2. Gradle 이 Release 로직을 복제하지 않고 **정본 진입점을 호출하는 wrapper** 다.
3. Git Write 는 명시적 승인(`-PconfirmGitWrite=true`) 없이는 수행되지 않는다.
4. Build / Verify / Prepare 는 어떤 경로로도 Git 을 변경하지 않는다.
5. 사용자 Task 영역(70)에 노출되고 90~99 내부 영역으로 숨겨지지 않는다.
"""
from __future__ import annotations

import io
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CONVENTIONS = ROOT / "cpf-tools/build/cpf-root-conventions.gradle"
WRAPPER = ROOT / "cpf-tools/release/open-git/cpf-open-git.ps1"
ENGINE = ROOT / "cpf-tools/release/open-git/cpf_open_git.py"
GROUP = "70. CPF 오픈깃 릴리즈"

BUILD_TASKS = ("cpfOpenGitBuild", "cpfOpenGitVerify", "cpfOpenGitStatus", "cpfOpenGitPrepare")
GIT_WRITE_TASKS = ("cpfOpenGitCommit", "cpfOpenGitPush", "cpfOpenGitCommitAndPush")


@lru_cache(maxsize=3)
def _text(path: str) -> str:
    return io.open(ROOT / path, encoding="utf-8-sig").read()


def _conventions() -> str:
    return _text("cpf-tools/build/cpf-root-conventions.gradle")


def test_all_open_git_tasks_are_registered_in_the_user_group() -> None:
    text = _conventions()
    for task in BUILD_TASKS + GIT_WRITE_TASKS:
        assert f"tasks.register('{task}')" in text, task
        # 가시 그룹에 등록하지 않으면 뒤의 분류가 '내부 빌드/검증' 또는 '원시 명령' 으로 덮어써
        # 사용자 화면에서 사라진다. 실제로 그렇게 사라졌다.
        assert f"'{task}':'{GROUP}'" in text, f"{task} is not registered in the visible group"


def test_open_git_tasks_are_not_hidden_in_the_internal_group() -> None:
    text = _conventions()
    for task in BUILD_TASKS + GIT_WRITE_TASKS:
        assert f"'{task}':'90." not in text, task
        assert f"'{task}':'99." not in text, task


def test_gradle_wraps_the_canonical_entry_point_instead_of_duplicating_logic() -> None:
    text = _conventions()
    assert "cpf-tools/release/open-git/cpf-open-git.ps1" in text
    # Release 로직을 Gradle 에 복제하면 CLI/PowerShell/Gradle 구현이 갈라진다.
    assert "cpf_open_git.py" not in text.split("def cpfOpenGitGroup")[-1].split("qa34IntegrationTest")[0]
    assert WRAPPER.is_file() and ENGINE.is_file()


def test_git_write_tasks_require_explicit_approval() -> None:
    text = _conventions()
    assert "confirmGitWrite" in text
    # 승인값이 정확히 'true' 일 때만 통과해야 한다. 오타/누락은 모두 거부한다.
    assert "confirmed != 'true'" in text
    body = text.split("def cpfOpenGitExec")[1].split("tasks.register('cpfOpenGitBuild')")[0]
    assert "-ConfirmGitWrite" in body


def test_non_git_tasks_never_request_git_write() -> None:
    text = _conventions()
    for task in BUILD_TASKS:
        block = text.split(f"tasks.register('{task}')")[1].split("tasks.register(")[0]
        assert "cpfOpenGitRun('commit'" not in block, task
        assert "cpfOpenGitRun('push'" not in block, task
        assert ", true)" not in block, f"{task} must not request git write"


def test_prepare_only_builds_and_verifies() -> None:
    text = _conventions()
    block = text.split("tasks.register('cpfOpenGitPrepare')")[1].split("tasks.register(")[0]
    assert "cpfOpenGitBuild" in block and "cpfOpenGitVerify" in block
    for task in GIT_WRITE_TASKS:
        assert task not in block, f"prepare must not depend on {task}"


def test_canonical_engine_fails_closed_without_approval() -> None:
    engine = _text("cpf-tools/release/open-git/cpf_open_git.py")
    assert "def _require_git_write_approval(" in engine
    assert "--confirm-git-write" in engine
    for action in ("commit", "push"):
        assert f'"{action}"' in engine
    # Commit/Push 는 승인 확인을 **가장 먼저** 수행해야 한다.
    for function in ("def commit_release(", "def push_release("):
        body = engine.split(function)[1].split("\n\n")[0]
        assert "_require_git_write_approval(approved)" in body, function


def test_canonical_engine_preflight_checks_target_and_release_state() -> None:
    engine = _text("cpf-tools/release/open-git/cpf_open_git.py")
    body = engine.split("def _open_git_write_preflight(")[1].split("\ndef ")[0]
    for expectation in (
        "Development Master",          # 개발 Master 대상 차단
        "정본 경로가 아닙니다",          # Open Git 작업 Repository 확인
        "branch",                      # branch 확인
        "remote",                      # remote 확인
        "Leakage",                     # Leakage 0
        "sourceIdentitySha256",        # Source identity
        "PASS",                        # Release/Verify 결과
    ):
        assert expectation in body, expectation


def test_wrapper_exposes_every_action_and_the_approval_switch() -> None:
    wrapper = _text("cpf-tools/release/open-git/cpf-open-git.ps1")
    assert "ValidateSet('build','check','status','setup','commit','push')" in wrapper
    assert "[switch]$ConfirmGitWrite" in wrapper
    assert "'--confirm-git-write'" in wrapper
    # 실패 시 원인/다음 조치/Evidence/Log 위치를 사용자에게 알려야 한다(§27.4, §29.5).
    for expectation in ("CPF_OPEN_GIT=FAIL", "CPF_OPEN_GIT=PASS", "다음 조치", "Evidence", "Log"):
        assert expectation in wrapper, expectation
    # PowerShell 자동 변수와 겹치는 이름을 쓰지 않는다(경고 0 규칙).
    assert re.search(r"(?m)^\s*\[string\]\$Profile\b", wrapper) is None

"""generated garbage cleanup이 IDE classpath 산출물을 지우지 않는지 보장한다.

VS Code(JDT LS)의 project model은 각 project의 `build/classes/java/main` 과 `build/libs/*.jar` 를
직접 참조한다. 그런데 JDT LS의 file watcher는 `**/*.java`, `**/*.gradle` 같은 Source 패턴만
등록하고 **`build/**` 산출물은 감시하지 않는다**. 따라서 이 산출물이 지워지면
`missing required library` 오류가 뜨고, 다시 만들어져도 IDE가 그것을 감지하지 못해
Reload Window / Clean Java Language Server Workspace 같은 수동 초기화 전까지 오류가 남는다.

`cleanup-cpf-generated-garbage.ps1` 은 매 검증 사이클마다 실행되는데, 예전에는
(1) 이름이 `build` 인 디렉터리를 통째로 지우고
(2) 빈 디렉터리 정리 단계에서 source-empty project의 canonical compile output(의도적으로 비어 있다)까지 지웠다.
그래서 정상 개발 흐름만으로 IDE Problems가 반복해서 되살아났다.

`verify-cpf-clean-source-tree.py` 는 `build/` 를 검사 대상에서 제외하며, 그 주석이
`build/classes/java/main` 을 "IDE classpath 계약이 요구하는 정본 출력 위치" 로 명시한다.
즉 build/ 삭제는 어떤 게이트도 요구하지 않는 부수피해였다.
"""
from __future__ import annotations

import io
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CLEANUP = ROOT / "cpf-tools/release/tools/cleanup-cpf-generated-garbage.ps1"
SEPARATOR = "[IO.Path]::DirectorySeparatorChar"


@lru_cache(maxsize=1)
def _script() -> str:
    return io.open(CLEANUP, encoding="utf-8", errors="replace").read()


def test_cleanup_does_not_target_build_directories() -> None:
    # 디렉터리 삭제 대상 선정에 'build' 이름이 다시 들어오면 IDE classpath가 통째로 깨진다.
    offending = [
        line.strip() for line in _script().splitlines()
        if re.search(r"\$_\.Name\s*-eq\s*'build'", line)
    ]
    assert offending == [], (
        "cleanup은 build/ 디렉터리를 삭제 대상으로 삼을 수 없다"
        f" (IDE classpath 산출물이 사라진다): {offending}"
    )


def test_cleanup_empty_directory_pass_protects_build_outputs() -> None:
    # source-empty project의 canonical compile output은 의도적으로 비어 있다.
    # 빈 디렉터리라는 이유로 지우면 같은 오류가 재발한다.
    text = _script()
    empty_pass = text.split("$empty=", 1)
    assert len(empty_pass) == 2, "cleanup의 빈 디렉터리 정리 단계를 찾지 못했다"
    guard = empty_pass[1].split("|")[1] if "|" in empty_pass[1] else empty_pass[1]
    assert "'build'" in guard and SEPARATOR in guard, (
        "빈 디렉터리 정리 단계가 build/ 하위를 제외하지 않는다"
        f" (source-empty project의 compile output이 지워진다): {guard.strip()[:200]}"
    )


def test_clean_source_gate_still_excludes_build() -> None:
    # 위 두 계약의 근거다. clean-source가 build/를 검사하기 시작하면 판단 전제가 바뀐다.
    gate = ROOT / "cpf-tools/verification/tools/verify-cpf-clean-source-tree.py"
    text = io.open(gate, encoding="utf-8", errors="replace").read()
    assert "part=='build'" in text.replace(" ", ""), (
        "clean-source가 더 이상 build/를 제외하지 않는다면 cleanup 정책을 다시 판정해야 한다")

"""Python 진입점이 스스로 UTF-8 출력을 고정하는지 검증한다.

CPF 표준 인코딩은 UTF-8 이다. 그런데 Windows 콘솔 기본 코드페이지는 cp949 이므로
`python cpf-tools/....py` 로 직접 실행하면 `sys.stdout.encoding == 'cp949'` 가 되고
한글 출력이 `?α? ?????` 처럼 깨진다. 실제로 Registry 게이트 결과와 게이트 실패 메시지가
그렇게 나와 진단이 불가능했다.

호출자가 `PYTHONUTF8=1` 을 넣어 주기를 기대하면 안 된다. PowerShell 진입점들이 이미 각자
UTF-8 preamble 을 들고 있는 것과 같은 이유로, Python 진입점도 자기 출력 스트림을 고정한다.

- Tool 진입점(단독 실행 .py): 각 파일이 preamble 을 가진다.
- Test 모듈: `cpf-tools/conftest.py` 가 정본 소유자다(파일마다 반복하지 않는다).
- pytest 실행기: `run-cpf-pytest.py` 가 자식 환경에도 UTF-8 을 넘긴다.
"""
from __future__ import annotations

import io
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
TOOLS = ROOT / "cpf-tools"
SKIP_DIRS = {"build", "node_modules", ".git", "__pycache__", "bin", "out"}
ENTRYPOINT = re.compile(r"if\s+__name__\s*==")
RECONFIGURE = re.compile(r"reconfigure\s*\(\s*encoding\s*=\s*[\"']utf-8[\"']")
NON_ASCII = re.compile(r"[^\x00-\x7f]")


def _is_test_module(path: Path) -> bool:
    return path.name.startswith("test_") or path.parent.name == "tests"


@lru_cache(maxsize=1)
def _tool_entrypoints() -> tuple[Path, ...]:
    found: list[Path] = []
    for path in TOOLS.rglob("*.py"):
        if set(path.relative_to(ROOT).parts) & SKIP_DIRS:
            continue
        if _is_test_module(path):
            continue
        text = io.open(path, encoding="utf-8", errors="replace").read()
        if ENTRYPOINT.search(text) and NON_ASCII.search(text):
            found.append(path)
    return tuple(sorted(found))


def test_tool_entrypoints_that_emit_korean_force_utf8_output() -> None:
    violations = []
    for path in _tool_entrypoints():
        text = io.open(path, encoding="utf-8", errors="replace").read()
        if not RECONFIGURE.search(text):
            violations.append(path.relative_to(ROOT).as_posix())
    assert violations == [], (
        "한글을 출력하는 Python 진입점은 자기 stdout/stderr 를 UTF-8 로 고정해야 한다. "
        "고정하지 않으면 Windows cp949 콘솔에서 진단 메시지가 깨진다: "
        f"{violations}")


def test_scan_actually_covers_tool_entrypoints() -> None:
    # 0건을 검사하는 빈 게이트가 되지 않도록 실제 적용 대상 수를 고정한다.
    assert len(_tool_entrypoints()) >= 60, len(_tool_entrypoints())


def test_test_modules_are_covered_by_the_canonical_conftest() -> None:
    conftest = TOOLS / "conftest.py"
    assert conftest.is_file(), "cpf-tools/conftest.py 가 테스트 출력 인코딩의 정본 소유자다"
    text = io.open(conftest, encoding="utf-8").read()
    assert RECONFIGURE.search(text), text[:400]
    assert "sys.stdout" in text and "sys.stderr" in text


def test_canonical_pytest_runner_passes_utf8_to_children() -> None:
    runner = TOOLS / "testing" / "tools" / "run-cpf-pytest.py"
    text = io.open(runner, encoding="utf-8").read()
    assert 'env["PYTHONUTF8"] = "1"' in text
    assert 'env["PYTHONIOENCODING"] = "utf-8"' in text


def test_repository_python_sources_have_no_syntax_warning() -> None:
    """`"...\\governance"` 같은 잘못된 escape 는 SyntaxWarning 으로 Problems 에 남는다."""
    import warnings

    offenders = []
    for path in TOOLS.rglob("*.py"):
        if set(path.relative_to(ROOT).parts) & SKIP_DIRS:
            continue
        source = io.open(path, encoding="utf-8", errors="replace").read()
        with warnings.catch_warnings(record=True) as captured:
            warnings.simplefilter("always")
            try:
                compile(source, str(path), "exec")
            except SyntaxError as broken:
                offenders.append(f"{path.relative_to(ROOT).as_posix()}: {broken}")
                continue
            for item in captured:
                if issubclass(item.category, SyntaxWarning):
                    offenders.append(f"{path.relative_to(ROOT).as_posix()}: {item.message}")
    assert offenders == [], offenders


def test_patterns_use_regex_boundaries_not_control_characters() -> None:
    for pattern in (ENTRYPOINT, RECONFIGURE, NON_ASCII):
        assert chr(8) not in pattern.pattern
        assert chr(11) not in pattern.pattern
    assert RECONFIGURE.search("stream.reconfigure(encoding='utf-8')") is not None
    assert RECONFIGURE.search("stream.reconfigure(encoding='cp949')") is None
    assert NON_ASCII.search("한글") is not None
    assert NON_ASCII.search("ascii only") is None

"""살아 있는 Runtime 이 쓰고 있는 증적 파일을 공유 모드로 읽는지 검증한다.

Windows 에서 File Log Owner(`CpfFileLogWriter`)는 rolling 파일 핸들을 연 채 유지한다.
`[IO.File]::ReadAllText/ReadAllLines/ReadLines` 와 `.NET` 기본 열기는 `FileShare.Read` 만
요청하므로, 쓰기 핸들이 살아 있으면 "다른 프로세스가 사용 중" 으로 던진다.

실제 증상: Batch Two-Worker 검증이 업무 단정(sampleRows/idempotencyRows/serviceCallSuccess)과
UNKNOWN→reconcile→fencing takeover 를 **전부 통과한 뒤** 파일 로그 lineage 단정에서만
`Exception calling "ReadLines" ... because it is being used by another process` 로 죽었다.
`smoke-integrated-log-correlation.ps1` 은 각 읽기를 try/catch 로 감싸므로 더 나쁘다 —
잠김 예외가 삼켜져 '상관관계 없음' 이라는 **잘못된 FAIL** 로 보고된다.

그래서 살아 있는 Runtime 증적을 읽는 검증기는 `FileShare.ReadWrite` 를 명시해야 한다.
"""
from __future__ import annotations

import io
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]

# Runtime 이 기동해 있는 동안 그 Runtime 이 생성한 파일 증적을 읽는 검증기들이다.
# 목록을 늘릴 때는 "그 스크립트가 살아 있는 Runtime 의 산출 파일을 읽는가" 만 기준으로 한다.
LIVE_RUNTIME_EVIDENCE_READERS = (
    "cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1",
    "cpf-tools/runtime/tools/smoke-file-log-standard-runtime.ps1",
    "cpf-tools/runtime/tools/smoke-integrated-log-correlation.ps1",
    "cpf-tools/runtime/tools/smoke-bat-trace-boost-runtime.ps1",
    "cpf-tools/runtime/tools/smoke-trace-boost-runtime.ps1",
    "cpf-tools/runtime/tools/smoke-bat-log-bean-runtime.ps1",
    "cpf-tools/verification/tools/smoke-standard-header-e2e.ps1",
)

# 새로 추가되는 스크립트까지 잡기 위한 구조적 규칙의 탐색 범위.
SCAN_DIRECTORIES = ("cpf-tools/runtime/tools", "cpf-tools/verification/tools")

RAW_READ = re.compile(r"\[(?:System\.)?IO\.File\]::(?:ReadAllText|ReadAllLines|ReadLines)\s*\(")
SHARED_READER = re.compile(r"function\s+Read-CpfLiveLogText\b", re.I)
SHARE_READWRITE = re.compile(r"\[(?:System\.)?IO\.FileShare\]::ReadWrite\b")
SHARE_DELETE = re.compile(r"\[(?:System\.)?IO\.FileShare\]::Delete\b")
# 살아 있는 Runtime 이 쓰는 로그 루트를 실제로 열거하는 구조인지 판정하는 표지.
# 느슨하게 잡으면 정적 파일만 읽는 검증기까지 위반이 된다. 실제로 `$mapped.logicalDatabase`
# 같은 속성 접근이 `.log` 로 걸렸고 `CPF_LOG_ROOT` 문자열만 보는 설정 검사기도 걸렸다.
# 그래서 "로그 루트를 열거하는 Get-ChildItem" 이라는 구조 자체를 표지로 쓴다.
LOG_DISCOVERY = re.compile(
    r"Get-ChildItem[^\n|]*(?:LogRoot|LogDir|LogBase|RuntimeLog|logs)", re.I)


def _strip_comments(text: str) -> str:
    """PowerShell 줄주석과 블록주석을 지운다.

    이 게이트가 금지하는 API 이름은 **설명 주석에도 등장한다**. 주석을 지우지 않으면
    올바르게 고친 스크립트가 자기 주석 때문에 위반으로 잡힌다(과거에 실제로 겪은 오작동이다).
    """
    without_block = re.sub(r"<#.*?#>", " ", text, flags=re.S)
    return re.sub(r"(?m)#[^\n]*", " ", without_block)


@lru_cache(maxsize=None)
def _source(relative: str) -> str:
    path = ROOT / relative
    assert path.is_file(), f"declared live-runtime evidence reader is missing: {relative}"
    return io.open(path, encoding="utf-8-sig", errors="replace").read()


def test_declared_live_runtime_readers_use_an_explicit_shared_open() -> None:
    missing = []
    for relative in LIVE_RUNTIME_EVIDENCE_READERS:
        text = _source(relative)
        if not SHARED_READER.search(text):
            missing.append(f"{relative}: Read-CpfLiveLogText is not defined")
            continue
        if not SHARE_READWRITE.search(text):
            missing.append(f"{relative}: FileShare.ReadWrite is not requested")
        if not SHARE_DELETE.search(text):
            missing.append(f"{relative}: FileShare.Delete is not requested (rolling rename)")
    assert missing == [], (
        "살아 있는 Runtime 증적을 읽는 검증기는 공유 모드를 명시해야 한다. "
        f"명시하지 않으면 파일 잠김이 검증 실패로 둔갑한다: {missing}")


def test_declared_live_runtime_readers_do_not_use_share_read_only_apis() -> None:
    violations = []
    for relative in LIVE_RUNTIME_EVIDENCE_READERS:
        body = _strip_comments(_source(relative))
        for match in RAW_READ.finditer(body):
            line = body.count(chr(10), 0, match.start()) + 1
            violations.append(f"{relative}:{line}")
    assert violations == [], (
        "[IO.File]::ReadAllText/ReadAllLines/ReadLines 는 FileShare.Read 로만 연다. "
        f"살아 있는 Runtime 의 증적에는 쓸 수 없다: {violations}")


def test_any_log_reading_runtime_smoke_uses_the_shared_reader() -> None:
    """목록에 없는 신규 스크립트도 같은 규칙을 받는다.

    선언 목록만 검사하면 다음에 추가되는 검증기가 같은 결함을 그대로 반복한다.
    로그 파일을 찾아 읽는 구조를 가진 스크립트는 자동으로 이 계약의 대상이다.
    """
    violations = []
    for directory in SCAN_DIRECTORIES:
        for path in sorted((ROOT / directory).glob("*.ps1")):
            relative = path.relative_to(ROOT).as_posix()
            raw = io.open(path, encoding="utf-8-sig", errors="replace").read()
            body = _strip_comments(raw)
            if not LOG_DISCOVERY.search(body) or not RAW_READ.search(body):
                continue
            if not SHARE_READWRITE.search(raw):
                violations.append(relative)
    assert violations == [], (
        "로그 파일을 탐색해 읽으면서 공유 모드를 명시하지 않은 검증기가 있다: "
        f"{violations}")


def test_scan_actually_covers_the_known_readers() -> None:
    # 규칙이 0건을 검사하는 빈 게이트가 되지 않도록 실제 적용 대상 수를 고정한다.
    assert len(LIVE_RUNTIME_EVIDENCE_READERS) >= 7
    for relative in LIVE_RUNTIME_EVIDENCE_READERS:
        assert (ROOT / relative).is_file(), relative


def test_patterns_use_regex_boundaries_not_control_characters() -> None:
    """붙여넣기 사고로 `\\b` 가 0x08 로 바뀌면 게이트가 조용히 0건을 반환한다."""
    for pattern in (RAW_READ, SHARED_READER, SHARE_READWRITE, SHARE_DELETE, LOG_DISCOVERY):
        assert chr(8) not in pattern.pattern
        assert chr(11) not in pattern.pattern
    assert RAW_READ.search("$x = [IO.File]::ReadAllText($p, [Text.Encoding]::UTF8)") is not None
    assert RAW_READ.search("$x = [System.IO.File]::ReadLines($p)") is not None
    assert RAW_READ.search("$x = Read-CpfLiveLogText -Path $p") is None
    assert _strip_comments("# [IO.File]::ReadAllText(x)\n$a=1").count("ReadAllText") == 0

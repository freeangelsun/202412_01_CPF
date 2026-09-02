"""PowerShell 스크립트가 참조하는 다른 스크립트가 실제로 존재하는지 보장한다.

`smoke-gateway-bat-runtime.ps1` 이 `Join-Path $PSScriptRoot "smoke-openapi.ps1"` 로 존재하지 않는
경로를 부르고 있었다. 그 파일은 `cpf-tools/verification/openapi` 에만 있고 이 폴더에는 git 이력상
한 번도 존재한 적이 없다. 그동안 더 앞 계층(Runtime 기동)이 먼저 죽어 가려져 있다가, 기동이
성공하자마자 `[142]` 가 "is not recognized as a name of a cmdlet" 로 끝났다.

같은 형태가 저장소에 4건 있었다. 실행하지 않아도 완전히 검출 가능한 결함이므로 정적으로 막는다.
"""
from __future__ import annotations

import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git"}
EXCLUDED_PREFIXES = ("cpf-release/",)
# `Join-Path $PSScriptRoot "<상대경로>.ps1"` 형태만 본다. 변수가 섞인 경로는 정적으로 확정할 수
# 없으므로 검사 대상이 아니다.
SCRIPT_REFERENCE = re.compile(r'Join-Path\s+\$PSScriptRoot\s+"([^"$]+\.ps1)"')


@lru_cache(maxsize=1)
def _scripts() -> tuple[Path, ...]:
    files: list[Path] = []
    for path in ROOT.rglob("*.ps1"):
        relative = path.relative_to(ROOT).as_posix()
        if set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS:
            continue
        if relative.startswith(EXCLUDED_PREFIXES):
            continue
        files.append(path)
    return tuple(files)


def _references() -> list[tuple[Path, str, Path]]:
    found: list[tuple[Path, str, Path]] = []
    for path in _scripts():
        text = path.read_text(encoding="utf-8", errors="replace")
        for match in SCRIPT_REFERENCE.finditer(text):
            reference = match.group(1)
            # PowerShell 경로는 역슬래시를 쓴다. POSIX 에서도 해석되도록 정규화한다.
            resolved = path.parent.joinpath(*reference.replace("\\", "/").split("/"))
            found.append((path, reference, resolved))
    return found


def test_referenced_powershell_scripts_exist() -> None:
    missing = [
        f"{path.relative_to(ROOT).as_posix()} -> {reference}"
        for path, reference, resolved in _references()
        if not resolved.is_file()
    ]
    assert missing == [], (
        "PowerShell 스크립트가 존재하지 않는 스크립트를 참조한다"
        f" (실행 시 'is not recognized' 로 즉시 실패): {missing}"
    )


def test_scan_actually_finds_references() -> None:
    # 참조가 0건이면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    references = _references()
    assert len(references) >= 20, f"script references not scanned: {len(references)}"
    assert len(_scripts()) >= 50, f"powershell scripts not scanned: {len(_scripts())}"

"""Spring Bean 의 주입 생성자가 모호하지 않음을 보장한다.

생성자가 하나면 Spring 이 그것을 쓴다. 둘 이상인데 @Autowired 로 대상을 지정하지 않으면 Spring 은
기본 생성자를 찾고, 없으면 'No default constructor found' 로 Runtime 기동이 실패한다.

컴파일도 단위테스트도 이 결함을 잡지 못한다. 실제로 CpfScgTargetResolver 와
BrokerReliabilityApprovalOwnerCommandAdapter 가 각각 Gateway/1-WAS 기동을 실패시켰고, 원인은
Runtime 로그에서만 드러났다. 운영 생성자와 테스트 seam 을 함께 두는 것은 정상적인 설계이므로,
금지하는 대신 주입 대상을 명시하도록 강제한다.
"""
from __future__ import annotations

import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git", "generated"}
EXCLUDED_PREFIXES = ("cpf-release/",)

STEREOTYPE = re.compile(
    r"(?<![\w.])@(Component|Service|Repository|Controller|RestController"
    r"|CpfService|CpfRepository|CpfController|CpfRestController)\b")
AUTOWIRED = re.compile(r"@(?:[\w.]*\.)?Autowired\b")
DECLARATION = re.compile(
    r"(?<![\w.])@(?:Component|Service|Repository|Controller|RestController|Cpf\w+)[^\n]*\n"
    r"(?:\s*@[^\n]*\n)*\s*public\s+(?:final\s+)?class\s+(\w+)")


@lru_cache(maxsize=1)
def _sources() -> list[Path]:
    files: list[Path] = []
    for path in ROOT.rglob("*.java"):
        relative = path.relative_to(ROOT).as_posix()
        if set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS:
            continue
        if relative.startswith(EXCLUDED_PREFIXES):
            continue
        if "/src/main/java/" not in path.as_posix():
            continue
        files.append(path)
    return files


def _violations() -> list[str]:
    found: list[str] = []
    for path in _sources():
        text = path.read_text(encoding="utf-8", errors="replace")
        # @Autowired 는 완전수식(@org.springframework...Autowired)으로도 쓰인다. 단순 문자열
        # 비교로 놓치면 이미 정상인 클래스를 위반으로 보고하게 된다.
        if not STEREOTYPE.search(text) or AUTOWIRED.search(text):
            continue
        declaration = DECLARATION.search(text)
        if not declaration:
            continue
        name = declaration.group(1)
        constructors = re.findall(
            r"(?:^|\n)\s*(?:public\s+|protected\s+|private\s+)?"
            + re.escape(name) + r"\s*\(([^)]*)\)\s*\{", text)
        if len(constructors) < 2:
            continue
        # 기본 생성자가 있으면 Spring 이 그것을 쓸 수 있으므로 기동은 실패하지 않는다.
        if any(argument.strip() == "" for argument in constructors):
            continue
        found.append(f"{path.relative_to(ROOT).as_posix()}:{name} constructors={len(constructors)}")
    return sorted(found)


def test_multi_constructor_beans_declare_the_injection_constructor() -> None:
    violations = _violations()
    assert violations == [], (
        "생성자가 여럿인 Spring Bean 은 @Autowired 로 주입 대상을 명시해야 한다"
        f" (미지정 시 'No default constructor found' 로 기동 실패): {violations}"
    )


def test_scan_actually_covers_spring_beans() -> None:
    # 대상이 0건이면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    files = _sources()
    assert len(files) > 500, f"product java sources not scanned: {len(files)}"
    beans = [p for p in files
             if STEREOTYPE.search(p.read_text(encoding="utf-8", errors="replace"))]
    assert len(beans) > 100, f"spring bean sources not found: {len(beans)}"

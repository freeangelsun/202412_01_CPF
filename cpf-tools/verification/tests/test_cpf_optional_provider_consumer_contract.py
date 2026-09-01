"""opt-in Provider를 필수 주입하는 소비자가 없음을 보장한다.

Provider AutoConfiguration이 `@ConditionalOnProperty`(matchIfMissing 없음)로 opt-in인데 소비자가
그 Bean을 생성자에서 필수로 받으면, 기능을 쓰지 않는 Runtime이 기동조차 못 한다. 소비자는
ObjectProvider로 받고 기능이 없으면 지원하지 않는다고 보고해야 한다.

실제로 FeatureFlagApprovalOwnerCommandAdapter(CpfFeatureFlagOperations)와
GatewayApprovalOwnerCommandAdapter(CpfGatewayRegistryPort)가 1-WAS 기동을 각각 실패시켰다.
컴파일도 단위테스트도 이 결함을 잡지 못한다.
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
# opt-in: 값이 있어야만 활성화되고 기본 활성(matchIfMissing)이 아니다.
# 완전수식(@org.springframework...ConditionalOnProperty)으로도 쓰인다. 단순 이름만 보면
# 이미 해결된 클래스를 위반으로 보고하게 된다.
OPT_IN = re.compile(r"@(?:[\w.]*\.)?ConditionalOnProperty\b")
MATCH_IF_MISSING = re.compile(r"matchIfMissing\s*=\s*true")
PORT_TYPE = re.compile(r"\b(Cpf[A-Za-z0-9]+(?:Port|Operations))\b")


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


BEAN_DECLARATION = re.compile(
    r"@Bean[^\n]*\n(?:\s*@[^\n]*\n)*\s*(?:public\s+)?"
    r"(Cpf[A-Za-z0-9]+(?:Port|Operations))\s+\w+\s*\(")
IMPLEMENTATION = re.compile(r"\bimplements\s+[^{]*?\b(Cpf[A-Za-z0-9]+(?:Port|Operations))\b")


@lru_cache(maxsize=1)
def _opt_in_types() -> set[str]:
    """모든 공급 경로가 opt-in 인 Port/Operations 타입만 optional 로 본다.

    같은 타입을 조건 없이 공급하는 경로가 하나라도 있으면 소비자의 필수 주입은 정상이다.
    한쪽만 보고 판정하면 정상 코드를 위반으로 보고하게 된다.
    """
    conditional: set[str] = set()
    unconditional: set[str] = set()
    for path in _sources():
        text = path.read_text(encoding="utf-8", errors="replace")
        opt_in = bool(OPT_IN.search(text)) and not MATCH_IF_MISSING.search(text)
        supplied = set(BEAN_DECLARATION.findall(text))
        if STEREOTYPE.search(text):
            supplied |= set(IMPLEMENTATION.findall(text))
        (conditional if opt_in else unconditional).update(supplied)
    return conditional - unconditional



def _injection_parameters(text: str, name: str) -> list[str]:
    """Spring 이 실제로 호출하는 생성자의 파라미터만 돌려준다.

    @Autowired 가 있으면 그 생성자가, 없으면 유일한 생성자가 주입 대상이다. 테스트 seam 을
    위반으로 세지 않기 위해 나머지는 보지 않는다.
    """
    pattern = re.compile(re.escape(name) + r"\s*\(([^)]*)\)\s*\{")
    signatures: list[tuple[str, str]] = []
    for match in pattern.finditer(text):
        head = text[max(0, match.start() - 200):match.start()]
        signatures.append((head, match.group(1)))
    if not signatures:
        return []
    annotated = [params for head, params in signatures if "Autowired" in head]
    if annotated:
        return annotated
    return [signatures[0][1]] if len(signatures) == 1 else []

def _violations() -> list[str]:
    optional = _opt_in_types()
    if not optional:
        return []
    found: list[str] = []
    for path in _sources():
        text = path.read_text(encoding="utf-8", errors="replace")
        if not STEREOTYPE.search(text):
            continue
        # Provider 와 같은 속성 조건을 붙여 기능이 꺼지면 Consumer 도 사라지는 것은 정상 해법이다.
        # 이 경우 필수 주입이어도 기동을 막지 않는다.
        if OPT_IN.search(text) and not MATCH_IF_MISSING.search(text):
            continue
        name = path.stem
        # 생성자가 여럿이면 Spring 이 실제로 쓰는 것만 본다. @Autowired 가 있으면 그 생성자가,
        # 없으면 유일한 생성자가 주입 대상이다. 테스트 seam 을 위반으로 세지 않는다.
        for params in _injection_parameters(text, name):
            for chunk in params.split(","):
                if "ObjectProvider" in chunk or "Optional" in chunk:
                    continue
                match = PORT_TYPE.search(chunk)
                if match and match.group(1) in optional:
                    found.append(f"{path.relative_to(ROOT).as_posix()}:{name} requires {match.group(1)}")
    return sorted(set(found))


def test_optional_providers_are_not_required_by_consumers() -> None:
    violations = _violations()
    assert violations == [], (
        "opt-in Provider Bean 은 ObjectProvider 로 받아야 한다"
        f" (필수 주입 시 기능 미사용 Runtime 이 기동 실패): {violations}"
    )


def test_scan_actually_finds_optional_providers() -> None:
    # opt-in Provider 가 0건이면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    assert len(_opt_in_types()) > 0, "opt-in provider types not detected"
    assert len(_sources()) > 500, f"product java sources not scanned: {len(_sources())}"

"""mandatory Admin Route가 요구하는 Port의 Provider는 opt-in일 수 없다.

`CANONICAL_PRODUCT_REQUIREMENTS.csv` 의 `ADM-LOG`(cpf-admin) 와 `CPF-FILELOG`
(platform-operations/observability) 는 모두 CURRENT mandatory capability 다. optional 로 표기된
capability(`CORE-MESSAGE` 등)와 달리 설정으로 취소할 수 있는 대상이 아니다.

그런데 `AdmRemoteLogController` 가 소비하는 `CpfRemoteLogArtifactPort` 의 유일한 구현이
`@ConditionalOnProperty(cpf.remote-log.local.enabled=true)` 로 opt-in 이어서, 기본 구성의 1-WAS 가
`required a bean of type CpfRemoteLogArtifactPort` 로 기동하지 못했다.

이때 Controller 를 조건부로 지우면 mandatory Admin Route Contract 가 축소되고, 조립 Runtime 마다
YAML 로 enabled=true 를 넣으면 ADM 을 업무 Domain 처럼 취급하는 것이 된다. 올바른 판정은
"mandatory route 가 요구하는 Port 는 Product Composition 이 무조건 공급해야 한다" 이므로,
그 계약을 정적으로 강제한다.
"""
from __future__ import annotations

import io
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
ROUTE_CONTRACT = ROOT / "cpf-admin/frontend/src/generated/adm-route-operation-contract.ts"
ADMIN_SOURCE = ROOT / "cpf-admin/src/main/java"
# ADM 을 조립해 기동하는 Runtime(1-WAS)이 실제로 싣는 모듈 집합.
COMPOSED_MODULES = (
    "cpf-admin",
    "cpf-backoffice/online",
    "cpf-gateway",
    "cpf-common",
    "cpf-starters",
    "cpf-framework",
    "cpf-internal",
    "cpf-tools/runtime/cpf-local-runtime",
)
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git", "generated"}
PORT = re.compile(r"\b(Cpf[A-Za-z0-9]+(?:Port|Operations))\b")
OPERATION_ID = re.compile(r'operationId\s*=\s*"([^"]+)"')
IMPLEMENTS = re.compile(r"class\s+(\w+)[^{]*?\bimplements\s+([^{]+)\{", re.S)
BEAN = re.compile(r"@Bean\b([^\n]*)")
CONDITIONAL_ON_PROPERTY = re.compile(r"@ConditionalOnProperty\b[^)]*\)", re.S)
STEREOTYPE = re.compile(
    r"(?<![\w.])@(Component|Service|Repository|Controller|RestController"
    r"|CpfService|CpfRepository|CpfController|CpfRestController)\b")


# Platform DB role 조건은 '기능 토글'이 아니라 ADM Control Plane 이 어차피 전제하는 '인프라 가용성'
# 이다. ADM 은 Platform DB 없이는 어떤 Route 도 서비스할 수 없다. 반대로 Generated Domain 은 이 role
# 을 등록하지 않으므로, 이 조건이 있어야 Domain Runtime 이 깨지지 않는다(WP-R16.01 참조).
# 따라서 이 조건은 mandatory route 충족을 막지 않는다.
INFRASTRUCTURE_CONDITIONS = ("role-datasources.cpf-platform-db",)


def _optional(annotations: str) -> bool:
    """설정으로 꺼질 수 있는 공급자인지. matchIfMissing 이 있으면 기본 제공이다."""
    match = CONDITIONAL_ON_PROPERTY.search(annotations)
    if not match:
        return False
    if "matchIfMissing" in match.group(0):
        return False
    return not any(marker in match.group(0) for marker in INFRASTRUCTURE_CONDITIONS)


@lru_cache(maxsize=1)
def _composed_sources() -> tuple[Path, ...]:
    files: list[Path] = []
    for module in COMPOSED_MODULES:
        directory = ROOT / module
        if not directory.is_dir():
            continue
        files += [
            path for path in directory.rglob("*.java")
            if "/src/main/java/" in path.as_posix()
            and not (set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS)
        ]
    return tuple(files)


@lru_cache(maxsize=1)
def _providers() -> dict[str, list[bool]]:
    """Port 별 공급자 목록. 값은 '설정으로 꺼질 수 있는가' 플래그다.

    공급 경로는 두 가지다. @Bean 메서드(반환 타입이 Port 이거나 Port 구현 클래스)와,
    Port 를 구현한 stereotype 클래스. @Bean 만 세면 `AdmParameterReferenceCatalogAdapter`
    같은 component-scan 공급자를 없는 것으로 오판한다.
    """
    implementations: dict[str, set[str]] = {}
    for path in _composed_sources():
        text = io.open(path, encoding="utf-8", errors="replace").read()
        for match in IMPLEMENTS.finditer(text):
            for port in PORT.findall(match.group(2)):
                implementations.setdefault(match.group(1), set()).add(port)

    found: dict[str, list[bool]] = {}
    for path in _composed_sources():
        text = io.open(path, encoding="utf-8", errors="replace").read()
        head = text[:text.find("public class")] if "public class" in text else text[:600]
        class_optional = _optional(head)
        for match in BEAN.finditer(text):
            tail = text[match.end():match.end() + 900]
            declaration = re.search(
                r"(?:^|\n)[ \t]*(?:public\s+|protected\s+)?(?:static\s+)?"
                r"([\w.]+(?:<[^>]*>)?)\s+(\w+)\s*\(", tail)
            if not declaration:
                continue
            returned = declaration.group(1).split(".")[-1]
            supplied = set(implementations.get(returned, set()))
            if re.fullmatch(r"Cpf[A-Za-z0-9]+(?:Port|Operations)", returned):
                supplied.add(returned)
            if not supplied:
                continue
            optional = class_optional or _optional(tail[:declaration.start()] + match.group(1))
            for port in supplied:
                found.setdefault(port, []).append(optional)
        if STEREOTYPE.search(head):
            for match in IMPLEMENTS.finditer(text):
                for port in PORT.findall(match.group(2)):
                    found.setdefault(port, []).append(class_optional)
    return found


@lru_cache(maxsize=1)
def _contract_operations() -> frozenset[str]:
    text = io.open(ROUTE_CONTRACT, encoding="utf-8", errors="replace").read()
    return frozenset(re.findall(r'"([a-zA-Z][A-Za-z0-9]*)"', text))


def _violations() -> list[str]:
    operations = _contract_operations()
    providers = _providers()
    found: list[str] = []
    for path in ADMIN_SOURCE.rglob("*.java"):
        text = io.open(path, encoding="utf-8", errors="replace").read()
        if "@RestController" not in text:
            continue
        if not (set(OPERATION_ID.findall(text)) & operations):
            continue
        for constructor in re.finditer(
                re.escape(path.stem) + r"\s*\(([^)]*)\)", text, re.S):
            for chunk in constructor.group(1).split(","):
                if "ObjectProvider" in chunk or "Optional" in chunk:
                    continue
                match = PORT.search(chunk)
                if not match:
                    continue
                port = match.group(1)
                supplied = providers.get(port, [])
                if not supplied or all(supplied):
                    reason = "provider 없음" if not supplied else "모든 provider 가 opt-in"
                    found.append(
                        f"{path.relative_to(ROOT).as_posix()}:{path.stem} requires {port} ({reason})")
    return sorted(set(found))


def test_mandatory_route_ports_have_unconditional_provider() -> None:
    violations = _violations()
    assert violations == [], (
        "mandatory Admin Route 가 요구하는 Port 는 Product Composition 이 무조건 공급해야 한다"
        " (Controller 를 조건부로 지우거나 Runtime YAML 로 enabled=true 를 넣는 것은 해법이 아니다):"
        f" {violations}"
    )


def test_scan_actually_covers_contract_and_providers() -> None:
    # 계약 operation 이나 provider 가 비면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    assert len(_contract_operations()) > 100, f"route contract not parsed: {len(_contract_operations())}"
    assert len(_providers()) > 20, f"providers not scanned: {len(_providers())}"
    assert "CpfRemoteLogArtifactPort" in _providers(), sorted(_providers())[:10]

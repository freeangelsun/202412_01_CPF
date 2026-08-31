"""CPF 3단 구조(Framework Base -> Domain Base -> Business) 준수 계약.

`CpfServicePolicyBeanPostProcessor` / `CpfControllerPolicyBeanPostProcessor` 는 기동 시점에
`@CpfService` / `@CpfRepository` / `@CpfController` Bean 이 abstract Domain Base 를 상속했는지
fail-closed 로 검증한다(기본값 enabled=true, requireBaseClass=true).

위반은 개별 App 을 따로 띄울 때는 잘 드러나지 않다가, ADM/Backoffice/EDU/Generated Domain 을
한 ApplicationContext 에 올리는 1-WAS 에서 기동 자체를 막는다. 실제로 40개 Business type 이
Domain Base 를 상속하지 않아 1-WAS 가 한 번도 기동하지 못했고, EDU 는 Domain Base 자체가
없었다. 정적 검사로 재발을 막는다.
"""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SKIP_PARTS = {"build", "bin", ".git", "node_modules", "cpf-release", "__pycache__"}
# Business Source 를 소유하는 제품 모듈(Generated Domain 포함)
BUSINESS_ROOTS = ("cpf-admin/", "cpf-backoffice/", "cpf-education/", "cpf-external/", "cpf-member/")
BUSINESS_ANNOTATIONS = ("@CpfService", "@CpfRepository", "@CpfRestController", "@CpfController")
DECLARATION = re.compile(
    r"(@Cpf(?:Service|Repository|RestController|Controller))\s*(?:\([^)]*\))?\s*\n"
    r"\s*public\s+(?:final\s+)?class\s+(\w+)([^{]*)\{")


def _business_sources() -> list[Path]:
    files = []
    for path in sorted(ROOT.rglob("*.java")):
        parts = path.relative_to(ROOT).parts
        if SKIP_PARTS.intersection(parts) or "test" in parts:
            continue
        if not path.relative_to(ROOT).as_posix().startswith(BUSINESS_ROOTS):
            continue
        files.append(path)
    return files


def test_every_cpf_business_type_extends_a_domain_base_class():
    violations = []
    for path in _business_sources():
        text = path.read_text(encoding="utf-8", errors="replace")
        if not any(a in text for a in BUSINESS_ANNOTATIONS):
            continue
        for match in DECLARATION.finditer(text):
            annotation, cls, tail = match.group(1), match.group(2), match.group(3)
            if "extends" in tail:
                continue
            violations.append(f"{path.relative_to(ROOT).as_posix()}: {annotation} {cls}")
    assert not violations, (
        "CPF 3단 구조 위반(Domain Base 미상속). 1-WAS 기동이 fail-closed 로 막힌다:\n  "
        + "\n  ".join(sorted(violations)))


def test_every_business_module_owns_a_domain_base_service():
    """Domain Base 가 없으면 그 모듈의 @CpfService 는 계약을 지킬 방법이 없다."""
    missing = []
    for module in BUSINESS_ROOTS:
        module_dir = ROOT / module.rstrip("/")
        if not module_dir.is_dir():
            continue
        has_service = any(
            "BaseService.java" in p.name
            for p in module_dir.rglob("*BaseService.java")
            if not SKIP_PARTS.intersection(p.relative_to(ROOT).parts))
        if not has_service:
            missing.append(module.rstrip("/"))
    assert not missing, f"Domain Base Service 가 없는 업무 모듈: {missing}"


def test_generator_emits_domain_base_extension_for_dependency_consumer():
    """Generated Domain 산출물도 같은 계약을 지켜야 idempotency 와 기동이 함께 유지된다."""
    engine = ROOT / "cpf-tools/generator/engine/cpf_domain_generator.py"
    text = engine.read_text(encoding="utf-8")
    assert "public class DomainDependencySampleService extends {d.class_name}BaseService" in text
    assert "import {d.package_name}.base.{d.class_name}BaseService;" in text

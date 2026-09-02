"""CPF Runtime 이 프록시하는 stereotype 클래스가 final 이 아님을 보장한다.

@CpfRepository 는 Spring @Repository stereotype 이라 예외변환 Advisor 가 CGLIB 프록시를 만든다.
@CpfTransactional/@Async 같은 Advice annotation 도 대상 Bean 을 프록시로 감싼다. final 클래스는
subclass 를 만들 수 없어 'Cannot subclass final class' 로 Runtime 기동이 실패한다.

컴파일과 단위테스트는 이 결함을 잡지 못한다. 실제로 BackofficeBootstrapApprovalRepository 가
final 로 유입되어 1-WAS 기동이 실패했고, 원인은 Runtime 로그에서만 드러났다. 그래서 정적
계약으로 고정한다.
"""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
# CPF 는 stereotype 이 붙은 Business Type 을 proxy-safe 로 요구한다. 근거는 제품 자신이다:
# CpfCapabilityUsageAspect.proxySafeBusinessType() 이 final Type 을 proxy-unsafe 로 판정한다.
# 어떤 Advisor 가 실제로 매칭되는지는 Runtime inventory/descriptor 상태에 따라 달라지므로
# 정적으로 "이 클래스는 프록시되지 않는다"고 단정할 수 없다. 실제로 stereotype 만 붙은
# BackofficeCommonCatalogController 가 프록시 대상이 되어 기동이 실패했다.
PROXIED_STEREOTYPES = ("@CpfRepository", "@CpfService", "@CpfController", "@CpfRestController")
ADVICE_ANNOTATIONS = ("@CpfTransactional", "@Transactional", "@Async",
                      "@Validated", "@Cacheable", "@CacheEvict", "@Retryable",
                      "@PreAuthorize", "@PostAuthorize", "@Secured",
                      "@CpfPermission", "@CpfLogging", "@CpfPerformance", "@CpfTimed",
                      "@CpfOnlineTransaction", "@CpfClient", "@CpfAudit",
                      "@CpfIdempotent", "@CpfApprovalRequired")
# 프록시는 클래스 annotation 뿐 아니라 '메서드' annotation 으로도 생긴다. 클래스 선언부만
# 보던 초판은 @RestController 만 붙고 @CpfTransactional/@CpfPermission 은 메서드에 있던
# AdmPlatformVersionController 를 놓쳐 기동 실패를 한 사이클 더 태웠다. Spring Bean 인
# final 클래스가 프록시 유발 annotation 을 '어디에든' 가지면 위반으로 본다.
SPRING_BEAN = re.compile(
    r"(?<![\w.])@(Component|Service|Repository|Controller|RestController"
    r"|ControllerAdvice|RestControllerAdvice|Configuration"
    r"|CpfService|CpfRepository|CpfController|CpfRestController)\b")
# @Scheduled 는 프록시를 만들지 않는다(스케줄러가 대상 메서드를 직접 호출한다). 포함하면
# 지금 정상 동작 중인 final @Scheduled 클래스 11 개를 오탐으로 보고하게 된다.
COMMENT = re.compile(r"//[^\n]*|/\*.*?\*/", re.S)
# 산출물/외부 소스는 제품 Source 가 아니다. cpf-release 는 Release 가 만든 투영본이므로
# 원본을 고치면 함께 갱신된다. 여기서 중복 검출하면 같은 결함을 두 번 세게 된다.
EXCLUDED_PARTS = {"build", "out", "node_modules", ".git", "generated"}
EXCLUDED_PREFIXES = ("cpf-release/",)

FINAL_CLASS = re.compile(r"^\s*public\s+final\s+class\s+(\w+)", re.MULTILINE)
# 선언 바로 앞에 붙은 annotation/주석만 훑는다. 본문에서 annotation 이름을 문자열이나
# Javadoc 으로 언급하는 클래스(정책 BeanPostProcessor 등)를 위반으로 세지 않기 위함이다.
DECLARATION_PREFIX = re.compile(r"^\s*(@|//|/\*|\*)")


def _product_java_files() -> list[Path]:
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


def _annotations_on_declaration(lines: list[str], index: int) -> set[str]:
    """`public final class` 선언 바로 위에 실제로 붙어 있는 annotation 만 모은다."""
    found: set[str] = set()
    cursor = index - 1
    while cursor >= 0:
        line = lines[cursor]
        if not line.strip():
            cursor -= 1
            continue
        if not DECLARATION_PREFIX.match(line):
            break
        stripped = line.strip()
        for marker in PROXIED_STEREOTYPES + ADVICE_ANNOTATIONS:
            if stripped.startswith(marker):
                found.add(marker)
        cursor -= 1
    return found


def _violations() -> list[str]:
    found: list[str] = []
    for path in _product_java_files():
        text = path.read_text(encoding="utf-8", errors="replace")
        if not any(marker in text for marker in PROXIED_STEREOTYPES + ADVICE_ANNOTATIONS):
            continue
        lines = text.splitlines()
        code = COMMENT.sub(" ", text)
        for match in FINAL_CLASS.finditer(text):
            line_index = text.count("\n", 0, match.start())
            markers = _annotations_on_declaration(lines, line_index)
            if not markers and SPRING_BEAN.search(code[max(0, match.start() - 400):match.start()]):
                # 클래스 선언부에 없더라도 메서드 annotation 이 프록시를 만든다.
                markers = {marker for marker in ADVICE_ANNOTATIONS
                           if re.search(re.escape(marker) + r"\b", code)}
            if markers:
                found.append(f"{path.relative_to(ROOT).as_posix()}:{match.group(1)}"
                             f" [{','.join(sorted(markers))}]")
    return sorted(found)


def test_proxied_stereotype_classes_are_not_final() -> None:
    violations = _violations()
    assert violations == [], (
        "CPF Runtime 이 프록시하는 stereotype 클래스는 final 일 수 없다"
        f" (CGLIB subclass 불가로 기동 실패): {violations}"
    )


def test_scan_actually_covers_product_sources() -> None:
    # 검사 대상이 0건이면 위 테스트는 언제나 통과하는 빈 게이트가 된다.
    files = _product_java_files()
    assert len(files) > 500, f"product java sources not scanned: {len(files)}"
    annotated = [p for p in files
                 if any(m in p.read_text(encoding="utf-8", errors="replace")
                        for m in PROXIED_STEREOTYPES + ADVICE_ANNOTATIONS)]
    assert len(annotated) > 20, f"proxied stereotype sources not found: {len(annotated)}"

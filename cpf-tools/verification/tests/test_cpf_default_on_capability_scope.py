"""기본 제공(matchIfMissing=true)으로 바뀐 capability가 의도치 않게 켜지지 않는지 검증한다.

mandatory Admin Route 를 만족시키려고 capability AutoConfiguration 을 `matchIfMissing = true` 로
바꾸면, 그 모듈을 선언한 **모든** Runtime 에서 켜진다. 실제로 `cpf-starter-integration-resilience`
는 Generated Domain(`cpf-member`, `cpf-external`)도 선언하는데, Domain 은 `CPF_PLATFORM_DB` role 을
등록하지 않아 `CpfDataSourceRegistry.require(CPF_PLATFORM_DB)` 가 throw 된다. 즉 ADM 을 고치려다
업무 Domain Runtime 을 깨뜨릴 수 있다.

그래서 기본 제공 capability 가 만드는 Bean 중 Platform role 자원을 요구하는 것은, Platform role 이
실제로 구성된 Runtime 에서만 만들어지도록 조건이 걸려 있어야 한다.
"""
from __future__ import annotations

import io
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git", "generated"}
EXCLUDED_PREFIXES = ("cpf-release/",)
PLATFORM_ROLE = "CpfDatabaseRole.CPF_PLATFORM_DB"
# Generated Domain 이 실제로 선언하는 Starter 만 본다. Domain 이 싣지 않는 모듈은 기본 제공으로
# 바뀌어도 Domain Runtime 을 깨뜨릴 수 없고, cpf-common 처럼 모든 CPF Runtime 이 Platform DB 를
# 전제하는 기반 모듈까지 위반으로 세면 게이트가 의미를 잃는다.
# Platform DB role 을 구성하지 않는 Runtime 만 본다 — Generated Domain 이다. batch/gateway 는
# launcher 가 role 을 넘기므로 이 게이트의 대상이 아니고, 그쪽에서 실제로 터진 무자격 주입 회귀는
# test_cpf_infrastructure_injection_resolvable.py 가 담당한다.
GENERATED_DOMAIN_BUILDS = (
    "cpf-member/online/build.gradle",
    "cpf-member/batch/build.gradle",
    "cpf-external/online/build.gradle",
)
PLATFORM_ROLE_GUARD = "role-datasources.cpf-platform-db"
DEFAULT_ON = re.compile(r"@ConditionalOnProperty\b[^)]*matchIfMissing\s*=\s*true[^)]*\)", re.S)
# Bean 자체가 opt-in 속성 뒤에 있으면 Platform role 이 없는 Runtime 에서 생성되지 않는다.
BEAN_OPT_IN = re.compile(r"@ConditionalOnProperty\b(?![^)]*matchIfMissing)[^)]*\)", re.S)
BEAN = re.compile(r"@Bean\b")
METHOD_NAME = re.compile(r"\b(\w+)\s*\(")


@lru_cache(maxsize=1)
def _domain_starter_modules() -> tuple[str, ...]:
    """Generated Domain 이 선언한 `cpf-starter-<a>-<b>` 를 모듈 경로 조각으로 바꾼다."""
    names: set[str] = set()
    for relative in GENERATED_DOMAIN_BUILDS:
        path = ROOT / relative
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        for artifact in re.findall(r"cpf-starter-([a-z0-9-]+)", text):
            names.add("cpf-starters/" + artifact.replace("-", "/"))
        # `project(':internal:integration:resilience')` 같은 논리 이름도 같은 물리 모듈을 가리킨다.
        for logical in re.findall(r"project\('(:internal:[^']+)'\)", text):
            names.add("cpf-starters/" + logical.removeprefix(":internal:").replace(":", "/"))
    return tuple(sorted(names))


@lru_cache(maxsize=1)
def _auto_configurations() -> tuple[Path, ...]:
    files: list[Path] = []
    for path in ROOT.rglob("*AutoConfiguration.java"):
        relative = path.relative_to(ROOT).as_posix()
        if set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS:
            continue
        if relative.startswith(EXCLUDED_PREFIXES) or "/src/main/java/" not in relative:
            continue
        if not any(relative.startswith(module) for module in _domain_starter_modules()):
            continue
        files.append(path)
    return tuple(files)


def _bean_blocks(text: str) -> list[str]:
    """각 @Bean 선언부터 다음 @Bean 직전까지를 하나의 블록으로 돌려준다."""
    positions = [match.start() for match in BEAN.finditer(text)]
    blocks: list[str] = []
    for index, start in enumerate(positions):
        end = positions[index + 1] if index + 1 < len(positions) else len(text)
        blocks.append(text[start:end])
    return blocks


def _bean_annotation_region(text: str, bean_start: int) -> str:
    """@Bean 부터 그 메서드 선언 줄까지의 annotation 구간을 돌려준다.

    문자 offset 창으로 자르면 여러 줄 annotation 이 잘려 가드를 놓친다. 줄 단위로 읽는다.
    """
    lines = []
    cursor = text.find(chr(10), bean_start)
    while cursor != -1:
        line_end = text.find(chr(10), cursor + 1)
        if line_end == -1:
            line_end = len(text)
        line = text[cursor + 1:line_end]
        lines.append(line)
        stripped = line.strip()
        if stripped and not stripped.startswith(("@", "//", "/*", "*")):
            break
        cursor = line_end
    return text[bean_start:bean_start + 5] + chr(10).join(lines)


def _violations() -> list[str]:
    found: list[str] = []
    for path in _auto_configurations():
        text = io.open(path, encoding="utf-8", errors="replace").read()
        header = text[:text.find("public class")] if "public class" in text else ""
        if not DEFAULT_ON.search(header):
            continue
        for usage in re.finditer(re.escape(PLATFORM_ROLE), text):
            bean_start = text.rfind("@Bean", 0, usage.start())
            if bean_start == -1:
                continue
            region = _bean_annotation_region(text, bean_start)
            # Platform role 조건이 직접 있거나 그 Bean 자체가 opt-in 이면, Platform role 이 없는
            # Runtime 에서는 애초에 생성되지 않는다.
            if PLATFORM_ROLE_GUARD in region or BEAN_OPT_IN.search(region):
                continue
            name = METHOD_NAME.search(region)
            found.append(
                f"{path.relative_to(ROOT).as_posix()}:{name.group(1) if name else chr(63)}"
                " requires CPF_PLATFORM_DB without a platform-role condition")
    return sorted(set(found))


def test_default_on_capabilities_do_not_require_unconfigured_platform_role() -> None:
    violations = _violations()
    assert violations == [], (
        "기본 제공 capability 가 Platform role 자원을 무조건 요구하면 그 role 이 없는 Runtime"
        "(Generated Domain 등)이 기동하지 못한다. Platform role 조건을 함께 걸어야 한다:"
        f" {violations}"
    )


def test_scan_actually_finds_default_on_capabilities() -> None:
    # 기본 제공 AutoConfiguration 이 0건이면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    default_on = [
        path.relative_to(ROOT).as_posix() for path in _auto_configurations()
        if DEFAULT_ON.search(
            io.open(path, encoding="utf-8", errors="replace").read().split("public class")[0])
    ]
    assert len(default_on) >= 1, f"default-on auto configurations not detected: {default_on}"
    assert len(_domain_starter_modules()) >= 3, f"domain starters not resolved: {_domain_starter_modules()}"
    assert len(_auto_configurations()) >= 3, f"auto configurations not scanned: {len(_auto_configurations())}"


def test_annotation_and_method_patterns_use_regex_boundaries_not_control_characters() -> None:
    """A pasted Backspace turns `\\b` into a literal control byte and silently weakens the scan."""
    assert METHOD_NAME.search("void provider(").group(1) == "provider"
    assert "\x08" not in METHOD_NAME.pattern
    assert "\x08" not in DEFAULT_ON.pattern
    assert "\x08" not in BEAN_OPT_IN.pattern

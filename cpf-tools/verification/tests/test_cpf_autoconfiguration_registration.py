"""@AutoConfiguration 클래스가 실제로 등록되어 있음을 보장한다.

Spring Boot 는 AutoConfiguration.imports 에 나열된 클래스만 활성화한다. @AutoConfiguration 을
붙였지만 등록하지 않으면 컴파일도 통과하고 단위테스트도 통과하지만, Runtime 에서 Bean 이
조용히 사라진다. 소비자는 'required a bean ... that could not be found' 로만 실패하므로 원인을
찾기 어렵다.

실제로 CpfAttachmentAutoConfiguration 이 등록되지 않아 CpfAttachmentStoragePort 가 만들어지지
않았고, 1-WAS 기동이 실패했다. 같은 결함을 정적으로 차단한다.
"""
from __future__ import annotations

import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
IMPORTS_RELATIVE = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git", "generated"}
EXCLUDED_PREFIXES = ("cpf-release/",)

# 일부 소스는 package/import/annotation 이 한 줄로 압축되어 있다. 줄 시작에 고정하면
# 그런 파일을 통째로 놓쳐 게이트가 조용히 비어 버린다.
ANNOTATION = re.compile(r"(?<![\w.])@AutoConfiguration\b")
PACKAGE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)


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


@lru_cache(maxsize=1)
def _registered() -> set[str]:
    names: set[str] = set()
    for path in ROOT.rglob(IMPORTS_RELATIVE):
        relative = path.relative_to(ROOT).as_posix()
        if set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS:
            continue
        if relative.startswith(EXCLUDED_PREFIXES):
            continue
        for line in path.read_text(encoding="utf-8").splitlines():
            value = line.strip()
            if value and not value.startswith("#"):
                names.add(value)
    return names


@lru_cache(maxsize=1)
def _declared() -> dict[str, Path]:
    declared: dict[str, Path] = {}
    for path in _sources():
        text = path.read_text(encoding="utf-8", errors="replace")
        if not ANNOTATION.search(text):
            continue
        package = PACKAGE.search(text)
        if not package:
            continue
        declared[f"{package.group(1)}.{path.stem}"] = path
    return declared


# 현재 어떤 imports 에도 없고 어디에서도 @Import 되지 않아 Runtime 에서 비활성인 항목이다.
# 등록하면 새 Bean 이 생겨 Runtime 동작이 바뀌므로, 활성화가 필요하다는 근거가 확인되기 전에는
# 임의로 켜지 않는다. 새로운 미등록 AutoConfiguration 이 유입되는 것은 아래 계약이 막는다.
KNOWN_INACTIVE = {
    "com.cpf.data.cache.caffeine.CpfCacheAutoConfiguration",
    "com.cpf.file.attachment.runtimecontrol.CpfAttachmentRuntimeControlAutoConfiguration",
    "com.cpf.file.tabular.CpfTabularAutoConfiguration",
    "com.cpf.platform.operations.observability.otlp.CpfOtlpTelemetryAutoConfiguration",
}


def test_every_autoconfiguration_class_is_registered() -> None:
    declared = _declared()
    missing = sorted(set(declared) - _registered() - KNOWN_INACTIVE)
    assert missing == [], (
        "@AutoConfiguration 인데 AutoConfiguration.imports 에 등록되지 않았다"
        f" (Runtime 에서 Bean 이 조용히 사라진다): {missing}"
    )


def test_known_inactive_entries_are_still_unregistered() -> None:
    # 등록된 뒤에도 목록에 남아 있으면, 다음 미등록 결함을 이 목록이 가려 버린다.
    declared = _declared()
    stale = sorted(name for name in KNOWN_INACTIVE
                   if name in _registered() or name not in declared)
    assert stale == [], f"KNOWN_INACTIVE 목록이 현행과 어긋난다(등록됨 또는 사라짐): {stale}"


@lru_cache(maxsize=1)
def _all_source_classes() -> set[str]:
    names: set[str] = set()
    for path in _sources():
        package = PACKAGE.search(path.read_text(encoding="utf-8", errors="replace"))
        if package:
            names.add(f"{package.group(1)}.{path.stem}")
    return names


def test_registered_entries_point_at_existing_classes() -> None:
    # imports 에는 @Configuration 클래스도 올릴 수 있으므로 @AutoConfiguration 여부가 아니라
    # 클래스 존재 여부로 판정한다. 등록만 남고 클래스가 사라지면 기동 시점에 로딩이 실패한다.
    dangling = sorted(name for name in _registered() if name not in _all_source_classes())
    assert dangling == [], f"AutoConfiguration.imports 항목의 클래스가 없다: {dangling}"


def test_scan_actually_covers_autoconfigurations() -> None:
    # 대상이 0건이면 위 두 계약은 언제나 통과하는 빈 게이트가 된다.
    assert len(_declared()) > 30, f"@AutoConfiguration classes not scanned: {len(_declared())}"
    assert len(_registered()) > 30, f"AutoConfiguration.imports not scanned: {len(_registered())}"

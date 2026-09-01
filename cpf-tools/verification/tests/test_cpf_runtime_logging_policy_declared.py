"""모든 실행 Runtime이 cpf.logging.files를 선언했는지 보장한다.

CpfRuntimeLoggingAutoConfiguration은 matchIfMissing=true로 항상 활성이고
CpfApplicationLoggingPolicyValidator는 빈 files를 거부한다. 즉 선언하지 않은 Runtime은
기동 자체가 불가능하다. 그런데 이 계약은 Generated Domain 템플릿에만 반영되어 있었고
Gateway/ADM/Backoffice/Batch 등 Platform Runtime 12개는 선언 없이 남아 있었다.

컴파일도 단위테스트도 이 결함을 잡지 못한다. 실제 기동에서만 드러나며, 다른 Bean 생성
실패가 먼저 보고되면 그 뒤에 숨는다. 정적으로 강제한다.
"""
from __future__ import annotations

import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
EXCLUDED_PARTS = {"build", "bin", "out", "node_modules", ".git"}
EXCLUDED_PREFIXES = ("cpf-release/",)
BOOT_APP = re.compile(r"@SpringBootApplication\b")
BOOT_MANUAL = re.compile(r"@EnableAutoConfiguration\b")


@lru_cache(maxsize=1)
def _runtime_modules() -> list[str]:
    modules: set[str] = set()
    for path in ROOT.rglob("*.java"):
        relative = path.relative_to(ROOT).as_posix()
        if set(path.relative_to(ROOT).parts) & EXCLUDED_PARTS:
            continue
        if relative.startswith(EXCLUDED_PREFIXES) or "/src/main/java/" not in relative:
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        if BOOT_APP.search(text) or (
            BOOT_MANUAL.search(text) and "SpringApplication" in text
        ):
            modules.add(relative.split("/src/main/java/")[0])
    return sorted(modules)


def _declares_log_files(module: str) -> bool:
    """cpf: -> logging: -> files: 경로를 들여쓰기로 확인한다.

    application.yml 전체를 파싱하지 않는 것은 PyYAML 이 없는 환경에서도 게이트가 돌아야
    하기 때문이다. 정본 Runtime YML 은 모두 2-space 들여쓰기를 쓴다.
    """
    config = ROOT / module / "src/main/resources/application.yml"
    if not config.is_file():
        return False
    in_cpf = in_logging = False
    for line in config.read_text(encoding="utf-8", errors="replace").splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        if not line.startswith(" "):
            in_cpf = line.rstrip() == "cpf:"
            in_logging = False
            continue
        if in_cpf and line.startswith("  ") and not line.startswith("   "):
            in_logging = line.rstrip() == "  logging:"
            continue
        if in_logging and line.startswith("    ") and not line.startswith("     "):
            if line.rstrip() == "    files:":
                return True
    return False


def test_every_runtime_declares_log_file_policy() -> None:
    missing = [m for m in _runtime_modules() if not _declares_log_files(m)]
    assert missing == [], (
        "cpf.logging.files 를 선언하지 않은 Runtime 은 기동할 수 없다"
        f" (CpfApplicationLoggingPolicyValidator 가 빈 files 를 거부): {missing}"
    )


def test_scan_actually_finds_runtimes() -> None:
    # Runtime 이 0건이면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    assert len(_runtime_modules()) >= 10, f"boot runtimes not scanned: {_runtime_modules()}"

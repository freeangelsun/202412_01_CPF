#!/usr/bin/env python3
"""Fail-closed Windows path compatibility gate for CPF source paths.

CPF의 Project Root 상대경로(파일명 포함)는 Windows 호환성을 위해 200자를 넘을 수 없습니다.
FullLocal은 실제 repository-root를 반영한 보수적인 전체 경로 예산도 별도로 검증합니다.
"""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass

import argparse
import re
from pathlib import Path

DEFAULT_RELATIVE_BUDGET = 200
DEFAULT_FULL_BUDGET = 240
FORBIDDEN_SEGMENT_PATTERNS = (
    re.compile(r"^REV[-_]?\d+$", re.I),
    re.compile(r"^SESSION[-_]?\d+$", re.I),
    re.compile(r"^\d{8}$"),
    re.compile(r"^FINAL_FINAL$", re.I),
)
EPHEMERAL_SEGMENTS = {".git", ".gradle", ".pytest_cache", "__pycache__", "node_modules", ".venv", ".cpf-python", "dist"}
VERSIONED_DIR_EXEMPT_PREFIXES = ("cpf-docs/deliverables/",)
# cpf-docs/governance/development-harness/evidence/platform/current/generated/** is machine-local, fully gitignored regeneration output
# (redirected Gradle project cache/local artifact repository, JVM crash/heap-dump capture,
# retired IDE cache snapshots). It is never product source and is recreated on every run, so it
# follows the same ephemeral-output policy as an ordinary module build/** directory below.
EPHEMERAL_PREFIXES = (
    "cpf-docs/governance/development-harness/evidence/platform/current/generated/",
    # cpf-release/** 는 릴리즈 생성 산출물이며 canonical Source Identity(cpf-source-state.py 의
    # GENERATED_PARTS)도 제품 Source 로 계산하지 않는다. 사용자가 clone 하는 대상이 아니고
    # 릴리즈를 한 번 만들었는지에 따라 같은 Source 가 PASS/FAIL 로 갈리면 안 된다.
    "cpf-release/",
)


def is_managed_source(path: Path, root: Path) -> bool:
    rel = path.relative_to(root)
    rel_posix = rel.as_posix()
    parts = rel.parts
    if any(part in EPHEMERAL_SEGMENTS for part in parts):
        return False
    if any(rel_posix.startswith(prefix) for prefix in EPHEMERAL_PREFIXES):
        return False
    # Ordinary module build output is generated, but cpf-tools/build/** is product source.
    if "build" in parts and not (len(parts) >= 2 and parts[0] == "cpf-tools" and parts[1] == "build"):
        return False
    return True


def projected_length(target_root: str, rel: str) -> int:
    root_text = target_root.rstrip("\\/")
    return len(root_text) + (1 if root_text else 0) + len(rel)


def is_versioned_dir_exempt(rel: str) -> bool:
    """Allow dated archival folders only under protected deliverables.

    This exemption applies to the naming rule only. Path-length budgets still
    cover every managed file under the protected path.
    """
    normalized = rel.replace("\\", "/")
    return any(normalized.startswith(prefix) for prefix in VERSIONED_DIR_EXEMPT_PREFIXES)


def is_captured_runtime_daily_log_roll(parts: tuple[str, ...], index: int) -> bool:
    """True only for the product's own canonical daily transaction-log roll segment.

    The FORBIDDEN_VERSIONED_DIR rule targets hand-made version-stamped working
    directories (``REV_1``, ``SESSION-2``, ``FINAL_FINAL``, a dated copy of a
    document tree). It must not reject the product's *own* runtime output: the
    canonical logging contract rolls transaction logs daily into
    ``.../logs/**/transactions/<YYYYMMDD>/`` (daily roll, 5-day compression,
    365-day deletion). Captured Evidence of a real Runtime therefore legitimately
    contains 8-digit directory segments, and forbidding them outright would put
    two canonical Requirements in direct contradiction.

    The exemption is deliberately narrow and evaluated per segment: the 8-digit
    segment must sit directly under a ``transactions`` directory that is itself
    below a ``logs`` directory. Every other version-stamped directory in the same
    path still fails independently.
    """
    if not re.fullmatch(r"\d{8}", parts[index]):
        return False
    if index == 0 or parts[index - 1] != "transactions":
        return False
    return "logs" in parts[: index - 1]


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--target-root-text", default="", help="Windows repository root used only for path-length projection")
    ap.add_argument("--max-relative-path", type=int, default=DEFAULT_RELATIVE_BUDGET)
    ap.add_argument("--max-full-path", type=int, default=DEFAULT_FULL_BUDGET)
    ap.add_argument("--strict-relative-budget", action="store_true", help="호환 옵션입니다. 상대경로 200자 제한은 항상 fail-closed입니다.")
    ns = ap.parse_args()

    root = Path(ns.root).resolve()
    target_root = ns.target_root_text or str(root)
    failures: list[str] = []
    warnings: list[str] = []
    files = [p for p in root.rglob("*") if p.is_file() and is_managed_source(p, root)]
    max_seen = (0, "")
    max_full = (0, "")

    for path in files:
        rel = path.relative_to(root).as_posix()
        rel_len = len(rel)
        full_len = projected_length(target_root, rel)
        if rel_len > max_seen[0]:
            max_seen = (rel_len, rel)
        if full_len > max_full[0]:
            max_full = (full_len, rel)
        if rel_len > ns.max_relative_path:
            failures.append(f"RELATIVE_PATH_TOO_LONG {rel_len} {rel}")
        if full_len > ns.max_full_path:
            failures.append(f"FULL_PATH_TOO_LONG {full_len} {rel}")
        if not is_versioned_dir_exempt(rel):
            rel_parts = Path(rel).parts
            for index, segment in enumerate(rel_parts[:-1]):
                if not any(rx.match(segment) for rx in FORBIDDEN_SEGMENT_PATTERNS):
                    continue
                if is_captured_runtime_daily_log_roll(rel_parts, index):
                    continue
                failures.append(f"FORBIDDEN_VERSIONED_DIR {segment} {rel}")

    print(f"WINDOWS_PATH_FILES={len(files)}")
    print(f"WINDOWS_PATH_MAX_RELATIVE={max_seen[0]} {max_seen[1]}")
    print(f"WINDOWS_PATH_MAX_FULL={max_full[0]} {max_full[1]}")
    print(f"WINDOWS_PATH_TARGET_ROOT={target_root}")
    print(f"WINDOWS_PATH_RELATIVE_BUDGET={ns.max_relative_path}")
    print(f"WINDOWS_PATH_FULL_BUDGET={ns.max_full_path}")
    print(f"WINDOWS_PATH_WARNINGS={len(warnings)}")
    for warning in warnings:
        print(warning)
    print(f"WINDOWS_PATH_FAILURES={len(failures)}")
    for failure in failures:
        print(failure)
    if failures:
        print("WINDOWS_PATH_COMPATIBILITY=FAIL")
        return 1
    print("WINDOWS_PATH_COMPATIBILITY=PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

"""Product SQL 이 정본 테이블명을 정본 대소문자로 조회하는지 검증한다.

정본 스키마는 대문자 테이블(`ADM_FILE_JOB`)을 렌더링한다. 그런데 Product SQL 일부가
소문자(`adm_file_job`)로 조회하고 있었다.

- Windows MariaDB 는 식별자 대소문자를 접어서 우연히 동작한다.
- **Linux MariaDB 는 보존한다.** 그래서 Docker 검증 DB 에서
  `Table 'cpf_verify_..._runtime.adm_file_job' doesn't exist` 가 연속 발생했고
  1-WAS 의 후속 검증 단계가 전부 실패했다.
- PostgreSQL 은 미인용 식별자를 소문자로, Oracle 은 대문자로 폴딩하므로 **대문자 표기는 3개 공식
  벤더 모두에서 동작한다.**

따라서 정본 대문자로 통일한다. 이 게이트가 소문자 회귀를 실행 없이 즉시 잡는다.
"""
from __future__ import annotations

import io
import json
import os
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCHEMA = ROOT / "cpf-tools/db/canonical/platform-schema.json"
SKIP_PARTS = {"build", "bin", "out", "node_modules", ".git", "cpf-release"}
SCAN_ROOTS = ("cpf-admin", "cpf-common", "cpf-core", "cpf-gateway", "cpf-batch",
              "cpf-starters", "cpf-backoffice", "cpf-backoffice-web", "cpf-education",
              "cpf-internal", "cpf-framework")
SUFFIXES = (".java", ".xml", ".sql")
# `FROM x` / `JOIN x` / `INTO x` / `UPDATE x` 뒤의 식별자만 테이블 참조로 본다.
REFERENCE = re.compile(r"\b(?:FROM|JOIN|INTO|UPDATE)\s+([A-Za-z_][A-Za-z0-9_]*)", re.I)


@lru_cache(maxsize=1)
def _canonical_names() -> frozenset[str]:
    document = json.load(io.open(SCHEMA, encoding="utf-8"))
    return frozenset(table["name"] for table in document["tables"])


@lru_cache(maxsize=1)
def _sources() -> tuple[Path, ...]:
    files: list[Path] = []
    for root in SCAN_ROOTS:
        base = ROOT / root
        if not base.is_dir():
            continue
        for current, directories, names in os.walk(base):
            directories[:] = [d for d in directories if d not in SKIP_PARTS]
            for name in names:
                if name.endswith(SUFFIXES):
                    files.append(Path(current) / name)
    return tuple(files)


def _violations() -> list[str]:
    canonical = _canonical_names()
    lower_to_canonical = {name.lower(): name for name in canonical}
    found: list[str] = []
    for path in _sources():
        text = io.open(path, encoding="utf-8", errors="replace").read()
        for match in REFERENCE.finditer(text):
            token = match.group(1)
            if token in canonical:
                continue
            expected = lower_to_canonical.get(token.lower())
            if expected and token != expected:
                line = text.count(chr(10), 0, match.start()) + 1
                found.append(f"{path.relative_to(ROOT).as_posix()}:{line} {token} -> {expected}")
    return sorted(set(found))


def test_product_sql_uses_canonical_table_case() -> None:
    violations = _violations()
    assert violations == [], (
        "정본 스키마는 대문자 테이블명을 만든다. Linux MariaDB 는 식별자 대소문자를 보존하므로 "
        "소문자 조회는 'Table ... doesn't exist' 로 실패한다: "
        f"{violations[:20]} (총 {len(violations)}건)")


def test_scan_actually_covers_product_sql() -> None:
    # 빈 게이트 방지: 실제로 정본 테이블을 조회하는 파일이 충분히 스캔되어야 한다.
    canonical = _canonical_names()
    assert len(canonical) >= 200, len(canonical)
    referencing = 0
    for path in _sources():
        text = io.open(path, encoding="utf-8", errors="replace").read()
        if any(match.group(1) in canonical for match in REFERENCE.finditer(text)):
            referencing += 1
    assert referencing >= 30, referencing


def test_pattern_has_no_control_characters() -> None:
    assert chr(8) not in REFERENCE.pattern
    assert chr(11) not in REFERENCE.pattern
    assert REFERENCE.findall("SELECT * FROM adm_file_job WHERE") == ["adm_file_job"]
    assert REFERENCE.findall("UPDATE ADM_FILE_JOB SET") == ["ADM_FILE_JOB"]

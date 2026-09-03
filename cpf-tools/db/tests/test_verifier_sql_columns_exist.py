"""검증기 SQL이 정본 스키마에 실재하는 컬럼만 쓰는지 확인한다.

같은 실수를 두 번 했다.

1. 진단 SQL 이 `BAT_CENTER_CUT_ITEM.item_state` 를 읽었는데 정본 컬럼은 `item_status` 였다.
   진단문이 먼저 던져 **정작 조사하려던 실패 원인을 가렸다.**
2. segment 진단에 `CPF_TRANSACTION_SEGMENT.failure_message` 를 넣었는데 정본 컬럼은
   `failure_message_masked` 였다. `ERROR 1054 Unknown column` 으로 Runtime 한 사이클을 소모했다.

컬럼명은 추측하지 말고 정본(`cpf-tools/db/canonical/platform-schema.json`)에서 확인해야 한다.
이 게이트가 그 규칙을 정적으로 강제한다. 실행 없이 즉시 검출되므로 Runtime 사이클을 태우지 않는다.
"""
from __future__ import annotations

import io
import json
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCHEMA = ROOT / "cpf-tools/db/canonical/platform-schema.json"

# `${DatabaseName}.<TABLE>` 로 정본 Platform DB 를 직접 조회하는 검증기들이다.
VERIFIER_SCRIPTS = (
    "cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1",
)

# `JSON_OBJECT('alias',column, ...)` 형태에서 컬럼 식별자만 뽑는다.
JSON_OBJECT_COLUMN = re.compile(r"'[A-Za-z][A-Za-z0-9]*',\s*([a-z_][a-z0-9_]*)")
QUALIFIED_TABLE = re.compile(r"\$\{DatabaseName\}\.([A-Z][A-Z0-9_]*)")
# SQL 함수/리터럴은 컬럼이 아니다.
NOT_A_COLUMN = {"null", "count", "max", "min", "sum", "coalesce", "concat_ws", "json_object"}


@lru_cache(maxsize=1)
def _canonical_columns() -> dict[str, set[str]]:
    document = json.load(io.open(SCHEMA, encoding="utf-8"))
    return {
        table["name"]: {column["name"].lower() for column in table["columns"]}
        for table in document["tables"]
    }


def _statements(text: str) -> list[tuple[str, str]]:
    """(table, statement) 목록. JSON_OBJECT 투영을 쓰는 단일 테이블 조회만 대상으로 한다."""
    found: list[tuple[str, str]] = []
    for match in re.finditer(
            r"SELECT JSON_OBJECT\((.*?)\)\s*FROM \$\{DatabaseName\}\.([A-Z][A-Z0-9_]*)",
            text, re.S):
        found.append((match.group(2), match.group(1)))
    return found


def test_verifier_json_projections_use_existing_columns() -> None:
    canonical = _canonical_columns()
    violations: list[str] = []
    scanned = 0
    for relative in VERIFIER_SCRIPTS:
        text = io.open(ROOT / relative, encoding="utf-8-sig").read()
        for table, projection in _statements(text):
            scanned += 1
            if table not in canonical:
                violations.append(f"{relative}: unknown table {table}")
                continue
            for column in JSON_OBJECT_COLUMN.findall(projection):
                if column in NOT_A_COLUMN:
                    continue
                if column not in canonical[table]:
                    violations.append(f"{relative}: {table}.{column}")
    assert scanned >= 3, f"scanned only {scanned} projections"
    assert violations == [], (
        "검증기 SQL 이 정본에 없는 컬럼을 읽는다. 진단문이 먼저 던지면 정작 조사하려던 실패 "
        f"원인이 가려진다: {violations}")


def test_referenced_tables_are_declared() -> None:
    canonical = _canonical_columns()
    unknown: list[str] = []
    for relative in VERIFIER_SCRIPTS:
        text = io.open(ROOT / relative, encoding="utf-8-sig").read()
        for table in sorted(set(QUALIFIED_TABLE.findall(text))):
            # information_schema 등 정본 밖 조회는 대상이 아니다.
            if table.startswith(("OPS_", "BAT_", "CPF_", "ADM_", "CMN_", "GW_", "SEC_")) \
                    and table not in canonical:
                unknown.append(f"{relative}: {table}")
    assert unknown == [], unknown


def test_patterns_do_not_contain_control_characters() -> None:
    for pattern in (JSON_OBJECT_COLUMN, QUALIFIED_TABLE):
        assert chr(8) not in pattern.pattern
        assert chr(11) not in pattern.pattern
    assert JSON_OBJECT_COLUMN.findall("'errorMessage',failure_message_masked,") == [
        "failure_message_masked"]

"""Query Pack 템플릿이 참조하는 컬럼이 Canonical Schema 에 실재함을 보장한다.

Migration 으로 컬럼을 바꾸면 Query Pack 템플릿은 자동으로 따라오지 않는다. 컴파일도 통과하고
단위테스트도 통과하지만, Runtime 이 그 SQL 을 처음 실행하는 순간 'Unknown column' 으로 기동이
실패한다. 실제로 V127 이 OPS_CHANNEL_EXECUTION_POLICY 의 컬럼을 정리한 뒤에도 템플릿이
original_channel_code / caller_channel_code / request_type 를 계속 조회해 Gateway 기동이 실패했다.

Migration 파일은 불변 이력이므로 검사 대상이 아니다. 검사 대상은 Runtime 이 실제로 실행하는
Query Pack 템플릿뿐이다.
"""
from __future__ import annotations

import json
import re
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
# Runtime 이 실제로 실행하는 것은 vendor Query Pack 이다. 템플릿만 검사하면 Pack 이 뒤처져도
# 게이트가 통과해 버린다(실제로 그렇게 놓쳤다). 둘 다 검사한다.
TEMPLATE_ROOT = ROOT / "cpf-tools/db/runtime-template"
VENDOR_PACK_ROOT = ROOT / "cpf-tools/db/vendor"
CANONICAL_SCHEMA = ROOT / "cpf-tools/db/canonical/platform-schema.json"

# 템플릿이 다루는 테이블을 찾는다. 대문자 테이블명 규칙을 그대로 쓴다.
TABLE_REFERENCE = re.compile(
    r"\b(?:FROM|INTO|UPDATE|MERGE\s+INTO|JOIN)\s+([A-Z][A-Z0-9_]{3,})\b")
# 컬럼 후보. SQL 키워드/바인드/치환 토큰은 아래에서 제외한다.
WORD = re.compile(r"(?<![\w.@])([a-z][a-z0-9_]{2,})(?![\w(])")

SQL_WORDS = {
    "select", "from", "where", "insert", "into", "values", "update", "set", "delete",
    "merge", "using", "when", "matched", "then", "not", "and", "or", "order", "group",
    "by", "asc", "desc", "join", "left", "right", "inner", "outer", "on", "as", "case",
    "end", "null", "is", "in", "exists", "distinct", "limit", "offset", "having", "union",
    "all", "dual", "conflict", "do", "nothing", "excluded", "target", "source", "duplicate",
    "key", "count", "max", "min", "sum", "avg", "coalesce", "cast", "current_timestamp",
    "with", "recursive", "for", "of", "returning", "true", "false", "between", "like",
    "add", "column", "table", "alter", "create", "drop", "constraint", "foreign",
    "references", "primary", "unique", "index", "default", "and_", "value", "row",
}


@lru_cache(maxsize=1)
def _schema_columns() -> dict[str, set[str]]:
    model = json.loads(CANONICAL_SCHEMA.read_text(encoding="utf-8"))
    tables: dict[str, set[str]] = {}

    def walk(node: object) -> None:
        if isinstance(node, dict):
            name = node.get("tableName") or node.get("table") or node.get("name")
            columns = node.get("columns")
            if isinstance(name, str) and isinstance(columns, list) and columns:
                names: set[str] = set()
                for column in columns:
                    if isinstance(column, str):
                        names.add(column.lower())
                    elif isinstance(column, dict):
                        value = column.get("name") or column.get("columnName")
                        if isinstance(value, str):
                            names.add(value.lower())
                if names:
                    tables.setdefault(name.upper(), set()).update(names)
            for value in node.values():
                walk(value)
        elif isinstance(node, list):
            for value in node:
                walk(value)

    walk(model)
    return tables


@lru_cache(maxsize=1)
def _templates() -> list[Path]:
    files: list[Path] = []
    if TEMPLATE_ROOT.is_dir():
        files.extend(TEMPLATE_ROOT.rglob("*.sql.template"))
    if VENDOR_PACK_ROOT.is_dir():
        # migration/rollback 은 불변 이력이므로 제외하고, Runtime 이 읽는 Query Pack 만 본다.
        for path in VENDOR_PACK_ROOT.glob("*/runtime/**/*.sql"):
            files.append(path)
    return sorted(files)


def _declared_columns(text: str) -> set[str]:
    """컬럼임이 문법적으로 확실한 위치만 모은다.

    SELECT 목록은 별칭(AS x)과 CTE 이름이 섞여 정적으로 컬럼과 구분되지 않는다. 오탐을 만들면
    게이트를 신뢰할 수 없게 되므로, INSERT 컬럼 목록과 SET 대상처럼 컬럼만 올 수 있는 자리만
    검사한다. 실제 결함(V127 이후 템플릿 드리프트)도 이 두 자리에 나타난다.
    """
    columns: set[str] = set()
    for block in re.findall(r"INSERT\s+(?:INTO\s+[A-Z][A-Z0-9_]*\s*)?\(([^)]*)\)", text, re.IGNORECASE):
        for token in block.split(","):
            name = token.strip().lower()
            if re.fullmatch(r"[a-z][a-z0-9_]*", name):
                columns.add(name)
    for name in re.findall(r"(?:^|,|SET\s+)\s*(?:target\.)?([a-z][a-z0-9_]{2,})\s*=", text, re.MULTILINE):
        columns.add(name.lower())
    return columns


def _violations() -> list[str]:
    schema = _schema_columns()
    found: list[str] = []
    for path in _templates():
        text = path.read_text(encoding="utf-8", errors="replace")
        tables = {name.upper() for name in TABLE_REFERENCE.findall(text)}
        known = [name for name in tables if name in schema]
        if len(known) != 1:
            # 다중 테이블 SQL 은 컬럼 소유 테이블을 정적으로 특정할 수 없다. 오탐을 만들지 않는다.
            continue
        table = known[0]
        allowed = schema[table]
        for column in _declared_columns(text):
            if column in SQL_WORDS or column in allowed:
                continue
            found.append(f"{path.relative_to(ROOT).as_posix()}:{table}.{column}")
    return sorted(set(found))


def test_query_templates_reference_existing_columns() -> None:
    violations = _violations()
    assert violations == [], (
        "Query Pack 템플릿이 Canonical Schema 에 없는 컬럼을 참조한다"
        f" (Runtime 에서 Unknown column 으로 기동 실패): {violations}"
    )


def test_scan_actually_covers_templates() -> None:
    # 대상이 0건이면 위 계약은 언제나 통과하는 빈 게이트가 된다.
    assert len(_templates()) > 20, f"query templates not scanned: {len(_templates())}"
    assert len(_schema_columns()) > 50, f"canonical schema not parsed: {len(_schema_columns())}"

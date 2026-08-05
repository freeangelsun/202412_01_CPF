#!/usr/bin/env python3
"""Generate fail-closed, archive-before-purge CPF retention SQL."""
from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import re
from pathlib import Path
from typing import Any

IDENT = re.compile(r"^[A-Za-z][A-Za-z0-9_]{0,62}$")
VENDORS = ("mariadb", "postgresql", "oracle")
ARCHIVE_METADATA_COLUMNS = ("archived_at", "archived_by", "archive_reason")


def ident(value: str) -> str:
    if not IDENT.fullmatch(value):
        raise ValueError(f"unsafe SQL identifier: {value!r}")
    return value


def literal(value: str) -> str:
    if any(ord(ch) < 32 or ord(ch) == 127 for ch in value):
        raise ValueError("SQL literal contains control characters")
    return "'" + value.replace("'", "''") + "'"


def load(path: Path) -> dict[str, Any]:
    value = json.loads(path.read_text(encoding="utf-8-sig"))
    if not isinstance(value, dict):
        raise ValueError(f"JSON root must be object: {path}")
    return value


def select_policy(contract: dict[str, Any], policy_id: str) -> dict[str, Any]:
    found = [policy for policy in contract["policies"] if policy["policyId"] == policy_id]
    if len(found) != 1:
        raise ValueError(f"policy must exist exactly once: {policy_id}")
    return found[0]


def schema_table(schema: dict[str, Any], table_name: str) -> dict[str, Any]:
    found = [table for table in schema.get("tables", []) if str(table.get("name", "")).casefold() == table_name.casefold()]
    if len(found) != 1:
        raise ValueError(f"canonical table must exist exactly once: {table_name}")
    return found[0]


def resolve_archive_contract(schema: dict[str, Any], policy: dict[str, Any]) -> tuple[list[str], list[str]]:
    source = schema_table(schema, policy["sourceTable"])
    archive = schema_table(schema, policy["archiveTable"])
    if source.get("logicalDatabase") != policy.get("logicalDatabase") or archive.get("logicalDatabase") != policy.get("logicalDatabase"):
        raise ValueError("retention source/archive logical database mismatch")
    source_columns = [ident(str(column["name"])) for column in source.get("columns", [])]
    archive_columns = [ident(str(column["name"])) for column in archive.get("columns", [])]
    if not source_columns:
        raise ValueError("retention source table has no columns")
    expected_archive = source_columns + list(ARCHIVE_METADATA_COLUMNS)
    if [value.casefold() for value in archive_columns] != [value.casefold() for value in expected_archive]:
        raise ValueError(
            "archive table columns must equal source columns followed by " + ",".join(ARCHIVE_METADATA_COLUMNS)
        )
    primary_key = ident(policy["primaryKeyColumn"])
    timestamp = ident(policy["timestampColumn"])
    source_set = {value.casefold() for value in source_columns}
    if primary_key.casefold() not in source_set or timestamp.casefold() not in source_set:
        raise ValueError("retention primary/timestamp column is absent from source table")
    return source_columns, archive_columns


def holds_clause(vendor: str, primary_key: str, keys: list[str]) -> str:
    if not keys:
        return "1=1"
    values = ", ".join(literal(key) for key in keys)
    cast = {
        "mariadb": f"CAST(s.{primary_key} AS CHAR)",
        "postgresql": f"CAST(s.{primary_key} AS TEXT)",
        "oracle": f"TO_CHAR(s.{primary_key})",
    }[vendor]
    return f"{cast} NOT IN ({values})"


def generate(
    vendor: str,
    policy: dict[str, Any],
    keys: list[str],
    cutoff: str,
    source_columns: list[str],
    archive_columns: list[str],
    operator: str,
    reason: str,
) -> str:
    if vendor not in VENDORS:
        raise ValueError(f"unsupported vendor: {vendor}")
    source = ident(policy["sourceTable"])
    archive = ident(policy["archiveTable"])
    primary_key = ident(policy["primaryKeyColumn"])
    timestamp = ident(policy["timestampColumn"])
    limit = int(policy["maxRowsPerRun"])
    if not 1 <= limit <= 1_000_000:
        raise ValueError("maxRowsPerRun out of range")
    hold = holds_clause(vendor, primary_key, keys)
    archive_column_sql = ", ".join(archive_columns)
    source_projection = ", ".join(f"s.{column}" for column in source_columns)
    audit_projection = f"CURRENT_TIMESTAMP, {literal(operator)}, {literal(reason)}"

    if vendor == "mariadb":
        return f"""START TRANSACTION;
CREATE TEMPORARY TABLE cpf_retention_candidates (record_key VARCHAR(256) PRIMARY KEY);
INSERT INTO cpf_retention_candidates(record_key)
SELECT CAST(s.{primary_key} AS CHAR) FROM {source} s
WHERE s.{timestamp} < TIMESTAMP {literal(cutoff)} AND {hold}
ORDER BY s.{timestamp}, s.{primary_key} LIMIT {limit};
INSERT INTO {archive} ({archive_column_sql})
SELECT {source_projection}, {audit_projection}
FROM {source} s JOIN cpf_retention_candidates c ON c.record_key=CAST(s.{primary_key} AS CHAR)
WHERE NOT EXISTS (SELECT 1 FROM {archive} a WHERE a.{primary_key}=s.{primary_key});
DELETE s FROM {source} s JOIN cpf_retention_candidates c ON c.record_key=CAST(s.{primary_key} AS CHAR)
WHERE EXISTS (SELECT 1 FROM {archive} a WHERE a.{primary_key}=s.{primary_key});
COMMIT;
"""
    if vendor == "postgresql":
        return f"""BEGIN;
CREATE TEMP TABLE cpf_retention_candidates (record_key TEXT PRIMARY KEY) ON COMMIT DROP;
INSERT INTO cpf_retention_candidates(record_key)
SELECT CAST(s.{primary_key} AS TEXT) FROM {source} s
WHERE s.{timestamp} < TIMESTAMPTZ {literal(cutoff)} AND {hold}
ORDER BY s.{timestamp}, s.{primary_key} LIMIT {limit};
INSERT INTO {archive} ({archive_column_sql})
SELECT {source_projection}, {audit_projection}
FROM {source} s JOIN cpf_retention_candidates c ON c.record_key=CAST(s.{primary_key} AS TEXT)
WHERE NOT EXISTS (SELECT 1 FROM {archive} a WHERE a.{primary_key}=s.{primary_key});
DELETE FROM {source} s USING cpf_retention_candidates c
WHERE CAST(s.{primary_key} AS TEXT)=c.record_key
AND EXISTS (SELECT 1 FROM {archive} a WHERE a.{primary_key}=s.{primary_key});
COMMIT;
"""
    candidate = (
        f"SELECT s.{primary_key} FROM {source} s "
        f"WHERE s.{timestamp} < TO_TIMESTAMP_TZ({literal(cutoff)}, 'YYYY-MM-DD\"T\"HH24:MI:SSTZH:TZM') "
        f"AND {hold} ORDER BY s.{timestamp}, s.{primary_key} FETCH FIRST {limit} ROWS ONLY"
    )
    return f"""WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET ECHO OFF
SET VERIFY OFF
SET DEFINE OFF
INSERT INTO {archive} ({archive_column_sql})
SELECT {source_projection}, {audit_projection}
FROM {source} s WHERE s.{primary_key} IN ({candidate})
AND NOT EXISTS (SELECT 1 FROM {archive} a WHERE a.{primary_key}=s.{primary_key});
DELETE FROM {source} s WHERE s.{primary_key} IN ({candidate})
AND EXISTS (SELECT 1 FROM {archive} a WHERE a.{primary_key}=s.{primary_key});
COMMIT;
EXIT
"""


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--vendor", required=True, choices=VENDORS)
    parser.add_argument("--policy-id", required=True)
    parser.add_argument("--legal-hold-manifest", required=True)
    parser.add_argument("--cutoff-utc", required=True)
    parser.add_argument("--operator", required=True)
    parser.add_argument("--reason", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    root = Path(args.root).resolve()
    contract = load(root / "cpf-tools/db/cpf-data-retention-policy.json")
    schema = load(root / "cpf-tools/db/canonical/platform-schema.json")
    policy = select_policy(contract, args.policy_id)
    source_columns, archive_columns = resolve_archive_contract(schema, policy)
    hold = load(Path(args.legal_hold_manifest))
    for field in contract["legalHoldManifest"]["requiredFields"]:
        if field not in hold:
            raise ValueError(f"legal hold manifest missing field: {field}")
    if hold["policyId"] != args.policy_id:
        raise ValueError("legal hold policyId mismatch")
    keys = [str(value) for value in hold["holdKeys"]]
    if len(keys) != len(set(keys)):
        raise ValueError("legal hold keys must be unique")
    parsed = dt.datetime.fromisoformat(args.cutoff_utc.replace("Z", "+00:00"))
    if parsed.tzinfo is None:
        raise ValueError("cutoff must include timezone")
    cutoff = parsed.astimezone(dt.timezone.utc).isoformat(timespec="seconds")
    sql = generate(
        args.vendor,
        policy,
        keys,
        cutoff,
        source_columns,
        archive_columns,
        args.operator,
        args.reason,
    )
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(sql, encoding="utf-8", newline="\n")
    print(
        json.dumps(
            {
                "vendor": args.vendor,
                "policyId": args.policy_id,
                "holdKeyCount": len(keys),
                "sourceColumnCount": len(source_columns),
                "archiveColumnCount": len(archive_columns),
                "sqlSha256": hashlib.sha256(sql.encode()).hexdigest(),
                "output": str(output.resolve()),
                "sanitized": True,
            },
            sort_keys=True,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

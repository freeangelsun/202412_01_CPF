#!/usr/bin/env python3
"""Verify canonical CPF schema ownership, referential integrity and index metadata.

The verifier is dependency-free and intentionally separates deterministic static
failures from performance candidates that still require representative runtime
plans. It never promotes static analysis to database-runtime evidence.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any

ALLOWED_OWNER_DATABASES = {
    "bat": {"cpfDB"},
    "adm": {"cpfDB"},
    "admin": {"cpfDB"},
    "biz": {"mbwDB"},
    "backoffice": {"mbwDB"},
    "cpf": {"cpfDB"},
    "core": {"cpfDB"},
    "cmn": {"cpfDB"},
    "common": {"cpfDB"},
}
OFFICIAL_LOGICAL_DATABASES = {"cpfDB", "mbwDB"}
PREFIX_LENGTH_RE = re.compile(r"^\s*([^()]+?)\s*\(\s*\d+\s*\)\s*$")


@dataclass(frozen=True)
class Finding:
    path: str
    message: str


def load_schema(path: Path) -> dict[str, Any]:
    with path.open(encoding="utf-8-sig") as handle:
        value = json.load(handle)
    if not isinstance(value, dict):
        raise ValueError("canonical schema root must be a JSON object")
    return value


def normalized_identifier(value: str) -> str:
    candidate = str(value).strip()
    match = PREFIX_LENGTH_RE.match(candidate)
    if match:
        candidate = match.group(1)
    return candidate.casefold()


def index_covers(index_columns: list[str], required_columns: list[str]) -> bool:
    normalized_index = [normalized_identifier(value) for value in index_columns]
    normalized_required = [normalized_identifier(value) for value in required_columns]
    return normalized_index[: len(normalized_required)] == normalized_required


def verify(schema_path: Path) -> tuple[list[Finding], dict[str, Any]]:
    schema = load_schema(schema_path)
    tables = schema.get("tables")
    if not isinstance(tables, list):
        return [Finding(str(schema_path), "tables must be an array")], {"status": "FAIL"}

    failures: list[Finding] = []
    warnings: list[Finding] = []
    table_by_name: dict[str, dict[str, Any]] = {}

    if schema.get("tableCount") != len(tables):
        failures.append(Finding(str(schema_path), f"tableCount={schema.get('tableCount')} but actual={len(tables)}"))

    for table in tables:
        name = str(table.get("name", "")).strip()
        key = normalized_identifier(name)
        if not name:
            failures.append(Finding(str(schema_path), "table without name"))
            continue
        if key in table_by_name:
            failures.append(Finding(name, f"duplicate table identifier conflicts with {table_by_name[key].get('name')}"))
        else:
            table_by_name[key] = table

    logical_database_counts: dict[str, int] = {}
    module_counts: dict[str, int] = {}
    foreign_key_count = 0
    uncovered_fk_indexes: list[dict[str, Any]] = []
    no_primary_key_tables: list[str] = []

    for table in tables:
        name = str(table.get("name", "")).strip()
        module = str(table.get("module", "")).strip()
        logical_database = str(table.get("logicalDatabase", "")).strip()
        module_counts[module] = module_counts.get(module, 0) + 1
        logical_database_counts[logical_database] = logical_database_counts.get(logical_database, 0) + 1

        if module not in ALLOWED_OWNER_DATABASES:
            failures.append(Finding(name, f"unknown owner module: {module!r}"))
        elif logical_database not in ALLOWED_OWNER_DATABASES[module]:
            failures.append(Finding(name, f"owner module {module} cannot own logical database {logical_database}"))
        if logical_database not in OFFICIAL_LOGICAL_DATABASES:
            failures.append(Finding(name, f"unsupported logical database: {logical_database!r}"))
        if not str(table.get("comment") or "").strip():
            failures.append(Finding(name, "table comment is required for ownership/audit metadata"))

        columns = table.get("columns")
        if not isinstance(columns, list) or not columns:
            failures.append(Finding(name, "columns must be a non-empty array"))
            continue
        column_map: dict[str, dict[str, Any]] = {}
        for column in columns:
            required_fields = {"name", "type", "nullable", "default", "autoIncrement", "onUpdate", "comment"}
            missing_fields = sorted(required_fields - set(column))
            if missing_fields:
                failures.append(Finding(name, f"column is missing required fields {missing_fields}: {column.get('name', '<unknown>')}"))
            column_name = str(column.get("name", "")).strip()
            column_key = normalized_identifier(column_name)
            if not column_name:
                failures.append(Finding(name, "column without name"))
                continue
            if column_key in column_map:
                failures.append(Finding(name, f"duplicate column identifier: {column_name}"))
            else:
                column_map[column_key] = column
            if not str(column.get("type") or "").strip():
                failures.append(Finding(f"{name}.{column_name}", "column type is required"))
            if "nullable" in column and not isinstance(column["nullable"], bool):
                failures.append(Finding(f"{name}.{column_name}", "nullable must be boolean"))
            if "autoIncrement" in column and not isinstance(column["autoIncrement"], bool):
                failures.append(Finding(f"{name}.{column_name}", "autoIncrement must be boolean"))
            if not str(column.get("comment") or "").strip():
                failures.append(Finding(f"{name}.{column_name}", "column comment is required"))

        primary_key = table.get("primaryKey") or []
        if not primary_key:
            no_primary_key_tables.append(name)
        key_sets: list[list[str]] = [primary_key]
        for collection_name in ("uniqueKeys", "indexes"):
            collection = table.get(collection_name) or []
            local_names: set[str] = set()
            for item in collection:
                object_name = normalized_identifier(str(item.get("name", "")))
                if not object_name:
                    failures.append(Finding(name, f"{collection_name} entry without name"))
                elif object_name in local_names:
                    failures.append(Finding(name, f"duplicate {collection_name} name: {item.get('name')}"))
                local_names.add(object_name)
                item_columns = item.get("columns") or []
                if not item_columns:
                    failures.append(Finding(name, f"{collection_name} {item.get('name')} has no columns"))
                key_sets.append(item_columns)

        for key_columns in key_sets:
            for column_name in key_columns:
                if normalized_identifier(column_name) not in column_map:
                    failures.append(Finding(name, f"key/index references missing column: {column_name}"))

        fk_names: set[str] = set()
        for foreign_key in table.get("foreignKeys") or []:
            foreign_key_count += 1
            required_fields = {"name", "columns", "refTable", "refColumns", "onDelete", "onUpdate"}
            missing_fields = sorted(required_fields - set(foreign_key))
            if missing_fields:
                failures.append(Finding(name, f"foreign key is missing required fields {missing_fields}: {foreign_key.get('name', '<unknown>')}"))
            fk_name = str(foreign_key.get("name", "")).strip()
            fk_key = normalized_identifier(fk_name)
            if not fk_name:
                failures.append(Finding(name, "foreign key without name"))
            elif fk_key in fk_names:
                failures.append(Finding(name, f"duplicate foreign key name: {fk_name}"))
            fk_names.add(fk_key)

            local_columns = foreign_key.get("columns") or []
            reference_columns = foreign_key.get("refColumns") or []
            if not local_columns or len(local_columns) != len(reference_columns):
                failures.append(Finding(f"{name}.{fk_name}", "foreign key local/reference column cardinality mismatch"))
            for column_name in local_columns:
                if normalized_identifier(column_name) not in column_map:
                    failures.append(Finding(f"{name}.{fk_name}", f"missing local column: {column_name}"))

            reference_name = str(foreign_key.get("refTable", "")).strip()
            reference_table = table_by_name.get(normalized_identifier(reference_name))
            if reference_table is None:
                failures.append(Finding(f"{name}.{fk_name}", f"missing referenced table: {reference_name}"))
            else:
                reference_column_map = {
                    normalized_identifier(column.get("name", "")): column
                    for column in reference_table.get("columns") or []
                }
                for column_name in reference_columns:
                    if normalized_identifier(column_name) not in reference_column_map:
                        failures.append(Finding(f"{name}.{fk_name}", f"missing referenced column: {reference_name}.{column_name}"))
                if reference_table.get("logicalDatabase") != logical_database:
                    failures.append(Finding(f"{name}.{fk_name}", "cross-logical-database foreign keys are prohibited"))

            if local_columns and not any(index_covers(columns, local_columns) for columns in key_sets if columns):
                uncovered_fk_indexes.append({
                    "table": name,
                    "foreignKey": fk_name,
                    "columns": local_columns,
                    "logicalDatabase": logical_database,
                })

    if no_primary_key_tables != ["BAT_SB_JOB_EXECUTION_PARAMS"]:
        failures.append(Finding(str(schema_path), f"unexpected tables without primary key: {no_primary_key_tables}"))

    if uncovered_fk_indexes:
        warnings.append(Finding(str(schema_path), f"{len(uncovered_fk_indexes)} foreign keys need representative plan/index review"))

    summary = {
        "schemaVersion": 1,
        "canonicalSchemaVersion": schema.get("schemaVersion"),
        "tableCount": len(tables),
        "moduleCounts": dict(sorted(module_counts.items())),
        "logicalDatabaseCounts": dict(sorted(logical_database_counts.items())),
        "foreignKeyCount": foreign_key_count,
        "foreignKeyIndexReviewCandidateCount": len(uncovered_fk_indexes),
        "foreignKeyIndexReviewCandidates": uncovered_fk_indexes,
        "failureCount": len(failures),
        "warningCount": len(warnings),
        "status": "PASS" if not failures else "FAIL",
        "runtimeClaim": "STATIC_METADATA_ONLY",
        "warnings": [finding.__dict__ for finding in warnings],
    }
    return failures, summary


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--schema", default="cpf-tools/db/canonical/platform-schema.json")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    schema_path = Path(args.root).resolve() / args.schema
    failures, summary = verify(schema_path)
    if args.json_output:
        output = Path(args.json_output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(
            json.dumps({**summary, "failures": [finding.__dict__ for finding in failures]}, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
    print(json.dumps({key: value for key, value in summary.items() if key != "foreignKeyIndexReviewCandidates"}, ensure_ascii=False, sort_keys=True))
    for failure in failures:
        print(f"FAIL {failure.path}: {failure.message}", file=sys.stderr)
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3
"""Render/check the append-only V117 platform seed currentization.

The current EDU rows come only from canonical/seed-model.json.  This tool does
not rewrite an existing migration; it owns one newly allocated forward
migration and its preserve/no-op rollback for the official DB3 packs.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
from pathlib import Path
from typing import Any


class ContractError(RuntimeError):
    pass


def read_text(path: Path) -> str:
    if not path.is_file():
        raise ContractError(f"required file missing: {path.as_posix()}")
    return path.read_text(encoding="utf-8-sig")


def load_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(read_text(path))
    except json.JSONDecodeError as exc:
        raise ContractError(f"invalid JSON: {path.as_posix()}: {exc}") from exc
    if not isinstance(value, dict):
        raise ContractError(f"JSON root must be an object: {path.as_posix()}")
    return value


def split_sql_values(source: str) -> list[list[str]]:
    tuples: list[list[str]] = []
    current_tuple: list[str] = []
    token: list[str] = []
    depth = 0
    quoted = False
    index = 0
    while index < len(source):
        char = source[index]
        if quoted:
            token.append(char)
            if char == "'":
                if index + 1 < len(source) and source[index + 1] == "'":
                    token.append(source[index + 1])
                    index += 1
                else:
                    quoted = False
        elif char == "'":
            quoted = True
            token.append(char)
        elif char == "(":
            if depth == 0:
                current_tuple = []
                token = []
            else:
                token.append(char)
            depth += 1
        elif char == ")":
            depth -= 1
            if depth < 0:
                raise ContractError("canonical seed VALUES has unmatched closing parenthesis")
            if depth == 0:
                current_tuple.append("".join(token).strip())
                tuples.append(current_tuple)
                token = []
            else:
                token.append(char)
        elif char == "," and depth == 1:
            current_tuple.append("".join(token).strip())
            token = []
        elif depth > 0:
            token.append(char)
        elif not char.isspace() and char != ",":
            raise ContractError(f"unexpected token outside canonical seed tuple: {char!r}")
        index += 1
    if quoted or depth != 0:
        raise ContractError("canonical seed VALUES has an unterminated quote or tuple")
    return tuples


def decode_sql_literal(token: str) -> Any:
    upper = token.upper()
    if upper == "NULL":
        return None
    if re.fullmatch(r"-?\d+", token):
        return int(token)
    if len(token) >= 2 and token[0] == token[-1] == "'":
        return token[1:-1].replace("''", "'")
    raise ContractError(f"unsupported canonical seed literal: {token}")


def sql_literal(value: Any) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        raise ContractError("boolean SQL literal is not supported by the seed contract")
    if isinstance(value, int):
        return str(value)
    if isinstance(value, str):
        return "'" + value.replace("'", "''") + "'"
    raise ContractError(f"unsupported SQL value type: {type(value).__name__}")


def canonical_row(seed: dict[str, Any], spec: dict[str, Any]) -> tuple[list[str], dict[str, Any]]:
    canonical_table = spec["canonicalTable"]
    key = spec["key"]
    matches: list[tuple[list[str], dict[str, Any]]] = []
    for statement in seed.get("statements", []):
        if (
            statement.get("currentTable") != canonical_table
            or statement.get("targetDatabaseRole") != "CPF_PLATFORM_DB"
            or statement.get("sourceKind") != "values"
        ):
            continue
        columns = list(statement.get("columns") or [])
        if not columns or not set(key).issubset(columns):
            continue
        for raw_row in split_sql_values(str(statement.get("source") or "")):
            if len(raw_row) != len(columns):
                raise ContractError(f"canonical seed column/value count mismatch: {canonical_table}")
            row = {column: decode_sql_literal(value) for column, value in zip(columns, raw_row)}
            if all(row.get(column) == value for column, value in key.items()):
                matches.append((columns, row))
    if len(matches) != 1:
        raise ContractError(f"canonical seed row must resolve exactly once: table={canonical_table} key={key} count={len(matches)}")
    return matches[0]


def validate_contract(root: Path, contract: dict[str, Any]) -> list[tuple[dict[str, Any], list[str], dict[str, Any]]]:
    if contract.get("contract") != "CPF_PLATFORM_SEED_CURRENTIZATION":
        raise ContractError("platform seed currentization contract marker is invalid")
    if contract.get("officialVendors") != ["mariadb", "postgresql", "oracle"]:
        raise ContractError("platform seed currentization official DB3 order drift")
    migration = contract.get("migration") or {}
    version = int(migration.get("allocatedVersion") or 0)
    observed = int(migration.get("observedRepositoryMaxVersionAtAllocation") or 0)
    if version != observed + 1:
        raise ContractError(f"migration version allocation drift: observed={observed} allocated={version}")
    if migration.get("logicalDatabase") != "cpfDB" or migration.get("name") != "platform_seed_currentization":
        raise ContractError("migration routing/name contract drift")
    if (contract.get("rollbackPolicy") or {}).get("mode") != "PRESERVE_CURRENT_NOOP":
        raise ContractError("rollback must preserve current EDU state and never recreate retired REF ownership")
    if (contract.get("safetyPolicy") or {}).get("retiredRowDeletion") != "EXACT_FULL_OWNERSHIP_FINGERPRINT_ONLY":
        raise ContractError("retired seed deletion policy is not fail-closed")

    seed_path = root / str(contract.get("sourceSeedModel") or "")
    seed = load_json(seed_path)
    resolved: list[tuple[dict[str, Any], list[str], dict[str, Any]]] = []
    for spec in contract.get("currentRows") or []:
        columns, row = canonical_row(seed, spec)
        resolved.append((spec, columns, row))
    if [item[0].get("canonicalTable") for item in resolved] != ["CMN_MESSAGE", "CMN_RESPONSE_CODE"]:
        raise ContractError("current EDU message/response row contract drift")
    packed_seed = json.dumps(seed, ensure_ascii=False)
    for stale in ("MREF090001", "EREF090001", "REF_EDU_SAMPLE"):
        if stale in packed_seed:
            raise ContractError(f"retired REF ownership remains in canonical current seed: {stale}")

    retired = contract.get("retiredOwnedRows") or []
    if [item.get("historicalTable") for item in retired] != ["cpf_response_code", "cpf_message"]:
        raise ContractError("retired row cleanup order must be response before message")
    for item in retired:
        fingerprint = item.get("ownershipFingerprint") or {}
        if len(fingerprint) < 10:
            raise ContractError(f"retired row ownership fingerprint is too weak: {item.get('historicalTable')}")
    protection = retired[1].get("protectWhileReferencedBy") or {}
    if protection != {"table": "cpf_response_code", "column": "message_code", "value": "MREF090001"}:
        raise ContractError("retired message reference-preservation contract drift")

    intent_catalog = load_json(root / "cpf-tools/db/canonical/migration-intent-catalog.json")
    intents = [item for item in intent_catalog.get("currentIntents", []) if item.get("id") == contract.get("intentId")]
    if len(intents) != 1 or intents[0].get("source") != "cpf-tools/db/canonical/platform-seed-currentization.json":
        raise ContractError("D-010 migration intent/catalog parity drift")
    scenarios = load_json(root / "cpf-tools/db/canonical/db3-lifecycle-scenarios.json")
    scenario_id = intents[0].get("lifecycleScenarioId")
    selected = [item for item in scenarios.get("scenarios", []) if item.get("id") == scenario_id]
    if len(selected) != 1 or not selected[0].get("sameScenarioForAllVendors"):
        raise ContractError("D-010 DB3 lifecycle scenario parity drift")

    historical = read_text(root / "cpf-tools/db/vendor/mariadb/migration/flyway/V1__cpf_baseline_install.sql")
    for marker in ("MREF090001", "REF_EDU_SAMPLE", "REF 동적 중복 교육 메시지"):
        if marker not in historical:
            raise ContractError(f"immutable MariaDB baseline evidence missing: {marker}")
    return resolved


def current_upsert(vendor: str, table: str, columns: list[str], row: dict[str, Any], key_columns: list[str]) -> str:
    values = ", ".join(sql_literal(row[column]) for column in columns)
    insert = f"INSERT INTO {table} ({', '.join(columns)}) VALUES ({values})"
    mutable = [column for column in columns if column not in set(key_columns) | {"created_by"}]
    if vendor == "mariadb":
        updates = [f"{column} = VALUES({column})" for column in mutable]
        updates.extend(["use_yn = 'Y'", "updated_at = CURRENT_TIMESTAMP"])
        return insert + " ON DUPLICATE KEY UPDATE " + ", ".join(updates) + ";"
    if vendor == "postgresql":
        updates = [f"{column} = EXCLUDED.{column}" for column in mutable]
        updates.extend(["use_yn = 'Y'", "updated_at = CURRENT_TIMESTAMP"])
        return insert + f" ON CONFLICT ({', '.join(key_columns)}) DO UPDATE SET " + ", ".join(updates) + ";"
    select_columns = ", ".join(f"{sql_literal(row[column])} {column}" for column in columns)
    updates = [f"tgt.{column} = src.{column}" for column in mutable]
    updates.extend(["tgt.use_yn = 'Y'", "tgt.updated_at = CURRENT_TIMESTAMP"])
    return "\n".join(
        [
            f"MERGE INTO {table} tgt USING (SELECT {select_columns} FROM dual) src",
            "ON (" + " AND ".join(f"tgt.{column} = src.{column}" for column in key_columns) + ")",
            "WHEN MATCHED THEN UPDATE SET " + ", ".join(updates),
            "WHEN NOT MATCHED THEN INSERT (" + ", ".join(columns) + ")",
            "VALUES (" + ", ".join(f"src.{column}" for column in columns) + ");",
        ]
    )


def exact_condition(vendor: str, fingerprint: dict[str, Any]) -> str:
    clauses: list[str] = []
    for column, value in fingerprint.items():
        if value is None:
            clauses.append(f"{column} IS NULL")
        elif vendor == "mariadb" and isinstance(value, str):
            clauses.append(f"BINARY {column} = BINARY {sql_literal(value)}")
        else:
            clauses.append(f"{column} = {sql_literal(value)}")
    return " AND ".join(clauses)


def render_forward(vendor: str, contract: dict[str, Any], rows: list[tuple[dict[str, Any], list[str], dict[str, Any]]]) -> str:
    version = contract["migration"]["allocatedVersion"]
    lines = [
        "-- GENERATED from cpf-tools/db/canonical/platform-seed-currentization.json and seed-model.json; DO NOT EDIT.",
        f"-- D-010 V{version}: canonical EDU seed currentization with exact-owned retired REF cleanup.",
    ]
    if vendor == "mariadb":
        lines.extend(["USE cpfDB;", "START TRANSACTION;"])
    elif vendor == "postgresql":
        lines.append("BEGIN;")
    else:
        lines.append("WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK")

    for spec, columns, row in rows:
        key_columns = list(spec["key"])
        lines.append(current_upsert(vendor, spec["historicalTable"], columns, row, key_columns))
    for retired in contract["retiredOwnedRows"]:
        condition = exact_condition(vendor, retired["ownershipFingerprint"])
        protection = retired.get("protectWhileReferencedBy")
        if protection:
            value_condition = (
                f"BINARY {protection['column']} = BINARY {sql_literal(protection['value'])}"
                if vendor == "mariadb"
                else f"{protection['column']} = {sql_literal(protection['value'])}"
            )
            condition += f" AND NOT EXISTS (SELECT 1 FROM {protection['table']} WHERE {value_condition})"
        lines.append(f"DELETE FROM {retired['historicalTable']} WHERE {condition};")
    lines.append("COMMIT;")
    return "\n".join(lines) + "\n"


def render_rollback(vendor: str, contract: dict[str, Any]) -> str:
    version = contract["migration"]["allocatedVersion"]
    reason = contract["rollbackPolicy"]["reason"]
    lines = [
        "-- GENERATED from cpf-tools/db/canonical/platform-seed-currentization.json; DO NOT EDIT.",
        f"-- D-010 R{version} policy=PRESERVE_CURRENT_NOOP.",
        f"-- {reason}",
    ]
    if vendor == "mariadb":
        lines.extend(["USE cpfDB;", "START TRANSACTION;", "-- Intentionally no data mutation.", "COMMIT;"])
    elif vendor == "postgresql":
        lines.extend(["BEGIN;", "-- Intentionally no data mutation.", "COMMIT;"])
    else:
        lines.extend(["WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK", "BEGIN", "    NULL;", "END;", "/", "COMMIT;"])
    return "\n".join(lines) + "\n"


def output_map(root: Path, contract: dict[str, Any], rows: list[tuple[dict[str, Any], list[str], dict[str, Any]]]) -> dict[Path, str]:
    version = contract["migration"]["allocatedVersion"]
    name = contract["migration"]["name"]
    maria_forward = render_forward("mariadb", contract, rows)
    maria_rollback = render_rollback("mariadb", contract)
    outputs = {
        root / f"cpf-tools/db/vendor/mariadb/migration/flyway/V{version}__{name}.sql": maria_forward,
        root / f"cpf-tools/db/vendor/mariadb/rollback/R{version}__{name}.sql": maria_rollback,
        root / f"cpf-tools/db/vendor/mariadb/source/migration/flyway/V{version}__{name}.sql": maria_forward,
        root / f"cpf-tools/db/vendor/mariadb/source/migration/rollback/R{version}__{name}.sql": maria_rollback,
    }
    for vendor in ("postgresql", "oracle"):
        outputs[root / f"cpf-tools/db/vendor/{vendor}/migration/flyway/cpfDB/V{version}__{name}.sql"] = render_forward(vendor, contract, rows)
        outputs[root / f"cpf-tools/db/vendor/{vendor}/rollback/cpfDB/R{version}__{name}.sql"] = render_rollback(vendor, contract)
    return outputs


def verify_version_allocation(root: Path, contract: dict[str, Any], outputs: dict[Path, str]) -> None:
    version = int(contract["migration"]["allocatedVersion"])
    observed = int(contract["migration"]["observedRepositoryMaxVersionAtAllocation"])
    expected_forward = {path.resolve() for path in outputs if path.name.startswith("V")}
    seen_prior: list[int] = []
    collisions: list[str] = []
    for path in (root / "cpf-tools/db/vendor").rglob("V*.sql"):
        if "migration" not in path.parts:
            continue
        match = re.match(r"^V(\d+)__.+\.sql$", path.name)
        if not match:
            continue
        value = int(match.group(1))
        resolved = path.resolve()
        if resolved in expected_forward:
            continue
        # This contract owns exactly the allocated version. Later append-only migrations
        # are valid repository history and must not make the historical V117 contract stale.
        if value == version:
            collisions.append(path.relative_to(root).as_posix())
        elif value < version:
            seen_prior.append(value)
    actual_max = max(seen_prior, default=0)
    if actual_max != observed:
        raise ContractError(f"migration allocation stale: contract observed V{observed}, repository max V{actual_max}")
    if collisions:
        raise ContractError(f"migration V{version} collision detected: {collisions}")


def verify_checksums(root: Path, outputs: dict[Path, str]) -> None:
    for path, expected in outputs.items():
        if not path.name.startswith("V"):
            continue
        manifest = path.parent / "checksums.sha256"
        entries: dict[str, str] = {}
        for line in read_text(manifest).splitlines():
            if not line.strip():
                continue
            match = re.fullmatch(r"([0-9a-fA-F]{64})\s+\*?(V\d+__.+\.sql)", line.strip())
            if not match:
                raise ContractError(f"invalid checksum manifest line: {manifest.as_posix()}: {line}")
            if match.group(2) in entries:
                raise ContractError(f"duplicate checksum entry: {manifest.as_posix()}: {match.group(2)}")
            entries[match.group(2)] = match.group(1).lower()
        digest = hashlib.sha256(expected.encode("utf-8")).hexdigest()
        if entries.get(path.name) != digest:
            raise ContractError(f"migration checksum registration mismatch: {path.as_posix()}")


def sync(root: Path, write: bool) -> list[str]:
    contract = load_json(root / "cpf-tools/db/canonical/platform-seed-currentization.json")
    rows = validate_contract(root, contract)
    outputs = output_map(root, contract, rows)
    verify_version_allocation(root, contract, outputs)
    changed: list[str] = []
    for path, expected in outputs.items():
        actual = path.read_text(encoding="utf-8-sig") if path.is_file() else None
        if actual == expected:
            continue
        if not write:
            raise ContractError(f"generated migration drift: {path.relative_to(root).as_posix()}")
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(expected, encoding="utf-8", newline="\n")
        changed.append(path.relative_to(root).as_posix())
    if not write:
        verify_checksums(root, outputs)
    return changed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--write", action="store_true")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        changed = sync(root, args.write)
    except ContractError as exc:
        print(f"[FAIL] CPF platform seed currentization: {exc}")
        return 1
    mode = "WRITE" if args.write else "CHECK"
    print(f"[PASS] CPF platform seed currentization {mode}: changed={len(changed)} version=V{load_json(root / 'cpf-tools/db/canonical/platform-seed-currentization.json')['migration']['allocatedVersion']} vendors=3 rollback=PRESERVE_CURRENT_NOOP")
    for path in changed:
        print(f"  [UPDATED] {path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

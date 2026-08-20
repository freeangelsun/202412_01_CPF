#!/usr/bin/env python3
"""Generate the current CPF physical database schema manifest.

The manifest is derived from the rendered MariaDB current schemas that are actually
installed (cpfDB, mbwDB, referenceFixture) and is cross-checked against the canonical
platform schema before it is written. Historical split DB source files are not a
manifest authority.
"""
from __future__ import annotations

import argparse
import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

PHYSICAL_SOURCES = {
    "cpfDB": "cpf-platform-schema.sql",
    "mbwDB": "backoffice-schema.sql",
    "referenceFixture": "reference-fixture-schema.sql",
}

IDENT = r"[A-Za-z][A-Za-z0-9_$#]*"


class ManifestError(RuntimeError):
    pass


@dataclass(frozen=True)
class ParsedTable:
    name: str
    columns: tuple[str, ...]
    indexes: tuple[tuple[str, bool, tuple[str, ...]], ...]
    foreign_keys: tuple[tuple[str, tuple[str, ...], str, tuple[str, ...]], ...]


def norm(name: str) -> str:
    return name.strip().strip("`").upper()


def split_top_level(body: str) -> list[str]:
    out: list[str] = []
    buf: list[str] = []
    depth = 0
    quote: str | None = None
    i = 0
    while i < len(body):
        ch = body[i]
        if quote:
            buf.append(ch)
            if ch == quote:
                if i + 1 < len(body) and body[i + 1] == quote:
                    buf.append(body[i + 1])
                    i += 1
                elif i == 0 or body[i - 1] != "\\":
                    quote = None
            i += 1
            continue
        if ch in ("'", '"', '`'):
            quote = ch
            buf.append(ch)
        elif ch == "(":
            depth += 1
            buf.append(ch)
        elif ch == ")":
            depth -= 1
            if depth < 0:
                raise ManifestError("unbalanced table definition")
            buf.append(ch)
        elif ch == "," and depth == 0:
            value = "".join(buf).strip()
            if value:
                out.append(value)
            buf.clear()
        else:
            buf.append(ch)
        i += 1
    if quote or depth != 0:
        raise ManifestError("unterminated quoted text or parenthesis in table definition")
    value = "".join(buf).strip()
    if value:
        out.append(value)
    return out


def parse_column_list(text: str) -> tuple[str, ...]:
    values: list[str] = []
    for token in split_top_level(text):
        clean = re.sub(r"\s+(ASC|DESC)\b", "", token.strip(), flags=re.I)
        clean = re.sub(r"\(\s*\d+\s*\)$", "", clean)
        m = re.match(rf"^`?({IDENT})`?$", clean.strip())
        if not m:
            raise ManifestError(f"unsupported index/FK column expression: {token!r}")
        values.append(m.group(1))
    return tuple(values)


def find_create_tables(sql: str) -> list[tuple[str, str]]:
    matches: list[tuple[str, str]] = []
    pattern = re.compile(rf"CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`?({IDENT})`?\s*\(", re.I)
    for match in pattern.finditer(sql):
        start = match.end() - 1
        depth = 0
        quote: str | None = None
        i = start
        while i < len(sql):
            ch = sql[i]
            if quote:
                if ch == quote:
                    if i + 1 < len(sql) and sql[i + 1] == quote:
                        i += 2
                        continue
                    if i == 0 or sql[i - 1] != "\\":
                        quote = None
                i += 1
                continue
            if ch in ("'", '"', '`'):
                quote = ch
            elif ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
                if depth == 0:
                    matches.append((match.group(1), sql[start + 1 : i]))
                    break
                if depth < 0:
                    raise ManifestError(f"unbalanced CREATE TABLE {match.group(1)}")
            i += 1
        else:
            raise ManifestError(f"CREATE TABLE not closed: {match.group(1)}")
    return matches


def parse_schema(path: Path) -> dict[str, ParsedTable]:
    sql = path.read_text(encoding="utf-8")
    tables: dict[str, dict[str, object]] = {}
    for table_name, body in find_create_tables(sql):
        key = norm(table_name)
        if key in tables:
            raise ManifestError(f"duplicate CREATE TABLE: {path} {table_name}")
        columns: list[str] = []
        indexes: list[tuple[str, bool, tuple[str, ...]]] = []
        foreign_keys: list[tuple[str, tuple[str, ...], str, tuple[str, ...]]] = []
        for raw in split_top_level(body):
            line = re.sub(r"\s+", " ", raw.strip())
            upper = line.upper()
            fk = re.match(
                rf"^CONSTRAINT\s+`?({IDENT})`?\s+FOREIGN\s+KEY\s*\((.*?)\)\s+REFERENCES\s+`?({IDENT})`?\s*\((.*?)\)",
                line,
                re.I | re.S,
            )
            if fk:
                foreign_keys.append((fk.group(1), parse_column_list(fk.group(2)), fk.group(3), parse_column_list(fk.group(4))))
                continue
            unique_constraint = re.match(
                rf"^CONSTRAINT\s+`?({IDENT})`?\s+UNIQUE(?:\s+(?:KEY|INDEX))?\s*\((.*?)\)", line, re.I | re.S
            )
            if unique_constraint:
                indexes.append((unique_constraint.group(1), True, parse_column_list(unique_constraint.group(2))))
                continue
            inline_index = re.match(
                rf"^(UNIQUE\s+)?(?:KEY|INDEX)\s+`?({IDENT})`?\s*\((.*?)\)", line, re.I | re.S
            )
            if inline_index:
                indexes.append((inline_index.group(2), bool(inline_index.group(1)), parse_column_list(inline_index.group(3))))
                continue
            if upper.startswith(("PRIMARY KEY", "CONSTRAINT ", "CHECK ", "FOREIGN KEY")):
                continue
            col = re.match(rf"^`?({IDENT})`?\s+", line)
            if not col:
                raise ManifestError(f"unsupported table definition: {path}:{table_name}: {line[:160]}")
            columns.append(col.group(1))
        tables[key] = {
            "name": table_name,
            "columns": columns,
            "indexes": indexes,
            "foreign_keys": foreign_keys,
        }

    standalone = re.compile(
        rf"CREATE\s+(UNIQUE\s+)?INDEX\s+`?({IDENT})`?\s+ON\s+`?({IDENT})`?\s*\((.*?)\)\s*;",
        re.I | re.S,
    )
    for match in standalone.finditer(sql):
        table_key = norm(match.group(3))
        if table_key not in tables:
            raise ManifestError(f"CREATE INDEX references unknown table: {path} {match.group(2)} -> {match.group(3)}")
        tables[table_key]["indexes"].append((match.group(2), bool(match.group(1)), parse_column_list(match.group(4))))

    result: dict[str, ParsedTable] = {}
    for key, item in tables.items():
        result[key] = ParsedTable(
            name=str(item["name"]),
            columns=tuple(item["columns"]),
            indexes=tuple(item["indexes"]),
            foreign_keys=tuple(item["foreign_keys"]),
        )
    return result


def normalize_index_column(value: str) -> str:
    clean = re.sub(r"\s+(ASC|DESC)\b", "", str(value).strip(), flags=re.I)
    clean = re.sub(r"\(\s*\d+\s*\)$", "", clean)
    return clean.strip().strip("`")


def canonical_indexes(table: dict) -> tuple[tuple[str, bool, tuple[str, ...]], ...]:
    values: list[tuple[str, bool, tuple[str, ...]]] = []
    for item in table.get("uniqueKeys", []):
        values.append((item["name"], True, tuple(normalize_index_column(x) for x in item["columns"])))
    for item in table.get("indexes", []):
        values.append((item["name"], bool(item.get("unique", False)), tuple(normalize_index_column(x) for x in item["columns"])))
    return tuple(values)


def canonical_fks(table: dict) -> tuple[tuple[str, tuple[str, ...], str, tuple[str, ...]], ...]:
    return tuple(
        (item["name"], tuple(item["columns"]), item["refTable"], tuple(item["refColumns"]))
        for item in table.get("foreignKeys", [])
    )


def index_key(value: tuple[str, bool, tuple[str, ...]]) -> tuple[str, bool, tuple[str, ...]]:
    return (norm(value[0]), value[1], tuple(norm(x) for x in value[2]))


def fk_key(value: tuple[str, tuple[str, ...], str, tuple[str, ...]]) -> tuple[str, tuple[str, ...], str, tuple[str, ...]]:
    return (norm(value[0]), tuple(norm(x) for x in value[1]), norm(value[2]), tuple(norm(x) for x in value[3]))


def assert_same(label: str, expected: Iterable, actual: Iterable, context: str) -> None:
    e = list(expected)
    a = list(actual)
    if e != a:
        raise ManifestError(f"{label} drift: {context} expected={e} actual={a}")


def build_manifest(root: Path) -> dict:
    canonical_path = root / "cpf-tools/db/canonical/platform-schema.json"
    profile_path = root / "cpf-tools/db/config/database-install.default.json"
    physical_root = root / "cpf-tools/db/generated/current/mariadb"
    canonical = json.loads(canonical_path.read_text(encoding="utf-8"))
    profile = json.loads(profile_path.read_text(encoding="utf-8"))

    if canonical.get("tableCount") != len(canonical.get("tables", [])):
        raise ManifestError("canonical platform-schema tableCount does not match tables length")

    expected_dbs = sorted({m["logicalDatabase"] for m in profile["modules"].values() if m.get("enabled")})
    if expected_dbs != sorted(PHYSICAL_SOURCES):
        raise ManifestError(f"enabled profile DB set drift: expected={sorted(PHYSICAL_SOURCES)} actual={expected_dbs}")

    parsed_by_db: dict[str, dict[str, ParsedTable]] = {}
    for database, filename in PHYSICAL_SOURCES.items():
        path = physical_root / filename
        if not path.is_file():
            raise ManifestError(f"rendered current schema missing: {path}")
        parsed_by_db[database] = parse_schema(path)

    canonical_by_db: dict[str, list[dict]] = {db: [] for db in PHYSICAL_SOURCES}
    for table in canonical["tables"]:
        db = table.get("logicalDatabase")
        if db not in canonical_by_db:
            raise ManifestError(f"canonical table has unsupported physical DB: {table.get('name')} db={db}")
        canonical_by_db[db].append(table)

    manifest_tables: list[dict] = []
    for database, filename in PHYSICAL_SOURCES.items():
        expected_tables = canonical_by_db[database]
        parsed = parsed_by_db[database]
        expected_names = {norm(t["name"]) for t in expected_tables}
        actual_names = set(parsed)
        if expected_names != actual_names:
            raise ManifestError(
                f"physical table set drift: db={database} missing={sorted(expected_names-actual_names)} unexpected={sorted(actual_names-expected_names)}"
            )
        for table in expected_tables:
            actual = parsed[norm(table["name"])]
            expected_columns = tuple(c["name"] for c in table["columns"])
            assert_same(
                "column order",
                [norm(x) for x in expected_columns],
                [norm(x) for x in actual.columns],
                f"db={database} table={table['name']}",
            )
            exp_indexes = sorted((index_key(x) for x in canonical_indexes(table)), key=lambda x: x[0])
            act_indexes = sorted((index_key(x) for x in actual.indexes), key=lambda x: x[0])
            assert_same("index", exp_indexes, act_indexes, f"db={database} table={table['name']}")
            exp_fks = sorted((fk_key(x) for x in canonical_fks(table)), key=lambda x: x[0])
            act_fks = sorted((fk_key(x) for x in actual.foreign_keys), key=lambda x: x[0])
            assert_same("foreign key", exp_fks, act_fks, f"db={database} table={table['name']}")

            local_columns = {norm(x) for x in actual.columns}
            for name, _, cols in actual.indexes:
                missing = [x for x in cols if norm(x) not in local_columns]
                if missing:
                    raise ManifestError(f"index references missing local column: db={database} table={table['name']} index={name} missing={missing}")
            for name, cols, ref_table, ref_cols in actual.foreign_keys:
                missing = [x for x in cols if norm(x) not in local_columns]
                if missing:
                    raise ManifestError(f"FK references missing local column: db={database} table={table['name']} fk={name} missing={missing}")
                target = parsed.get(norm(ref_table))
                if target is None:
                    raise ManifestError(f"FK references table outside physical DB: db={database} table={table['name']} fk={name} ref={ref_table}")
                target_columns = {norm(x) for x in target.columns}
                missing_ref = [x for x in ref_cols if norm(x) not in target_columns]
                if missing_ref:
                    raise ManifestError(f"FK references missing target column: db={database} table={table['name']} fk={name} missing={missing_ref}")
                if len(cols) != len(ref_cols):
                    raise ManifestError(f"FK column arity mismatch: db={database} table={table['name']} fk={name}")

            manifest_tables.append(
                {
                    "vendor": "mariadb",
                    "logicalDatabase": database,
                    "sourceFile": filename,
                    "tableName": table["name"],
                    "columns": list(expected_columns),
                    "indexes": [
                        {"name": name, "unique": unique, "columns": list(cols)}
                        for name, unique, cols in canonical_indexes(table)
                    ],
                    "foreignKeys": [
                        {
                            "name": name,
                            "columns": list(cols),
                            "referencedTable": ref_table,
                            "referencedColumns": list(ref_cols),
                        }
                        for name, cols, ref_table, ref_cols in canonical_fks(table)
                    ],
                }
            )

    if len(manifest_tables) != canonical["tableCount"]:
        raise ManifestError(f"manifest table count drift: expected={canonical['tableCount']} actual={len(manifest_tables)}")

    return {
        "schemaVersion": 2,
        "generatedBy": "cpf-tools/db/tools/generate-database-schema-manifest.py",
        "canonicalSchema": "cpf-tools/db/canonical/platform-schema.json",
        "physicalSourceRoot": "cpf-tools/db/generated/current/mariadb",
        "installProfile": "cpf-tools/db/config/database-install.default.json",
        "generatedBusinessDomainPolicy": "NO_FIXED_BUSINESS_DOMAIN_SCHEMA",
        "tableCount": len(manifest_tables),
        "tables": sorted(manifest_tables, key=lambda x: (x["logicalDatabase"].lower(), x["tableName"].lower())),
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--output", default="cpf-tools/db/generated/database-schema-manifest.json")
    parser.add_argument("--check", action="store_true", help="fail if the tracked manifest differs; do not write")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    output = Path(args.output)
    if not output.is_absolute():
        output = root / output
    manifest = build_manifest(root)
    rendered = json.dumps(manifest, ensure_ascii=False, indent=2) + "\n"
    if args.check:
        if not output.is_file():
            raise ManifestError(f"manifest missing: {output}")
        current = output.read_text(encoding="utf-8")
        if current != rendered:
            raise ManifestError(f"database schema manifest drift: {output}")
        print(f"CPF_DB_SCHEMA_MANIFEST=PASS tables={manifest['tableCount']} mode=check")
        return 0
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(rendered, encoding="utf-8", newline="\n")
    print(f"CPF_DB_SCHEMA_MANIFEST=PASS tables={manifest['tableCount']} output={output}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except ManifestError as exc:
        print(f"CPF_DB_SCHEMA_MANIFEST=FAIL reason={exc}")
        raise SystemExit(1)

#!/usr/bin/env python3
"""Validate CPF transaction identity and standard execution ID contracts."""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import defaultdict
from pathlib import Path


class GateError(RuntimeError):
    pass


OFFICIAL_VENDORS = ("mariadb", "postgresql", "oracle")
OPENAPI_CONFIG = "cpf-starters/profiles/web-api/src/main/java/com/cpf/starter/profile/webapi/internal/openapi/CpfOpenApiAutoConfiguration.java"
STALE_OPENAPI_CONFIG = "cpf-core/src/main/java/com/cpf/core/config/CpfOpenApiAutoConfiguration.java"
EXECUTION_ID = re.compile(r"^[OSB][A-Z]{3}[A-Z0-9]{2}(?!0000)[0-9]{4}$")
LEGACY_EXECUTION_ID = re.compile(r"[OB][A-Z]{3}-[A-Z0-9]{3}-[A-Z0-9]{2}-[0-9]{4}")
ANNOTATION = re.compile(r"@Cpf(?P<type>OnlineTransaction|SharedApi|BatchJob)\s*\((?P<body>.*?)\)", re.S)
LITERAL_ID = re.compile(r'\bid\s*=\s*"(?P<id>[^"]+)"')
PREFIX = {"OnlineTransaction": "O", "SharedApi": "S", "BatchJob": "B"}
TEXT_EXTENSIONS = {".java", ".xml", ".yml", ".yaml", ".sql", ".json", ".gradle"}
EXCLUDED_NAMES = {"V32__standard_execution_id_v2.sql", "52_standard_execution_alias_seed.sql"}
ALIAS_INSERT = re.compile(
    r"INSERT\s+INTO\s+cpf_standard_execution_alias\s*\(.*?updated_at\s*=\s*CURRENT_TIMESTAMP\s*;",
    re.I | re.S,
)


def read(root: Path, relative: str, errors: list[str]) -> str:
    path = root / relative
    if not path.is_file():
        errors.append(f"missing file: {relative}")
        return ""
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        errors.append(f"invalid UTF-8: {relative}")
        return ""


def require_pattern(
    root: Path, relative: str, pattern: str, message: str, errors: list[str], flags: int = 0
) -> None:
    text = read(root, relative, errors)
    if text and not re.search(pattern, text, flags):
        errors.append(f"{relative}: {message}")


def production_java_files(root: Path) -> list[Path]:
    files: list[Path] = []
    for path in root.rglob("*.java"):
        rel = path.relative_to(root).as_posix()
        if "/src/main/java/" not in f"/{rel}":
            continue
        if any(part in {"build", ".gradle", ".git"} for part in path.parts):
            continue
        files.append(path)
    return sorted(files)


def scan_execution_annotations(root: Path, errors: list[str]) -> tuple[int, int]:
    locations: dict[str, list[str]] = defaultdict(list)
    annotation_count = 0
    for path in production_java_files(root):
        try:
            source = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            errors.append(f"invalid UTF-8: {path.relative_to(root).as_posix()}")
            continue
        rel = path.relative_to(root).as_posix()
        for match in ANNOTATION.finditer(source):
            annotation_count += 1
            literal = LITERAL_ID.search(match.group("body"))
            if not literal:
                errors.append(f"{rel}: standard execution annotation id must be a string literal")
                continue
            execution_id = literal.group("id")
            if not EXECUTION_ID.fullmatch(execution_id):
                errors.append(f"{rel}: invalid standard execution ID: {execution_id}")
            expected = PREFIX[match.group("type")]
            if not execution_id.startswith(expected):
                errors.append(
                    f"{rel}: annotation type/prefix mismatch: type={match.group('type')} id={execution_id}"
                )
            locations[execution_id].append(rel)
    duplicates = 0
    for execution_id, paths in sorted(locations.items()):
        if len(paths) > 1:
            duplicates += 1
            errors.append(f"duplicate standard execution ID: {execution_id} ({', '.join(sorted(set(paths)))})")
    return annotation_count, duplicates


def legacy_scan_roots(root: Path) -> list[Path]:
    roots: list[Path] = []
    for child in root.iterdir() if root.is_dir() else []:
        if not child.is_dir():
            continue
        if child.name.startswith("cpf-") and child.name != "cpf-docs":
            roots.append(child)
    vendor_root = root / "cpf-tools/db/vendor"
    if vendor_root.is_dir() and vendor_root not in roots:
        roots.append(vendor_root)
    return sorted(set(roots))


def scan_legacy_ids(root: Path, errors: list[str]) -> int:
    found = 0
    visited: set[Path] = set()
    for scan_root in legacy_scan_roots(root):
        for path in scan_root.rglob("*"):
            if not path.is_file() or path in visited:
                continue
            visited.add(path)
            rel = path.relative_to(root).as_posix()
            if path.suffix.lower() not in TEXT_EXTENSIONS or path.name in EXCLUDED_NAMES:
                continue
            if any(part in {"build", ".gradle", ".git", "node_modules", "dist", "coverage", "test-results"} for part in path.parts):
                continue
            if "/src/test/" in f"/{rel}" or "/evidence/" in f"/{rel}":
                continue
            try:
                text = path.read_text(encoding="utf-8")
            except UnicodeDecodeError:
                errors.append(f"invalid UTF-8: {rel}")
                continue
            if path.suffix.lower() == ".sql":
                text = ALIAS_INSERT.sub("", text)
            ids = sorted(set(LEGACY_EXECUTION_ID.findall(text)))
            if ids:
                found += len(ids)
                errors.append(f"{rel}: legacy standard execution ID use: {', '.join(ids)}")
    return found


def validate(root: Path) -> dict[str, object]:
    root = root.resolve()
    errors: list[str] = []
    required = [
        (
            "cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java",
            r"MODULE_ID_LENGTH\s*=\s*3",
            "MODULE_ID_LENGTH must be 3",
        ),
        (
            "cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java",
            r"WAS_ID_LENGTH\s*=\s*7",
            "WAS_ID_LENGTH must be 7",
        ),
        (
            "cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java",
            r"DEFAULT_SEQUENCE_DIGITS\s*=\s*7",
            "DEFAULT_SEQUENCE_DIGITS must be 7",
        ),
        (
            "cpf-core/src/main/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptor.java",
            r"inboundHeaderValidator\.missingRequiredHeaders",
            "online API required-header validation is missing",
        ),
        (
            "cpf-core/src/main/java/com/cpf/core/common/header/CpfInboundHeaderValidator.java",
            r"TransactionIdGenerator\.isValid",
            "X-Transaction-Id format validation is missing",
        ),
        (
            OPENAPI_CONFIG,
            r'"X-Transaction-Id".*?true.*?yyyyMMddHHmmssSSS',
            "OpenAPI must expose required 34-character transaction ID",
        ),
        (
            "cpf-core/src/main/resources/application-cpf.yml",
            r"module-id:\s*\$\{[^\r\n]*:CPF\}\}\}",
            "cpf-core module-id default must be CPF",
        ),
        (
            "cpf-core/src/main/java/com/cpf/core/common/system/CpfSystemCodes.java",
            r'public\s+static\s+final\s+String\s+CORE\s*=\s*"CPF"',
            "cpf-core system code must be CPF",
        ),
        (
            "cpf-core/src/main/resources/application-cpf.yml",
            r"was-id:\s*\$\{[^\r\n]*:[A-Za-z0-9]{7}\}\}\}",
            "CPF was-id default must be 7 characters",
        ),
        (
            "cpf-admin/frontend/src/shared/transaction.ts",
            r"createTransactionId",
            "ADM transaction ID generator is missing",
        ),
    ]
    for relative, pattern, message in required:
        require_pattern(root, relative, pattern, message, errors, re.S)
    if (root / STALE_OPENAPI_CONFIG).is_file():
        errors.append(
            "OpenAPI runtime configuration must be starter-owned; "
            f"stale core owner file exists: {STALE_OPENAPI_CONFIG}"
        )

    vendor_expectations = {
        "mariadb": (r"SERVER_INSTANCE_ID\s+VARCHAR\(160\)", r"DATE\(@sample_start_time\)"),
        "postgresql": (r"SERVER_INSTANCE_ID\s+VARCHAR\(160\)", r"DATE\(:sample_start_time\)"),
        "oracle": (r"SERVER_INSTANCE_ID\s+VARCHAR2\(160\s+CHAR\)", r"TRUNC\(&&sample_start_time\)"),
    }
    for vendor in OFFICIAL_VENDORS:
        schema_pattern, date_pattern = vendor_expectations[vendor]
        require_pattern(
            root,
            f"cpf-tools/db/vendor/{vendor}/source/10_cpf_schema.sql",
            schema_pattern,
            "transaction log server instance column is missing or has the wrong width",
            errors,
            re.I,
        )
        require_pattern(
            root,
            f"cpf-tools/db/vendor/{vendor}/source/70_test_data.sql",
            date_pattern,
            "LOG_DATE sample must be derived from START_TIME",
            errors,
            re.I,
        )

    annotation_count, duplicate_count = scan_execution_annotations(root, errors)
    legacy_count = scan_legacy_ids(root, errors)
    result = {
        "status": "PASS" if not errors else "FAIL",
        "officialVendors": list(OFFICIAL_VENDORS),
        "documentationSurface": OPENAPI_CONFIG,
        "executionAnnotationCount": annotation_count,
        "duplicateExecutionIdCount": duplicate_count,
        "legacyExecutionIdCount": legacy_count,
        "errors": errors,
    }
    if errors:
        raise GateError("\n".join(errors))
    return result


def write_json(root: Path, output_value: str | None, result: dict[str, object]) -> None:
    if not output_value:
        return
    output = Path(output_value)
    if not output.is_absolute():
        output = root / output
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = args.root.resolve()
    try:
        result = validate(root)
    except (GateError, OSError) as exc:
        result = {"status": "FAIL", "errors": str(exc).splitlines()}
        write_json(root, args.json_output, result)
        print(f"CPF transaction identity gate FAILED: {exc}", file=sys.stderr)
        return 1
    write_json(root, args.json_output, result)
    print(json.dumps(result, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

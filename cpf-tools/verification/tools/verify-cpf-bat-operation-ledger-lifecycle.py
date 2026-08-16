#!/usr/bin/env python3
"""Fail-closed V100 BAT operation-request ledger lifecycle gate."""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

VENDORS = ("mariadb", "postgresql", "oracle")
TABLE = "bat_operation_request"
MARIADB_R100_HELPER = "cpf_assert_empty_bat_operation_request_r100"


class LedgerLifecycleError(RuntimeError):
    pass


def normalize(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip().upper()


def require_file(path: Path, label: str) -> str:
    if not path.is_file():
        raise LedgerLifecycleError(f"{label}: missing {path}")
    text = path.read_text(encoding="utf-8-sig")
    if not text.strip():
        raise LedgerLifecycleError(f"{label}: empty {path}")
    return text


def canonical_has_table(path: Path) -> bool:
    data = json.loads(require_file(path, "canonical schema"))
    return any(str(table.get("name", "")).lower() == TABLE for table in data.get("tables", []))


def body(text: str, case_name: str) -> str:
    match = re.search(rf"(?is)'{re.escape(case_name)}'\s*\{{(.*?)\}}", text)
    if not match:
        raise LedgerLifecycleError(f"lifecycle script: mode block missing {case_name}")
    return match.group(1)


def require_order(text: str, tokens: list[str] | tuple[str, ...], label: str) -> None:
    cursor = 0
    for token in tokens:
        position = text.find(token, cursor)
        if position < 0:
            raise LedgerLifecycleError(f"{label}: missing/out-of-order token {token}")
        cursor = position + len(token)


def require_mariadb_retry_safe_guard(guard: str) -> None:
    helper = re.escape(MARIADB_R100_HELPER)
    create = re.search(rf"(?is)\bCREATE\s+PROCEDURE\s+`?{helper}`?\b", guard)
    if not create:
        raise LedgerLifecycleError("mariadb/R100: named fail-closed helper procedure missing")
    cleanup = re.search(
        rf"(?is)\bDROP\s+PROCEDURE\s+IF\s+EXISTS\s+`?{helper}`?\s*;",
        guard[: create.start()],
    )
    if not cleanup:
        raise LedgerLifecycleError(
            "mariadb/R100: retry-safe pre-cleanup missing before helper CREATE PROCEDURE"
        )
    call = re.search(rf"(?is)\bCALL\s+`?{helper}`?\s*\(\s*\)", guard)
    final_cleanup = re.search(
        rf"(?is)\bDROP\s+PROCEDURE\s+`?{helper}`?\b",
        guard[create.end() :],
    )
    if not call or not final_cleanup:
        raise LedgerLifecycleError("mariadb/R100: helper CALL/final cleanup missing")


def verify(root: Path) -> dict[str, object]:
    root = root.resolve()
    canonical = root / "cpf-tools/db/canonical/platform-schema.json"
    if not canonical_has_table(canonical):
        raise LedgerLifecycleError(f"canonical schema: {TABLE} missing")

    vendor_results: dict[str, dict[str, str]] = {}
    for vendor in VENDORS:
        base = root / "cpf-tools/db/vendor" / vendor
        source = require_file(base / "source/35_bat_schema.sql", f"{vendor}/source")
        install = require_file(base / "install/00_empty_install.sql", f"{vendor}/install")
        migration_path = base / "migration/V100__bat_operation_request_ledger.sql"
        rollback_path = base / "rollback/R100__bat_operation_request_ledger.sql"
        verify_path = base / "verify/V100__bat_operation_request_ledger.sql"
        migration = require_file(migration_path, f"{vendor}/V100")
        rollback = require_file(rollback_path, f"{vendor}/R100")
        verification = require_file(verify_path, f"{vendor}/verify V100")
        if TABLE.upper() not in normalize(source):
            raise LedgerLifecycleError(f"{vendor}/source: {TABLE} missing")
        if TABLE.upper() not in normalize(install):
            raise LedgerLifecycleError(f"{vendor}/install: {TABLE} missing")
        if not re.search(rf"(?is)\bCREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:[\w\"`$#]+\.)?[\"`]?{TABLE}[\"`]?\b", migration):
            raise LedgerLifecycleError(f"{vendor}/V100: CREATE TABLE {TABLE} missing")
        drop = re.search(rf"(?is)\bDROP\s+TABLE\s+(?:IF\s+EXISTS\s+)?(?:[\w\"`$#]+\.)?[\"`]?{TABLE}[\"`]?\b", rollback)
        if not drop:
            raise LedgerLifecycleError(f"{vendor}/R100: DROP TABLE {TABLE} missing")
        guard = rollback[:drop.start()]
        if TABLE.upper() not in normalize(guard) or not re.search(r"(?is)\b(EXISTS|COUNT\s*\()", guard):
            raise LedgerLifecycleError(f"{vendor}/R100: non-empty ledger rollback guard missing")
        required_guard = {
            "mariadb": "SIGNAL SQLSTATE",
            "postgresql": "RAISE EXCEPTION",
            "oracle": "RAISE_APPLICATION_ERROR",
        }[vendor]
        if required_guard not in normalize(guard):
            raise LedgerLifecycleError(f"{vendor}/R100: fail-closed marker missing {required_guard}")
        if vendor == "mariadb":
            require_mariadb_retry_safe_guard(guard)
        if TABLE.upper() not in normalize(verification) or not re.search(r"(?is)\bSELECT\b", verification):
            raise LedgerLifecycleError(f"{vendor}/verify V100: executable table verification missing")
        vendor_results[vendor] = {
            "migration": migration_path.relative_to(root).as_posix(),
            "rollback": rollback_path.relative_to(root).as_posix(),
            "verify": verify_path.relative_to(root).as_posix(),
        }

    lifecycle_path = root / "cpf-tools/db/tools/run-db-vendor-lifecycle.ps1"
    lifecycle = require_file(lifecycle_path, "lifecycle script")
    declarations = (
        "vendor-pack-manifest.json",
        "Get-CpfMigrationCatalog",
        "checksums.sha256",
        "initialize-cpf-database.ps1",
        "invoke-platform-database-migration.ps1",
        "ExpectedLifecyclePlanSha256",
    )
    for declaration in declarations:
        if declaration not in lifecycle:
            raise LedgerLifecycleError(f"lifecycle script: missing declaration {declaration}")
    require_order(
        lifecycle,
        ("role = 'Provision'", "role = 'EmptyInstall'", "role = 'ProductSeed'", "role = 'Verify'"),
        "FreshInstall manifest order",
    )
    require_order(
        lifecycle,
        ("stage = 'Rollback'", "stage = 'Reapply'"),
        "Current-edge rollback/reapply",
    )
    for forbidden in ("$v98=", "$v99=", "$v100=", "Run-Sql"):
        if forbidden in lifecycle:
            raise LedgerLifecycleError(f"lifecycle script: hard-coded/direct SQL token forbidden {forbidden}")
    return {
        "status": "PASS",
        "table": TABLE,
        "vendors": vendor_results,
        "lifecycleScript": lifecycle_path.relative_to(root).as_posix(),
        "meaning": "Static/Semantic lifecycle closure including MariaDB failed-rollback retry safety; real vendor execution remains required",
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        result = verify(Path(args.root))
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    text = json.dumps(result, ensure_ascii=False, indent=2)
    if args.json_output:
        output = Path(args.json_output)
        output = output if output.is_absolute() else Path(args.root).resolve() / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(text + "\n", encoding="utf-8")
    print(text)
    return code


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3
"""Fail-closed CPF physical database consolidation verifier.

Current Product Runtime may use exactly cpfDB/mbwDB/mbrDB/exsDB as production
physical database identities. referenceFixture is non-production only. Historical
immutable migration bytes may retain retired logical names, but active runtime,
current generated SQL, DataSource defaults and current canonical contracts may not
promote cmnDB/admDB/batDB/bzaDB/refDB back to physical targets.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

ALLOWED_PRODUCTION = {"cpfDB", "mbwDB", "mbrDB", "exsDB"}
RETIRED = {"cmnDB", "admDB", "batDB", "bzaDB", "refDB"}
REFERENCE_FIXTURE = "referenceFixture"
TEXT_SUFFIXES = {".java", ".kt", ".groovy", ".gradle", ".yml", ".yaml", ".properties", ".env", ".ps1", ".sh", ".cmd", ".bat", ".sql", ".json"}

# Product/current execution surfaces. Historical migration/rollback trees are
# intentionally excluded because released bytes are immutable evidence, not current targets.
ACTIVE_ROOTS = (
    "cpf-admin/src/main",
    "cpf-backoffice/src/main",
    "cpf-backoffice-web/src/main",
    "cpf-batch",
    "cpf-common/src/main",
    "cpf-core/src/main",
    "cpf-education/src/main",
    "cpf-external",
    "cpf-gateway/src/main",
    "cpf-member",
    "cpf-starters",
    "deploy/environments",
    "cpf-tools/db/tools",
    "cpf-tools/runtime",
    "cpf-tools/security/tools",
    "cpf-tools/environment/docker-development-test",
    "cpf-tools/db/generated/current",
)

SKIP_PARTS = {"build", "node_modules", "dist", ".gradle", ".git", "__pycache__", ".pytest_cache", "tests", "test"}


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig", errors="strict")


def actionable_legacy_hits(text: str) -> list[str]:
    hits: list[str] = []
    for name in RETIRED:
        escaped = re.escape(name)
        patterns = (
            rf"(?i)jdbc:[^\s'\"}}]+/{escaped}(?:[?;/'\"\s]|$)",
            rf"(?im)^\s*USE\s+`?{escaped}`?\s*;",
            rf"(?i)CREATE\s+(?:DATABASE|SCHEMA)\s+(?:IF\s+NOT\s+EXISTS\s+)?[\"`]?{escaped}[\"`]?\b",
            rf"(?i)\b(?:database|databaseName|dbName|schema)\s*[:=]\s*[\"']?{escaped}[\"']?\b",
            rf"(?i)\b(?:DB_URL|DATASOURCE_URL|JDBC_URL)\s*=.*?/{escaped}(?:[?;/'\"\s]|$)",
        )
        for pattern in patterns:
            if re.search(pattern, text):
                hits.append(name)
                break
    return sorted(set(hits))


def active_files(root: Path):
    seen: set[Path] = set()
    for rel in ACTIVE_ROOTS:
        base = root / rel
        if not base.exists():
            continue
        for path in base.rglob("*"):
            if not path.is_file() or path.suffix.lower() not in TEXT_SUFFIXES:
                continue
            relp = path.relative_to(root)
            # Keep canonical batch bin shell source even though path component is bin;
            # exclude tests/build artifacts and immutable history trees.
            parts = set(relp.parts)
            if parts & SKIP_PARTS:
                continue
            posix = relp.as_posix()
            if "/migration/" in posix or "/rollback/" in posix or "/historical/" in posix:
                continue
            if path not in seen:
                seen.add(path)
                yield path


def verify(root: Path) -> tuple[list[str], dict[str, object]]:
    failures: list[str] = []
    info: dict[str, object] = {}

    schema_path = root / "cpf-tools/db/canonical/platform-schema.json"
    if not schema_path.is_file():
        return ["canonical platform-schema.json missing"], info
    schema = json.loads(read_text(schema_path))
    policy = schema.get("canonicalPolicy") or {}
    production = set(policy.get("productionPhysicalTargets") or [])
    retired = set(policy.get("removedProductionPhysicalTargets") or [])
    if production != ALLOWED_PRODUCTION:
        failures.append(f"productionPhysicalTargets must be exactly {sorted(ALLOWED_PRODUCTION)}; actual={sorted(production)}")
    if retired != RETIRED:
        failures.append(f"removedProductionPhysicalTargets must be exactly {sorted(RETIRED)}; actual={sorted(retired)}")

    arch = policy.get("platformDatabaseArchitecture") or {}
    if ((arch.get("CPF_PLATFORM_DB") or {}).get("defaultPhysicalName")) != "cpfDB":
        failures.append("CPF_PLATFORM_DB defaultPhysicalName must be cpfDB")
    customer = arch.get("CUSTOMER_BUSINESS_DB") or {}
    if customer.get("backofficeDefaultPhysicalName") != "mbwDB":
        failures.append("Backoffice default physical DB must be mbwDB")
    generated_defaults = customer.get("generatedDomainDefaultPhysicalNames") or {}
    if generated_defaults != {"MBR": "mbrDB", "EXS": "exsDB"}:
        failures.append(f"Generated Domain physical DB defaults drift: {generated_defaults}")
    ref = arch.get("REFERENCE_FIXTURE") or {}
    if ref.get("physicalName") != REFERENCE_FIXTURE or ref.get("productionDefault") is not False:
        failures.append("referenceFixture must remain non-production fixture-only")

    table_targets: set[str] = set()
    for table in schema.get("tables") or []:
        if table.get("productionDefault", True):
            target = table.get("targetPhysicalDatabase")
            if not target:
                failures.append(f"{table.get('name')}: production table targetPhysicalDatabase missing")
                continue
            table_targets.add(str(target))
            if target in RETIRED:
                failures.append(f"{table.get('name')}: retired active target {target}")
            if target not in ALLOWED_PRODUCTION:
                failures.append(f"{table.get('name')}: unsupported production physical target {target}")
        current = table.get("currentLogicalDatabase")
        if current in RETIRED:
            failures.append(f"{table.get('name')}: retired currentLogicalDatabase {current}; preserve only as legacyLogicalDatabase")
    info["canonicalProductionTableTargets"] = sorted(table_targets)

    nto_path = root / "cpf-tools/db/canonical/platform-non-table-objects.json"
    if not nto_path.is_file():
        failures.append("platform-non-table-objects.json missing")
    else:
        nto = json.loads(read_text(nto_path))
        current_db = (nto.get("canonicalPolicy") or {}).get("currentLogicalDatabase")
        current_file = (nto.get("canonicalPolicy") or {}).get("currentSourceFile")
        if current_db != "cpfDB" or current_file != "10_cpf_schema.sql":
            failures.append(f"Spring Batch current non-table owner must be cpfDB/10_cpf_schema.sql, actual={current_db}/{current_file}")
        for obj in nto.get("objects") or []:
            if obj.get("logicalDatabase") != "cpfDB" or obj.get("sourceFile") != "10_cpf_schema.sql":
                failures.append(f"{obj.get('name')}: current non-table object is not cpfDB current projection")
            if obj.get("legacyLogicalDatabase") != "batDB" or obj.get("legacySourceFile") != "35_bat_schema.sql":
                failures.append(f"{obj.get('name')}: immutable Batch history metadata missing")

    active_hits: list[dict[str, object]] = []
    scanned = 0
    for path in active_files(root):
        scanned += 1
        try:
            content = read_text(path)
        except UnicodeDecodeError as exc:
            failures.append(f"non-UTF8 active source: {path.relative_to(root).as_posix()}: {exc}")
            continue
        hits = actionable_legacy_hits(content)
        if hits:
            rel = path.relative_to(root).as_posix()
            active_hits.append({"path": rel, "retiredTargets": hits})
            failures.append(f"active physical DB reference: {rel}: {','.join(hits)}")
    info["activeFilesScanned"] = scanned
    info["activeLegacyReferenceCount"] = len(active_hits)
    info["activeLegacyReferences"] = active_hits[:100]

    # Explicit high-risk entrypoints: these used to carry retired defaults and must never regress.
    explicit = {
        "cpf-admin/src/main/resources/application-adm-local.yml": RETIRED,
        "cpf-tools/security/tools/apply-v15-adm-api-permission-management.ps1": RETIRED,
        "cpf-tools/verification/tools/smoke-center-cut-adapter.ps1": {"refDB"},
    }
    for rel, forbidden in explicit.items():
        path = root / rel
        if path.is_file():
            text = read_text(path)
            bad = sorted(x for x in forbidden if x in actionable_legacy_hits(text))
            if bad:
                failures.append(f"high-risk runtime entrypoint retains retired DB: {rel}:{','.join(bad)}")

    return failures, info


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    failures, info = verify(root)
    payload = {
        "status": "PASS" if not failures else "FAIL",
        "productionPhysicalTargets": sorted(ALLOWED_PRODUCTION),
        "retiredProductionPhysicalTargets": sorted(RETIRED),
        "referenceFixturePolicy": "NON_PRODUCTION_ONLY",
        **info,
        "failureCount": len(failures),
        "failures": failures,
    }
    if args.json_output:
        out = Path(args.json_output)
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(payload, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())

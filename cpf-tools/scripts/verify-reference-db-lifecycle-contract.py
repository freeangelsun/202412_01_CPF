#!/usr/bin/env python3
"""Fail-closed static verification for the removable CPF reference DB overlays."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path
from typing import Any


def fail(message: str) -> None:
    print(f"[CPF][REF-DB-LIFECYCLE][FAIL] {message}", file=sys.stderr)
    raise SystemExit(1)


def load_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        fail(f"JSON을 읽을 수 없습니다: {path}: {exc}")
    if not isinstance(value, dict):
        fail(f"JSON root는 object여야 합니다: {path}")
    return value


def sql_identifiers(sql: str, keyword: str) -> set[str]:
    pattern = rf"\b{keyword}\s+(?:IF\s+(?:NOT\s+)?EXISTS\s+)?(?:[A-Za-z0-9_$#\"`]+\.)?[\"`]?([A-Za-z][A-Za-z0-9_$#]*)[\"`]?"
    return {match.upper() for match in re.findall(pattern, sql, re.IGNORECASE)}


def owned_table_mentions(sql: str, owned_tables: set[str]) -> set[str]:
    upper_sql = sql.upper()
    return {
        table
        for table in owned_tables
        if re.search(rf"(?<![A-Z0-9_$#]){re.escape(table)}(?![A-Z0-9_$#])", upper_sql)
    }


def resolve_artifact(base: Path, relative: str, root: Path) -> Path:
    candidate = (base / relative).resolve()
    try:
        candidate.relative_to(base.resolve())
    except ValueError:
        fail(f"Vendor artifact가 pack root 밖을 가리킵니다: {relative}")
    if not candidate.is_file():
        fail(f"Vendor artifact가 없습니다: {candidate.relative_to(root)}")
    return candidate


def checksum_entries(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    for line_number, raw_line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        match = re.fullmatch(r"([0-9a-fA-F]{64})\s+\*?([^\s]+)", line)
        if not match:
            fail(f"Checksum manifest 형식이 올바르지 않습니다: {path}:{line_number}")
        filename = match.group(2)
        if filename in result:
            fail(f"Checksum manifest filename이 중복됩니다: {path}:{filename}")
        result[filename] = match.group(1).lower()
    return result


def normalized_bind_name(name: str) -> str:
    snake = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", name)
    return snake.lower()


def ddl_object_names(sql: str) -> tuple[set[str], set[str], set[str]]:
    tables = sql_identifiers(sql, "CREATE\\s+TABLE")
    indexes = sql_identifiers(sql, "CREATE\\s+(?:UNIQUE\\s+)?INDEX")
    constraints = {
        match.upper()
        for match in re.findall(r"\bCONSTRAINT\s+([A-Za-z][A-Za-z0-9_$#]*)", sql, re.IGNORECASE)
    }
    return tables, indexes, constraints


def assert_select_only(path: Path, sql: str) -> None:
    without_comments = re.sub(r"(?m)^\s*--.*$", "", sql)
    statements = [statement.strip() for statement in without_comments.split(";") if statement.strip()]
    if not statements or any(not re.match(r"^SELECT\b", statement, re.IGNORECASE) for statement in statements):
        fail(f"Runtime query pack은 SELECT-only여야 합니다: {path}")


def validate_state_contract(contract: dict[str, Any], packs: dict[str, Any]) -> None:
    states = contract.get("expectedSchemaStates")
    transitions = contract.get("lifecycleTransitions")
    if not isinstance(states, dict) or not isinstance(transitions, list):
        fail("expectedSchemaStates/lifecycleTransitions 계약이 없습니다.")

    required_states = {
        "baseline": (),
        "core": ("core",),
        "coreAndBatch": ("core", "batch"),
    }
    aliases: set[str] = set()
    for state_name, expected_packs in required_states.items():
        state = states.get(state_name)
        if not isinstance(state, dict):
            fail(f"Schema state가 없습니다: {state_name}")
        enabled = tuple(state.get("enabledPacks", ()))
        if enabled != expected_packs:
            fail(f"Schema state pack 순서/구성이 다릅니다: {state_name}={enabled}")
        for pack_name in enabled:
            dependencies = tuple(packs[pack_name].get("dependencies", ()))
            if any(dependency not in enabled for dependency in dependencies):
                fail(f"Schema state dependency가 닫혀 있지 않습니다: {state_name}/{pack_name}")
        for alias in state.get("aliases", ()): 
            normalized = str(alias).lower()
            if normalized in aliases:
                fail(f"Schema state alias가 중복됩니다: {alias}")
            aliases.add(normalized)

    expected_transitions = [
        ("baseline", "V93", "core"),
        ("core", "V94", "coreAndBatch"),
        ("coreAndBatch", "U94", "core"),
        ("core", "U93", "baseline"),
    ]
    actual_transitions = [
        (item.get("from"), item.get("operation"), item.get("to"))
        for item in transitions
        if isinstance(item, dict)
    ]
    if actual_transitions != expected_transitions:
        fail(f"Schema lifecycle transition이 다릅니다: {actual_transitions}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    root = Path(args.root).resolve()

    contract_path = root / "cpf-tools/generator/contracts/reference-edu-schema-ownership-contract.json"
    vendor_manifest_path = root / "cpf-tools/db/vendor-pack-manifest.json"
    contract = load_json(contract_path)
    vendor_manifest = load_json(vendor_manifest_path)

    official_vendors = tuple(vendor_manifest.get("officialVendors", ()))
    required_vendors = tuple(contract.get("requiredVendors", ()))
    if set(required_vendors) != set(official_vendors) or len(required_vendors) != len(official_vendors):
        fail(f"Reference pack vendor 집합이 official vendor와 다릅니다: {required_vendors}/{official_vendors}")
    if contract.get("ownerModule") != "cpf-reference" or contract.get("logicalDatabase") != "refDB":
        fail("Reference schema ownership은 cpf-reference/refDB여야 합니다.")
    if contract.get("generatedDomainAllowed") is not False:
        fail("Reference schema는 generated domain에 포함될 수 없습니다.")
    lifecycle_mapping = {
        "source": "source",
        "install": "install",
        "upgrade": "migration",
        "rollback": "rollback",
        "runtime-query": "runtimeQuery",
        "verify": "verify",
        "checksum": "checksum",
    }
    if set(contract.get("requiredLifecycle", ())) != set(lifecycle_mapping):
        fail("requiredLifecycle 계약이 source/install/upgrade/rollback/runtime-query/verify/checksum과 다릅니다.")
    verification_policy = contract.get("verificationPolicy")
    if not isinstance(verification_policy, dict) or verification_policy.get("failClosed") is not True:
        fail("verificationPolicy.failClosed는 true여야 합니다.")
    if set(verification_policy.get("requiredObjectKinds", ())) != {"table", "index", "constraint"}:
        fail("Verify는 table/index/constraint를 모두 검증해야 합니다.")
    live_validation = contract.get("liveDbValidation")
    if not isinstance(live_validation, dict):
        fail("liveDbValidation canonical contract가 없습니다.")
    required_live_assertions = {
        "same-key-same-hash-replay",
        "same-key-different-hash-conflict",
        "single-row-preserved",
        "cleanup-zero-rows",
    }
    if set(live_validation.get("requiredAssertions", ())) != required_live_assertions:
        fail("Live DB idempotency assertion 계약이 다릅니다.")
    for key in ("runner", "testSource", "repositorySource"):
        live_path = (root / str(live_validation.get(key, ""))).resolve()
        try:
            live_path.relative_to(root)
        except ValueError:
            fail(f"liveDbValidation.{key}가 repository 밖을 가리킵니다.")
        if not live_path.is_file():
            fail(f"liveDbValidation.{key} artifact가 없습니다: {live_path}")

    packs = contract.get("featurePacks")
    if not isinstance(packs, dict) or set(packs) != {"core", "batch"}:
        fail("Reference feature pack은 core/batch 두 개여야 합니다.")
    expected_versions = {"core": 93, "batch": 94}
    all_tables: set[str] = set()
    for pack_name, metadata in packs.items():
        if not isinstance(metadata, dict):
            fail(f"Feature pack metadata가 object가 아닙니다: {pack_name}")
        if metadata.get("migrationVersion") != expected_versions[pack_name]:
            fail(f"Feature pack migration version이 다릅니다: {pack_name}")
        prefix = str(metadata.get("tablesPrefix", "")).upper()
        tables = tuple(str(table).upper() for table in metadata.get("tables", ()))
        if not prefix or not tables or any(not table.startswith(prefix) for table in tables):
            fail(f"Feature pack table prefix/table 계약이 다릅니다: {pack_name}")
        overlap = all_tables.intersection(tables)
        if overlap:
            fail(f"Feature pack table ownership이 중복됩니다: {sorted(overlap)}")
        all_tables.update(tables)
        if not metadata.get("vendorPackKey"):
            fail(f"Feature pack vendorPackKey가 없습니다: {pack_name}")
        runtime_bindings = metadata.get("runtimeBindings")
        if not isinstance(runtime_bindings, list) or not runtime_bindings:
            fail(f"Feature pack runtimeBindings가 없습니다: {pack_name}")
        if any(binding != normalized_bind_name(binding) for binding in runtime_bindings):
            fail(f"runtimeBindings는 canonical snake_case여야 합니다: {pack_name}")
        if len(runtime_bindings) != len(set(runtime_bindings)):
            fail(f"runtimeBindings가 중복됩니다: {pack_name}")
    if packs["core"].get("dependencies") != [] or packs["batch"].get("dependencies") != ["core"]:
        fail("Batch pack은 core에만 의존해야 합니다.")
    if packs["core"].get("optional") is not False or packs["batch"].get("optional") is not True:
        fail("core/batch optional 계약이 다릅니다.")
    validate_state_contract(contract, packs)

    baseline_files = [
        root / "cpf-tools/db/canonical/platform-schema.json",
        root / "cpf-tools/db/generated/database-schema-manifest.json",
    ]
    for vendor in official_vendors:
        baseline_files.append(root / f"cpf-tools/db/vendor/{vendor}/install/00_empty_install.sql")
    for baseline_file in baseline_files:
        if not baseline_file.is_file():
            fail(f"Baseline schema artifact가 없습니다: {baseline_file.relative_to(root)}")
        mentions = owned_table_mentions(baseline_file.read_text(encoding="utf-8"), all_tables)
        if mentions:
            fail(f"Baseline schema에 optional REF overlay가 포함되었습니다: {baseline_file.relative_to(root)} {sorted(mentions)}")

    artifact_roles = {"source", "install", "migration", "rollback", "runtimeQuery", "verify", "checksum"}
    vendor_signatures: dict[str, dict[str, tuple[set[str], set[str], set[str]]]] = {}
    for vendor in official_vendors:
        vendor_base = root / f"cpf-tools/db/vendor/{vendor}"
        pack_manifest = load_json(vendor_base / "pack.json")
        if pack_manifest.get("vendor") != vendor:
            fail(f"Vendor pack identity가 다릅니다: {vendor}")

        vendor_signatures[vendor] = {}
        shared_checksum_paths: set[Path] = set()
        for pack_name, metadata in packs.items():
            expected_tables = {str(table).upper() for table in metadata["tables"]}
            artifacts = metadata.get("artifacts")
            if not isinstance(artifacts, dict) or set(artifacts) != artifact_roles:
                fail(f"Feature pack lifecycle artifact 집합이 다릅니다: {pack_name}")
            resolved = {
                role: resolve_artifact(vendor_base, str(relative), root)
                for role, relative in artifacts.items()
            }
            shared_checksum_paths.add(resolved["checksum"])

            migration_version = int(metadata["migrationVersion"])
            if resolved["migration"].parent.name != "refDB" or resolved["rollback"].parent.name != "refDB":
                fail(f"{vendor}/{pack_name} migration/rollback은 refDB pack에 있어야 합니다.")
            if not resolved["migration"].name.startswith(f"V{migration_version}__"):
                fail(f"{vendor}/{pack_name} migration version/path가 다릅니다.")
            if not resolved["rollback"].name.startswith(f"U{migration_version}__"):
                fail(f"{vendor}/{pack_name} rollback version/path가 다릅니다.")

            source_signature: tuple[set[str], set[str], set[str]] | None = None
            for role in ("source", "install", "migration"):
                sql = resolved[role].read_text(encoding="utf-8")
                signature = ddl_object_names(sql)
                actual_tables = signature[0]
                if actual_tables != expected_tables:
                    fail(f"{vendor}/{pack_name}/{role} CREATE TABLE 집합이 다릅니다: {sorted(actual_tables)}")
                mentions = owned_table_mentions(sql, all_tables)
                if not mentions.issubset(expected_tables):
                    fail(f"{vendor}/{pack_name}/{role}가 다른 overlay table을 참조합니다: {sorted(mentions - expected_tables)}")
                if role == "source":
                    source_signature = signature
                    if not signature[1] or not signature[2]:
                        fail(f"{vendor}/{pack_name} canonical source index/constraint가 비어 있습니다.")
                elif signature != source_signature:
                    fail(f"{vendor}/{pack_name}/{role} DDL object signature가 canonical source와 다릅니다.")
            assert source_signature is not None
            vendor_signatures[vendor][pack_name] = source_signature

            rollback_sql = resolved["rollback"].read_text(encoding="utf-8")
            dropped_tables = sql_identifiers(rollback_sql, "DROP\\s+TABLE")
            if dropped_tables != expected_tables:
                fail(f"{vendor}/{pack_name}/rollback DROP TABLE 집합이 다릅니다: {sorted(dropped_tables)}")

            for role in ("runtimeQuery", "verify"):
                sql = resolved[role].read_text(encoding="utf-8")
                mentions = owned_table_mentions(sql, all_tables)
                if mentions != expected_tables:
                    fail(f"{vendor}/{pack_name}/{role} table 소비 집합이 다릅니다: {sorted(mentions)}")
                if role == "runtimeQuery":
                    assert_select_only(resolved[role], sql)
                    actual_bindings = {
                        normalized_bind_name(name)
                        for name in re.findall(r":([A-Za-z_][A-Za-z0-9_]*)", sql)
                    }
                    expected_bindings = set(metadata["runtimeBindings"])
                    if actual_bindings != expected_bindings:
                        fail(f"{vendor}/{pack_name} runtime binding 계약이 다릅니다: {sorted(actual_bindings)}")
                else:
                    expected_object_names = set().union(*source_signature)
                    verify_mentions = owned_table_mentions(sql, expected_object_names)
                    if verify_mentions != expected_object_names:
                        fail(f"{vendor}/{pack_name} verify object coverage가 다릅니다: {sorted(verify_mentions)}")
                    fail_closed_token = {
                        "mariadb": "SIGNAL SQLSTATE ''45000''",
                        "postgresql": "RAISE EXCEPTION",
                        "oracle": "RAISE_APPLICATION_ERROR",
                    }[vendor]
                    if fail_closed_token not in sql.upper():
                        fail(f"{vendor}/{pack_name} verify가 fail-closed가 아닙니다.")
                    if "CHECK_NAME" not in sql.upper() or "PASSED" not in sql.upper():
                        fail(f"{vendor}/{pack_name} verify sentinel output이 없습니다.")

            checksums = checksum_entries(resolved["checksum"])
            migration = resolved["migration"]
            actual_digest = hashlib.sha256(migration.read_bytes()).hexdigest()
            if checksums.get(migration.name) != actual_digest:
                fail(f"{vendor}/{pack_name} migration checksum이 다릅니다: {migration.name}")

            vendor_pack_key = str(metadata["vendorPackKey"])
            vendor_pack_metadata = pack_manifest.get(vendor_pack_key)
            if not isinstance(vendor_pack_metadata, dict):
                fail(f"{vendor} pack.json에 {vendor_pack_key}가 없습니다.")
            if set(map(str.upper, vendor_pack_metadata.get("tables", ()))) != expected_tables:
                fail(f"{vendor}/{vendor_pack_key} table metadata가 canonical contract와 다릅니다.")
            path_map = {
                "source": "canonicalSource",
                "install": "freshInstall",
                "migration": "migration",
                "rollback": "rollback",
                "runtimeQuery": "runtimeQueries",
                "verify": "verify",
                "checksum": "checksumManifest",
            }
            for contract_role, vendor_key in path_map.items():
                if vendor_pack_metadata.get(vendor_key) != artifacts[contract_role]:
                    fail(f"{vendor}/{vendor_pack_key}/{vendor_key} path metadata가 canonical contract와 다릅니다.")

        for checksum_path in shared_checksum_paths:
            entries = checksum_entries(checksum_path)
            migration_files = {path.name: path for path in checksum_path.parent.glob("V*.sql")}
            if set(entries) != set(migration_files):
                fail(f"{vendor} refDB checksum/file 집합이 다릅니다: {checksum_path.relative_to(root)}")
            for filename, migration_path in migration_files.items():
                digest = hashlib.sha256(migration_path.read_bytes()).hexdigest()
                if entries[filename] != digest:
                    fail(f"{vendor} refDB checksum drift: {migration_path.relative_to(root)}")

    reference_vendor = official_vendors[0]
    for vendor in official_vendors[1:]:
        if vendor_signatures[vendor] != vendor_signatures[reference_vendor]:
            fail(f"Cross-vendor REF DDL object-name parity가 다릅니다: {reference_vendor}/{vendor}")

    print(
        "[CPF][REF-DB-LIFECYCLE][PASS] "
        f"vendors={len(official_vendors)} states=3 transitions=4 coreTables=7 batchTables=3 baselineOverlayTables=0"
    )


if __name__ == "__main__":
    main()

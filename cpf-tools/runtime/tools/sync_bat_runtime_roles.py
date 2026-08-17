#!/usr/bin/env python3
"""Synchronize and fail-closed verify the canonical BAT runtime-role contract.

The JSON contract is the only authoring source for active runtime-role names.  This
tool currentizes deploy resources and DB authoring templates, renders the two
affected DB3 runtime queries, and renders the V116 forward/rollback data migration.
Released historical migrations are never opened or rewritten.
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import sys
from pathlib import Path
from typing import Any


class ContractError(RuntimeError):
    pass


def read_text(path: Path) -> str:
    if not path.is_file():
        raise ContractError(f"required file missing: {path.as_posix()}")
    return path.read_text(encoding="utf-8-sig")


def write_or_check(path: Path, expected: str, write: bool, changed: list[str]) -> None:
    actual = read_text(path) if path.is_file() else None
    if actual == expected:
        return
    if not write:
        raise ContractError(f"generated/current contract drift: {path.as_posix()}")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(expected, encoding="utf-8", newline="\n")
    changed.append(path.as_posix())


def replace_all(text: str, replacements: dict[str, str]) -> str:
    for old in sorted(replacements, key=len, reverse=True):
        text = text.replace(old, replacements[old])
    return text


def transform_json_strings(value: Any, replacements: dict[str, str]) -> Any:
    if isinstance(value, str):
        return replace_all(value, replacements)
    if isinstance(value, list):
        return [transform_json_strings(item, replacements) for item in value]
    if isinstance(value, dict):
        return {key: transform_json_strings(item, replacements) for key, item in value.items()}
    return value


def canonical_roles(contract: dict[str, Any]) -> list[str]:
    roles = [str(item["name"]) for item in contract["roles"]]
    required = ["CONTROL_PLANE", "SCHEDULER", "WORKER", "CENTER_CUT", "AGENT"]
    if roles != required or len(set(roles)) != len(roles):
        raise ContractError(f"canonical role order/set mismatch: {roles}")
    return roles


def replacement_map(contract: dict[str, Any]) -> dict[str, str]:
    replacements: dict[str, str] = {}
    canonical_names: dict[str, set[str]] = {
        "role": set(), "environment": set(), "artifact": set(), "project": set(), "port": set()
    }
    retired_names: dict[str, set[str]] = {"role": set(), "environment": set(), "artifact": set()}

    def register_replacement(alias: str, target: str, namespace: str) -> None:
        if alias in replacements and replacements[alias] != target:
            raise ContractError(f"cross-namespace retired alias collision: {alias}: {replacements[alias]} != {target}")
        if alias in retired_names[namespace]:
            raise ContractError(f"duplicate retired {namespace} alias: {alias}")
        retired_names[namespace].add(alias)
        replacements[alias] = target

    for role in contract["roles"]:
        canonical = str(role["name"])
        identity = {
            "role": canonical,
            "environment": str(role["envPrefix"]),
            "artifact": str(role["artifactName"]),
            "project": str(role["projectPath"]),
            "port": str(role["localPort"]),
        }
        for namespace, value in identity.items():
            if value in canonical_names[namespace]:
                raise ContractError(f"duplicate canonical {namespace}: {value}")
            canonical_names[namespace].add(value)
        for alias in role.get("legacyAliases", []):
            register_replacement(str(alias), canonical, "role")
        env_prefix = str(role["envPrefix"])
        for alias in role.get("legacyEnvPrefixes", []):
            register_replacement(str(alias), env_prefix, "environment")
        artifact = str(role["artifactName"])
        for alias in role.get("legacyArtifactNames", []):
            register_replacement(str(alias), artifact, "artifact")
    for namespace in ("role", "environment", "artifact"):
        collisions = canonical_names[namespace].intersection(retired_names[namespace])
        if collisions:
            raise ContractError(f"canonical/retired {namespace} collision: {sorted(collisions)}")
    if len(set(replacements.values())) < 3:
        raise ContractError("retired role mapping is incomplete")
    return replacements


def verify_migration_version_lock(root: Path, contract: dict[str, Any]) -> None:
    migration = contract["migration"]
    allocated = int(migration["allocatedVersion"])
    observed = int(migration["observedRepositoryMaxVersionAtAllocation"])
    discovered: list[int] = []
    for path in (root / "cpf-tools/db/vendor").glob("*/migration/**/*.sql"):
        match = re.match(r"V(\d+)__", path.name)
        if match and int(match.group(1)) < allocated:
            discovered.append(int(match.group(1)))
    actual = max(discovered, default=0)
    if actual != observed or allocated != observed + 1:
        raise ContractError(
            f"migration version lock mismatch: actualPriorMax={actual} observed={observed} allocated={allocated}"
        )


def verify_java_enum(root: Path, contract: dict[str, Any]) -> None:
    text = read_text(root / contract["canonicalJavaEnum"])
    match = re.search(r"enum\s+RuntimeRole\s*\{(?P<body>.*?)\}", text, re.S)
    if not match:
        raise ContractError("RuntimeRole enum not found")
    body = re.sub(r"/\*.*?\*/|//[^\n]*", "", match.group("body"), flags=re.S)
    values = [part.strip() for part in body.replace(";", ",").split(",") if part.strip()]
    if values != canonical_roles(contract):
        raise ContractError(f"Java RuntimeRole parity mismatch: {values}")


def verify_owned_surfaces(root: Path, contract: dict[str, Any]) -> None:
    runtime_sql = contract["runtimeSql"]
    deployment = contract["deployment"]
    if len(runtime_sql["authoringFiles"]) != 4:
        raise ContractError("BAT runtime-role authoring surface must contain exactly four files")
    if len(runtime_sql["generatedKeys"]) * len(runtime_sql["vendors"]) != 6:
        raise ContractError("BAT runtime-role generated DB3 surface must contain exactly six files")
    deploy_count = 1 + len(deployment["inventories"]) + len(deployment["environmentFiles"]) + len(deployment["topologies"])
    if deploy_count != 13:
        raise ContractError(f"BAT runtime-role deploy surface must contain exactly 13 files: {deploy_count}")

    schema = json.loads(read_text(root / "cpf-tools/db/canonical/platform-schema.json"))
    actual_targets: set[tuple[str, str, str]] = set()
    table_by_target: dict[tuple[str, str], dict[str, Any]] = {}
    for table in schema.get("tables", []):
        table_by_target[(str(table["logicalDatabase"]), str(table["name"]).lower())] = table
        for column in table.get("columns", []):
            if str(column.get("name", "")).lower() == "runtime_role":
                actual_targets.add((str(table["logicalDatabase"]), str(table["name"]).lower(), "runtime_role"))
    expected_targets = {
        (str(item["logicalDatabase"]), str(item["table"]).lower(), str(item["column"]).lower())
        for item in contract["migration"]["targets"]
    }
    if actual_targets != expected_targets:
        raise ContractError(
            f"canonical schema runtime_role target parity mismatch: expected={sorted(expected_targets)} actual={sorted(actual_targets)}"
        )
    for target in validate_migration_targets(contract):
        table = table_by_target[(str(target["logicalDatabase"]), str(target["table"]).lower())]
        column = next(item for item in table["columns"] if str(item["name"]).lower() == str(target["column"]).lower())
        if bool(column["nullable"]) != bool(target["nullable"]):
            raise ContractError(f"runtime_role NULL policy drift: {target['table']}")
        checks = {str(item["name"]): item for item in table.get("checks", [])}
        check = checks.get(str(target["constraint"]))
        if check is None:
            raise ContractError(f"canonical runtime_role CHECK missing: {target['table']}.{target['constraint']}")
        expected_default = constraint_expression(contract, target, "postgresql")
        expected_maria = constraint_expression(contract, target, "mariadb")
        normalize = lambda value: re.sub(r"\s+", "", str(value))
        if normalize(check.get("expression")) != normalize(expected_default):
            raise ContractError(f"canonical runtime_role CHECK expression drift: {target['constraint']}")
        if normalize((check.get("vendorExpressions") or {}).get("mariadb")) != normalize(expected_maria):
            raise ContractError(f"MariaDB case-sensitive runtime_role CHECK drift: {target['constraint']}")


def render_runtime_sql(root: Path, contract: dict[str, Any], write: bool, changed: list[str]) -> None:
    replacements = replacement_map(contract)
    sql = contract["runtimeSql"]
    author_paths = [root / item for item in sql["authoringFiles"]]
    for path in author_paths:
        expected = replace_all(read_text(path), replacements)
        write_or_check(path, expected, write, changed)

    template_root = root / "cpf-tools/db/runtime-template/bat"
    for vendor in sql["vendors"]:
        for key in sql["generatedKeys"]:
            override = template_root / "vendor" / vendor / "repository" / f"{key}.sql.template"
            common = template_root / "repository" / f"{key}.sql.template"
            source = override if override.is_file() else common
            expected = read_text(source).strip() + "\n"
            target = root / f"cpf-tools/db/vendor/{vendor}/runtime/bat/repository/{key}.sql"
            write_or_check(target, expected, write, changed)


def render_deploy(root: Path, contract: dict[str, Any], write: bool, changed: list[str]) -> None:
    replacements = replacement_map(contract)
    deployment = contract["deployment"]
    schema_path = root / deployment["schema"]
    schema = json.loads(read_text(schema_path))
    schema["properties"]["runtimeRole"]["enum"] = canonical_roles(contract)
    write_or_check(schema_path, json.dumps(schema, ensure_ascii=False, indent=2) + "\n", write, changed)

    role_by_name = {item["name"]: item for item in contract["roles"]}
    for relative in deployment["inventories"]:
        path = root / relative
        inventory = transform_json_strings(json.loads(read_text(path)), replacements)
        batch = [item for item in inventory.get("services", []) if item.get("module") == "BAT"]
        actual_roles = [item.get("runtimeRole") for item in batch]
        if sorted(actual_roles) != sorted(canonical_roles(contract)):
            raise ContractError(f"BAT inventory role cardinality mismatch: {relative}: {actual_roles}")
        for item in batch:
            role = role_by_name[item["runtimeRole"]]
            item["projectPath"] = role["projectPath"]
            item["artifactName"] = role["artifactName"]
            item["serviceName"] = role["artifactName"]
            item["deployBase"] = f"build/deploy/{role['artifactName']}"
            item["portEnvKey"] = f"{role['envPrefix']}_PORT"
        expected = json.dumps(inventory, ensure_ascii=False, indent=2) + "\n"
        write_or_check(path, expected, write, changed)

    for collection in ("environmentFiles", "topologies"):
        for relative in deployment[collection]:
            path = root / relative
            expected = replace_all(read_text(path), replacements)
            write_or_check(path, expected, write, changed)


def render_runtime_artifacts(root: Path, contract: dict[str, Any], write: bool, changed: list[str]) -> None:
    deployment = contract["deployment"]
    canonical_paths = set(deployment["canonicalRuntimeArtifacts"])
    expected_paths = {
        "deploy/runtimes/batch/config/cpf-batch-center-cut.properties",
        "deploy/runtimes/batch/systemd/cpf-batch-center-cut.service",
        "deploy/runtimes/batch/agent/install-batch-agent.sh",
        "deploy/runtimes/batch/agent/install-batch-agent.ps1",
    }
    if canonical_paths != expected_paths:
        raise ContractError(f"canonical BAT runtime artifact surface mismatch: {sorted(canonical_paths)}")
    legacy_paths = set(deployment.get("inactiveLegacyArtifacts", []))
    if canonical_paths.intersection(legacy_paths):
        raise ContractError("canonical BAT runtime artifact is also marked inactive legacy")
    if legacy_paths:
        raise ContractError(f"retired BAT runtime artifacts must be removed from the current contract: {sorted(legacy_paths)}")

    role_by_name = {item["name"]: item for item in contract["roles"]}
    center = role_by_name["CENTER_CUT"]
    services = [role["artifactName"] for role in contract["roles"]]
    shell_services = " ".join(services)
    powershell_services = ",".join(f"'{name}'" for name in services)
    artifacts = {
        "deploy/runtimes/batch/config/cpf-batch-center-cut.properties": (
            f"spring.application.name={center['artifactName']}\n"
            f"server.port={center['localPort']}\n"
            "cpf.batch.control.base-url=${CPF_BATCH_CONTROL_BASE_URL:http://127.0.0.1:8180}\n"

            "cpf.batch.runtime.heartbeat-ms=${CPF_RUNTIME_HEARTBEAT_MS:5000}\n"
            "logging.level.root=${CPF_LOG_LEVEL:INFO}\n"
        ),
        "deploy/runtimes/batch/systemd/cpf-batch-center-cut.service": (
            "[Unit]\n"
            f"Description=CPF {center['artifactName']}\n"
            "After=network-online.target\nWants=network-online.target\n"
            "[Service]\nType=simple\nUser=cpf\nGroup=cpf\n"
            f"EnvironmentFile=-/etc/cpf/{center['artifactName']}.env\n"
            f"ExecStart=/opt/cpf/bin/{center['artifactName']}.sh run\n"
            "Restart=on-failure\nRestartSec=5\nTimeoutStopSec=600\nKillSignal=SIGTERM\n"
            "NoNewPrivileges=true\nPrivateTmp=true\nProtectSystem=strict\nProtectHome=true\n"
            f"ReadWritePaths=/opt/cpf/{center['artifactName']} /var/log/cpf/{center['artifactName']}\n"
            "[Install]\nWantedBy=multi-user.target\n"
        ),
        "deploy/runtimes/batch/agent/install-batch-agent.sh": (
            "#!/usr/bin/env bash\nset -euo pipefail\n"
            "[[ \"${EUID}\" -eq 0 ]] || { echo 'root required' >&2; exit 10; }\n"
            "BASE=\"$(cd \"$(dirname \"$0\")/..\" && pwd)\"; CPF_USER=\"${CPF_USER:-cpf}\"\n"
            "id \"$CPF_USER\" >/dev/null 2>&1 || useradd --system --home /opt/cpf --shell /usr/sbin/nologin \"$CPF_USER\"\n"
            "install -d -m 0750 /etc/cpf /opt/cpf/bin\n"
            f"for service in {shell_services}; do\n"
            "  install -d -o \"$CPF_USER\" -g \"$CPF_USER\" \"/opt/cpf/$service/releases\" \"/opt/cpf/$service/config\" \"/opt/cpf/$service/work\" \"/var/log/cpf/$service\"\n"
            "  install -m 0755 \"$BASE/bin/$service.sh\" \"/opt/cpf/bin/$service.sh\"\n"
            "  install -m 0644 \"$BASE/systemd/$service.service\" \"/etc/systemd/system/$service.service\"\n"
            "  if [[ -f \"$BASE/config/$service.properties\" && ! -f \"/opt/cpf/$service/config/$service.properties\" ]]; then\n"
            "    install -o \"$CPF_USER\" -g \"$CPF_USER\" -m 0640 \"$BASE/config/$service.properties\" \"/opt/cpf/$service/config/$service.properties\"\n"
            "  fi\ndone\n"
            "install -m 0755 \"$BASE/bin/cpf-runtime.sh\" /opt/cpf/bin/cpf-runtime.sh\n"
            "systemctl daemon-reload\nsystemctl enable cpf-batch-agent.service\n"
            "printf '%s\\n' 'BAT managed-service layout installed. Place signed Batch Agent artifact, set current.version, provision /etc/cpf/*.env, then start Batch Agent.'\n"
        ),
        "deploy/runtimes/batch/agent/install-batch-agent.ps1": (
            "param([string]$Root='C:\\cpf')\n"
            "$ErrorActionPreference='Stop';$base=(Resolve-Path \"$PSScriptRoot\\..\").Path\n"
            f"$services=@({powershell_services})\n"
            "New-Item -ItemType Directory -Force -Path (Join-Path $Root 'bin')|Out-Null\n"
            "Copy-Item (Join-Path $base 'bin\\cpf-runtime.ps1') (Join-Path $Root 'bin\\cpf-runtime.ps1') -Force\n"
            "Copy-Item (Join-Path $base 'bin\\cpf-service-control.ps1') (Join-Path $Root 'bin\\cpf-service-control.ps1') -Force\n"
            "foreach($service in $services){\n"
            " $serviceRoot=Join-Path $Root $service;foreach($d in @('releases','config','work')){New-Item -ItemType Directory -Force -Path (Join-Path $serviceRoot $d)|Out-Null};New-Item -ItemType Directory -Force -Path (Join-Path $Root \"logs\\$service\")|Out-Null\n"
            " Copy-Item (Join-Path $base \"bin\\$service.ps1\") (Join-Path $Root \"bin\\$service.ps1\") -Force\n"
            " $template=Join-Path $base \"config\\$service.properties\";$target=Join-Path $serviceRoot \"config\\$service.properties\";if((Test-Path $template)-and-not(Test-Path $target)){Copy-Item $template $target}\n"
            "}\n"
            "Write-Host 'BAT managed-service Windows layout installed. Configure startup policy for Batch Agent using the approved enterprise service wrapper/task policy.'\n"
        ),
    }
    for relative, expected in artifacts.items():
        write_or_check(root / relative, expected, write, changed)



def sql_literals(contract: dict[str, Any], forward: bool) -> tuple[list[str], dict[str, str]]:
    roles = canonical_roles(contract)
    mapping: dict[str, str] = {}
    for role in contract["roles"]:
        aliases = list(role.get("legacyAliases", []))
        if aliases:
            if len(aliases) != 1:
                raise ContractError(f"migration requires one-to-one alias mapping: {role['name']}")
            if forward:
                mapping[aliases[0]] = role["name"]
            else:
                mapping[role["name"]] = aliases[0]
    allowed = roles + [alias for role in contract["roles"] for alias in role.get("legacyAliases", [])]
    if len(allowed) != len(set(allowed)) or len(mapping) != 3:
        raise ContractError("runtime role migration alias collision/incompleteness")
    return allowed, mapping


def case_expression(column: str, mapping: dict[str, str]) -> str:
    clauses = " ".join(f"WHEN '{old}' THEN '{new}'" for old, new in mapping.items())
    return f"CASE {column} {clauses} ELSE {column} END"


def constraint_expression(contract: dict[str, Any], target: dict[str, Any], vendor: str) -> str:
    column = str(target["column"])
    value = f"BINARY {column}" if vendor == "mariadb" else column
    allowed = ", ".join(f"'{role}'" for role in canonical_roles(contract))
    predicate = f"{value} IN ({allowed})"
    return f"{column} IS NULL OR {predicate}" if bool(target["nullable"]) else predicate


def validate_migration_targets(contract: dict[str, Any]) -> list[dict[str, Any]]:
    targets = list(contract["migration"]["targets"])
    if len(targets) != 3 or {item["logicalDatabase"] for item in targets} != {"cpfDB"}:
        raise ContractError("D-009 requires exactly three cpfDB runtime_role targets")
    keys = {(str(item["table"]).lower(), str(item["column"]).lower()) for item in targets}
    constraints = {str(item["constraint"]).lower() for item in targets}
    if len(keys) != 3 or len(constraints) != 3:
        raise ContractError("D-009 target or constraint collision")
    return targets


def role_value_union(targets: list[dict[str, Any]]) -> str:
    return " UNION ALL ".join(
        f"SELECT {target['column']} AS runtime_role FROM {target['table']}" for target in targets
    )


def render_updates(targets: list[dict[str, Any]], mapping: dict[str, str], vendor: str) -> list[str]:
    old_sql = ", ".join(f"'{item}'" for item in mapping)
    statements: list[str] = []
    for target in targets:
        column = str(target["column"])
        predicate_column = f"BINARY {column}" if vendor == "mariadb" else column
        statements.append(
            f"UPDATE {target['table']} SET {column} = {case_expression(column, mapping)} "
            f"WHERE {predicate_column} IN ({old_sql});"
        )
    return statements


def render_mariadb(contract: dict[str, Any], forward: bool) -> str:
    allowed, mapping = sql_literals(contract, forward)
    allowed_sql = ", ".join(f"'{item}'" for item in allowed)
    targets = validate_migration_targets(contract)
    union = role_value_union(targets)
    metadata_predicate = " OR ".join(
        f"(LOWER(TABLE_NAME) = LOWER('{target['table']}') AND CONSTRAINT_NAME = '{target['constraint']}')"
        for target in targets
    )
    constraint_count = (
        "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS "
        "WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_TYPE = 'CHECK' AND ("
        f"{metadata_predicate})"
    )
    constraint_guard = f"({constraint_count})" if forward else f"ABS({len(targets)} - ({constraint_count}))"
    direction = "forward" if forward else "rollback"
    lines = [
        "-- GENERATED from cpf-tools/runtime/metadata/bat-runtime-role-contract.json; DO NOT EDIT.",
        f"-- D-009 {direction}: phase 1 validates every target before any write.",
        "USE cpfDB;",
        "DROP TEMPORARY TABLE IF EXISTS cpf_bat_runtime_role_guard;",
        "CREATE TEMPORARY TABLE cpf_bat_runtime_role_guard (invalid_count BIGINT NOT NULL, CONSTRAINT ck_cpf_bat_runtime_role_guard CHECK (invalid_count = 0));",
        "INSERT INTO cpf_bat_runtime_role_guard (invalid_count) SELECT "
        f"(SELECT COUNT(*) FROM ({union}) role_values WHERE runtime_role IS NOT NULL "
        f"AND BINARY runtime_role NOT IN ({allowed_sql})) + {constraint_guard};",
        "DROP TEMPORARY TABLE cpf_bat_runtime_role_guard;",
        f"-- D-009 {direction}: phase 2 applies the data operation transaction and constraint lifecycle.",
    ]
    if not forward:
        for target in targets:
            lines.append(f"ALTER TABLE {target['table']} DROP CONSTRAINT {target['constraint']};")
    lines.append("START TRANSACTION;")
    lines.extend(render_updates(targets, mapping, "mariadb"))
    lines.append("COMMIT;")
    if forward:
        for target in targets:
            lines.append(
                f"ALTER TABLE {target['table']} ADD CONSTRAINT {target['constraint']} "
                f"CHECK ({constraint_expression(contract, target, 'mariadb')});"
            )
    return "\n".join(lines) + "\n"


def render_postgresql(contract: dict[str, Any], logical_db: str, forward: bool) -> str:
    if logical_db != "cpfDB":
        raise ContractError(f"D-009 PostgreSQL target must be cpfDB: {logical_db}")
    allowed, mapping = sql_literals(contract, forward)
    allowed_sql = ", ".join(f"'{item}'" for item in allowed)
    targets = validate_migration_targets(contract)
    union = role_value_union(targets)
    metadata_predicate = " OR ".join(
        f"(lower(table_name) = lower('{target['table']}') AND constraint_name = '{target['constraint']}')"
        for target in targets
    )
    constraint_count = (
        "SELECT COUNT(*) FROM information_schema.table_constraints "
        "WHERE table_schema = current_schema() AND constraint_type = 'CHECK' AND ("
        f"{metadata_predicate})"
    )
    constraint_guard = f"({constraint_count})" if forward else f"ABS({len(targets)} - ({constraint_count}))"
    direction = "forward" if forward else "rollback"
    lines = [
        "-- GENERATED from cpf-tools/runtime/metadata/bat-runtime-role-contract.json; DO NOT EDIT.",
        f"-- D-009 {direction}: one transaction, all-target preflight before writes.",
        "BEGIN;",
        "CREATE TEMPORARY TABLE cpf_bat_runtime_role_guard (invalid_count BIGINT NOT NULL CHECK (invalid_count = 0)) ON COMMIT DROP;",
        "INSERT INTO cpf_bat_runtime_role_guard (invalid_count) SELECT "
        f"(SELECT COUNT(*) FROM ({union}) role_values WHERE runtime_role IS NOT NULL "
        f"AND runtime_role NOT IN ({allowed_sql})) + {constraint_guard};",
    ]
    if not forward:
        for target in targets:
            lines.append(f"ALTER TABLE {target['table']} DROP CONSTRAINT {target['constraint']};")
    lines.extend(render_updates(targets, mapping, "postgresql"))
    if forward:
        for target in targets:
            lines.append(
                f"ALTER TABLE {target['table']} ADD CONSTRAINT {target['constraint']} "
                f"CHECK ({constraint_expression(contract, target, 'postgresql')});"
            )
    lines.append("COMMIT;")
    return "\n".join(lines) + "\n"


def render_oracle(contract: dict[str, Any], logical_db: str, forward: bool) -> str:
    if logical_db != "cpfDB":
        raise ContractError(f"D-009 Oracle target must be cpfDB: {logical_db}")
    allowed, mapping = sql_literals(contract, forward)
    allowed_sql = ", ".join(f"'{item}'" for item in allowed)
    targets = validate_migration_targets(contract)
    union = role_value_union(targets)
    metadata_predicate = " OR ".join(
        f"(table_name = UPPER('{target['table']}') AND constraint_name = UPPER('{target['constraint']}'))"
        for target in targets
    )
    constraint_count = f"SELECT COUNT(*) FROM user_constraints WHERE constraint_type = 'C' AND ({metadata_predicate})"
    constraint_guard = f"({constraint_count})" if forward else f"ABS({len(targets)} - ({constraint_count}))"
    direction = "forward" if forward else "rollback"
    lines = [
        "-- GENERATED from cpf-tools/runtime/metadata/bat-runtime-role-contract.json; DO NOT EDIT.",
        "WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK",
        f"-- D-009 {direction}: phase 1 validates every target before any write or DDL.",
        "DECLARE",
        "  v_invalid_count NUMBER;",
        "BEGIN",
        "  SELECT (SELECT COUNT(*) FROM ("
        f"{union}) role_values WHERE runtime_role IS NOT NULL AND runtime_role NOT IN ({allowed_sql})) + "
        f"{constraint_guard} INTO v_invalid_count FROM dual;",
        "  IF v_invalid_count <> 0 THEN",
        "    raise_application_error(-20009, 'D-009 runtime role preflight failed');",
        "  END IF;",
        "EXCEPTION WHEN OTHERS THEN",
        "  ROLLBACK;",
        "  RAISE;",
        "END;",
        "/",
        f"-- D-009 {direction}: phase 2 applies the data operation and constraint lifecycle.",
    ]
    if not forward:
        for target in targets:
            lines.append(f"ALTER TABLE {target['table']} DROP CONSTRAINT {target['constraint']};")
    lines.extend(["BEGIN"] + ["  " + statement for statement in render_updates(targets, mapping, "oracle")])
    lines.extend([
        "EXCEPTION WHEN OTHERS THEN",
        "  ROLLBACK;",
        "  RAISE;",
        "END;",
        "/",
    ])
    if forward:
        for target in targets:
            lines.append(
                f"ALTER TABLE {target['table']} ADD CONSTRAINT {target['constraint']} "
                f"CHECK ({constraint_expression(contract, target, 'oracle')});"
            )
    lines.append("COMMIT;")
    return "\n".join(lines) + "\n"


def migration_outputs(root: Path, contract: dict[str, Any]) -> dict[Path, str]:
    migration = contract["migration"]
    version = int(migration["allocatedVersion"])
    observed = int(migration["observedRepositoryMaxVersionAtAllocation"])
    if version <= observed:
        raise ContractError(f"migration version lock invalid: observed={observed} allocated={version}")
    name = "batch_runtime_role_currentization"
    outputs: dict[Path, str] = {
        root / f"cpf-tools/db/vendor/mariadb/migration/flyway/V{version}__{name}.sql": render_mariadb(contract, True),
        root / f"cpf-tools/db/vendor/mariadb/rollback/R{version}__{name}.sql": render_mariadb(contract, False),
        root / f"cpf-tools/db/vendor/mariadb/source/migration/flyway/V{version}__{name}.sql": render_mariadb(contract, True),
        root / f"cpf-tools/db/vendor/mariadb/source/migration/rollback/R{version}__{name}.sql": render_mariadb(contract, False),
    }
    for vendor, renderer in (("postgresql", render_postgresql), ("oracle", render_oracle)):
        logical_db = "cpfDB"
        outputs[root / f"cpf-tools/db/vendor/{vendor}/migration/flyway/{logical_db}/V{version}__{name}.sql"] = renderer(contract, logical_db, True)
        outputs[root / f"cpf-tools/db/vendor/{vendor}/rollback/{logical_db}/R{version}__{name}.sql"] = renderer(contract, logical_db, False)
    return outputs


def render_migrations(root: Path, contract: dict[str, Any], write: bool, changed: list[str]) -> None:
    outputs = migration_outputs(root, contract)
    version = int(contract["migration"]["allocatedVersion"])
    for vendor in ("mariadb", "postgresql", "oracle"):
        migration_root = root / f"cpf-tools/db/vendor/{vendor}/migration"
        collisions = [path for path in migration_root.rglob(f"V{version}__*.sql") if path not in outputs]
        if collisions:
            raise ContractError(f"migration V{version} collision: {[p.as_posix() for p in collisions]}")
    for path, expected in outputs.items():
        write_or_check(path, expected, write, changed)


def verify_migration_safety(root: Path, contract: dict[str, Any]) -> None:
    outputs = migration_outputs(root, contract)
    if len(outputs) != 8:
        raise ContractError(f"D-009 DB3/source migration output count mismatch: {len(outputs)}")
    targets = validate_migration_targets(contract)
    for path, sql in outputs.items():
        if sql.index("NOT IN") > sql.index("UPDATE "):
            raise ContractError(f"D-009 preflight occurs after write: {path.as_posix()}")
        forward = path.name.startswith("V")
        for target in targets:
            operation = f"ADD CONSTRAINT {target['constraint']}" if forward else f"DROP CONSTRAINT {target['constraint']}"
            if sql.count(operation) != 1:
                raise ContractError(f"D-009 constraint lifecycle mismatch: {path.as_posix()}: {operation}")
        vendor = next(name for name in contract["runtimeSql"]["vendors"] if name in path.parts)
        if vendor == "mariadb":
            if "START TRANSACTION;" not in sql or "COMMIT;" not in sql or "BINARY runtime_role" not in sql:
                raise ContractError(f"MariaDB D-009 transaction/case policy missing: {path.as_posix()}")
        elif vendor == "postgresql":
            if "BEGIN;" not in sql or "COMMIT;" not in sql:
                raise ContractError(f"PostgreSQL D-009 transaction missing: {path.as_posix()}")
        elif "WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK" not in sql or "ROLLBACK;" not in sql:
            raise ContractError(f"Oracle D-009 rollback policy missing: {path.as_posix()}")
    maria = root / "cpf-tools/db/vendor/mariadb"
    if read_text(maria / "migration/flyway/V116__batch_runtime_role_currentization.sql") != read_text(
        maria / "source/migration/flyway/V116__batch_runtime_role_currentization.sql"
    ):
        raise ContractError("MariaDB V116 runtime/source mirror drift")
    if read_text(maria / "rollback/R116__batch_runtime_role_currentization.sql") != read_text(
        maria / "source/migration/rollback/R116__batch_runtime_role_currentization.sql"
    ):
        raise ContractError("MariaDB R116 runtime/source mirror drift")


def verify_vendor_schema_projection(root: Path, contract: dict[str, Any]) -> None:
    targets = validate_migration_targets(contract)
    vendors = list(contract["runtimeSql"]["vendors"])
    if vendors != ["mariadb", "postgresql", "oracle"]:
        raise ContractError(f"official DB3 order drift: {vendors}")
    for vendor in vendors:
        projections = [
            root / f"cpf-tools/db/vendor/{vendor}/source/10_cpf_schema.sql",
            root / f"cpf-tools/db/vendor/{vendor}/install/00_empty_install.sql",
            root / f"cpf-tools/db/generated/current/{vendor}/cpf-platform-schema.sql",
        ]
        for path in projections:
            text = read_text(path)
            compact_text = re.sub(r"\s+", "", text)
            for target in targets:
                marker = f"CONSTRAINT {target['constraint']} CHECK ({constraint_expression(contract, target, vendor)})"
                if compact_text.count(re.sub(r"\s+", "", marker)) != 1:
                    raise ContractError(f"canonical CHECK projection drift: vendor={vendor} file={path.as_posix()} constraint={target['constraint']}")


def verify_lifecycle_scenario(root: Path) -> None:
    scenarios = json.loads(read_text(root / "cpf-tools/db/canonical/db3-lifecycle-scenarios.json"))["scenarios"]
    matches = [item for item in scenarios if item.get("id") == "CPF-DB3-D009-BAT-RUNTIME-ROLE-001"]
    required = [
        "mixed-canonical-and-retired-role-preflight",
        "unknown-role-preflight-no-write",
        "constraint-name-collision-preflight-no-write",
        "v116-upgrade",
        "canonical-role-check-constraint",
        "r116-rollback",
        "v116-reapply",
    ]
    if len(matches) != 1 or any(step not in matches[0].get("steps", []) for step in required):
        raise ContractError("D-009 DB3 lifecycle scenario coverage drift")
    catalog = json.loads(read_text(root / "cpf-tools/db/canonical/migration-intent-catalog.json"))
    intents = [item for item in catalog.get("currentIntents", []) if item.get("id") == "D-009-BAT-RUNTIME-ROLE-CURRENTIZATION"]
    if len(intents) != 1 or intents[0].get("lifecycleScenarioId") != matches[0]["id"]:
        raise ContractError("D-009 migration intent/lifecycle scenario parity drift")


def verify_no_active_aliases(root: Path, contract: dict[str, Any]) -> None:
    aliases = list(replacement_map(contract))
    targets = list(contract["runtimeSql"]["authoringFiles"])
    targets += list(contract["deployment"]["inventories"])
    targets += list(contract["deployment"]["environmentFiles"])
    targets += list(contract["deployment"]["topologies"])
    targets += list(contract["deployment"]["canonicalRuntimeArtifacts"])
    targets.append(contract["deployment"]["schema"])
    inactive = set(contract["deployment"]["inactiveLegacyArtifacts"])
    for path in (root / "deploy").rglob("*"):
        if path.is_file():
            relative = path.relative_to(root).as_posix()
            if relative not in inactive and relative not in targets:
                targets.append(relative)
    for relative in targets:
        text = read_text(root / relative)
        present = [alias for alias in aliases if alias in text]
        if present:
            raise ContractError(f"retired BAT role remains active: {relative}: {present}")
    if inactive:
        raise ContractError(f"retired BAT runtime artifacts remain in the active contract: {sorted(inactive)}")


def verify_checksum_entries(root: Path, contract: dict[str, Any]) -> None:
    version = int(contract["migration"]["allocatedVersion"])
    migration_files = [path for path in migration_outputs(root, contract) if path.name.startswith(f"V{version}__")]
    for path in migration_files:
        manifest = path.parent / "checksums.sha256"
        lines = read_text(manifest).splitlines()
        matches = [line for line in lines if re.search(rf"(?:\*|\s)V{version}__", line)]
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        if len(matches) != 1 or digest not in matches[0].lower() or path.name not in matches[0]:
            raise ContractError(f"migration checksum lock mismatch: {path.as_posix()}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--write", action="store_true")
    parser.add_argument("--skip-checksums", action="store_true", help="only for the first render before checksum manifests are rebuilt")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    contract_path = root / "cpf-tools/runtime/metadata/bat-runtime-role-contract.json"
    contract = json.loads(read_text(contract_path))
    changed: list[str] = []
    try:
        canonical_roles(contract)
        replacement_map(contract)
        verify_java_enum(root, contract)
        verify_migration_version_lock(root, contract)
        verify_owned_surfaces(root, contract)
        render_runtime_sql(root, contract, args.write, changed)
        render_deploy(root, contract, args.write, changed)
        render_runtime_artifacts(root, contract, args.write, changed)
        render_migrations(root, contract, args.write, changed)
        verify_migration_safety(root, contract)
        verify_vendor_schema_projection(root, contract)
        verify_lifecycle_scenario(root)
        verify_no_active_aliases(root, contract)
        if not args.skip_checksums:
            verify_checksum_entries(root, contract)
    except (ContractError, KeyError, TypeError, ValueError, json.JSONDecodeError) as exc:
        print(json.dumps({"status": "FAIL", "error": str(exc)}, ensure_ascii=False, indent=2))
        return 1
    print(json.dumps({
        "status": "PASS",
        "mode": "WRITE" if args.write else "CHECK",
        "canonicalRoles": canonical_roles(contract),
        "vendors": contract["runtimeSql"]["vendors"],
        "migrationVersion": contract["migration"]["allocatedVersion"],
        "changed": changed,
    }, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    sys.exit(main())

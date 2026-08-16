#!/usr/bin/env python3
"""Canonical Generated Customer Domain inventory.

Generated Project 내부의 영구 manifest/ownership metadata를 읽지 않는다.
Framework가 관리하는 cpf-domain.yaml과 canonical generator validation을 사용한다.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", required=True)
    selector = parser.add_mutually_exclusive_group()
    selector.add_argument("--domain")
    selector.add_argument("--file")
    parser.add_argument("--include-missing", action="store_true")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    engine = root / "cpf-tools/generator/engine"
    sys.path.insert(0, str(engine))
    from cpf_domain_generator import (  # type: ignore
        GENERATOR_VERSION,
        load_yaml_subset,
        validate_definition,
    )

    settings = (root / "settings.gradle").read_text(encoding="utf-8") if (root / "settings.gradle").is_file() else ""
    definitions = root / "cpf-tools/generator/definitions"
    result = []
    seen_system: set[str] = set()
    seen_package: set[str] = set()
    seen_ports: set[int] = set()
    if args.file:
        selected = [Path(args.file).resolve()]
    else:
        selected = sorted(definitions.glob("*/cpf-domain.yaml"))
        if args.domain:
            normalized = args.domain.strip().lower()
            selected = [path for path in selected if path.parent.name.lower() == normalized]
            if not selected:
                raise SystemExit(f"Generated Domain canonical definition이 없습니다: {normalized}")

    for definition in selected:
        if not definition.is_file():
            raise SystemExit(f"Generated Domain definition이 없습니다: {definition}")
        d = validate_definition(load_yaml_subset(definition))
        project_name = f"cpf-{d.name}"
        project_dir = root / project_name
        exists = project_dir.is_dir()
        if not exists and not args.include_missing:
            continue
        for label, value, seen in (
            ("systemCode", d.system_code, seen_system),
            ("packageName", d.package_name, seen_package),
        ):
            if value in seen:
                raise SystemExit(f"Generated Domain {label} 중복: {value}")
            seen.add(value)
        for port in (d.local_online_port,):
            if port and port in seen_ports:
                raise SystemExit(f"Generated Domain local port 중복: {port}")
            if port:
                seen_ports.add(port)
        forbidden = []
        if exists:
            for rel in (
                ".cpf",
                "cpf-domain.yaml",
                "generator.lock",
                "manifest/domain-manifest.json",
                "manifest/generator-ownership.json",
            ):
                if (project_dir / rel).exists():
                    forbidden.append(rel)
        try:
            definition_path = definition.relative_to(root).as_posix()
        except ValueError:
            definition_path = str(definition)
        result.append({
            "projectName": project_name,
            "projectPath": project_name,
            "definitionPath": definition_path,
            "definitionSha256": hashlib.sha256(definition.read_bytes()).hexdigest(),
            "generatorVersion": GENERATOR_VERSION,
            "domainName": d.name,
            "moduleName": d.module_name,
            "className": d.class_name,
            "systemCode": d.system_code,
            "packageName": d.package_name,
            "tablePrefix": d.table_prefix,
            "databaseRole": d.database_role,
            "databaseEnabled": d.persistence != "none" or d.sample_transaction,
            "persistence": d.persistence,
            "onlineEnabled": d.online,
            "batchCapabilitySelection": "PROJECT_SETUP",
            "sampleTransaction": d.sample_transaction,
            "httpClient": d.http_client,
            "resilience": d.resilience,
            "cache": d.cache,
            "messaging": d.messaging,
            "objectStorage": d.object_storage,
            "securityProfile": d.security_profile,
            "domainDependencies": [
                {"domainName": dependency.name, "systemCode": dependency.system_code}
                for dependency in d.domain_dependencies
            ],
            "externalClients": [
                {"name": client.name, "id": client.client_id, "capability": client.capability}
                for client in d.external_clients
            ],
            "localOnlinePort": d.local_online_port,
            "dependencyModel": "root-generated-regression",
            "exists": exists,
            "settingsIncluded": bool(re.search(
                rf"(?m)^\s*include\s+['\"]{re.escape(project_name)}['\"]\s*$",
                settings,
            )),
            "generatedProjectMetadata": "NONE",
            "forbiddenPermanentMetadata": forbidden,
        })
    print(json.dumps({"schemaVersion": 1, "domains": result}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

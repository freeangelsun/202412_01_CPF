#!/usr/bin/env python3
"""Canonical Generated Customer Domain inventory.

Generated Project 내부의 영구 manifest/ownership metadata를 읽지 않는다.
Developer-Facing gradle.properties 계약과 canonical Generator validation을 사용한다.
"""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass

import argparse
import hashlib
import json
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
        load_domain_contract,
    )

    result = []
    seen_system: set[str] = set()
    seen_package: set[str] = set()
    seen_ports: set[int] = set()
    if args.file:
        selected = [Path(args.file).resolve()]
    else:
        selected = sorted(path for path in root.glob("cpf-*/gradle.properties")
                          if "cpf.domain.contractVersion=" in path.read_text(encoding="utf-8-sig"))
        if args.domain:
            normalized = args.domain.strip().lower()
            normalized = normalized if normalized.startswith("cpf-") else f"cpf-{normalized}"
            selected = [path for path in selected if path.parent.name.lower() == normalized]
            if not selected:
                raise SystemExit(f"Generated Domain Developer 계약이 없습니다: {normalized}")

    for contract in selected:
        if not contract.is_file():
            raise SystemExit(f"Generated Domain 계약이 없습니다: {contract}")
        d = load_domain_contract(contract)
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
                "cpf-generator.lock.json",
                "manifest/domain-manifest.json",
                "manifest/generator-ownership.json",
                "online/src/main/resources/META-INF/cpf/generated-domain.properties",
                "batch/src/main/resources/META-INF/cpf/generated-domain.properties",
            ):
                if (project_dir / rel).exists():
                    forbidden.append(rel)
        try:
            contract_path = contract.relative_to(root).as_posix()
        except ValueError:
            contract_path = str(contract)
        result.append({
            "projectName": project_name,
            "projectPath": project_name,
            "contractPath": contract_path,
            "contractSha256": hashlib.sha256(contract.read_bytes()).hexdigest(),
            "generatorVersion": GENERATOR_VERSION,
            "domainName": d.name,
            "moduleName": d.module_name,
            "className": d.class_name,
            "generationMode": d.generation_mode,
            "systemCode": d.system_code,
            "packageName": d.package_name,
            "tablePrefix": d.table_prefix,
            "databaseRole": d.database_role,
            "databaseEnabled": d.persistence != "none" or d.sample_transaction,
            "persistence": d.persistence,
            "onlineEnabled": d.online,
            "batchEnabled": d.batch,
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
            "settingsIncluded": exists and (project_dir / "settings.gradle").is_file(),
            "generatedProjectMetadata": "ABSENT",
            "forbiddenPermanentMetadata": forbidden,
        })
    print(json.dumps({"schemaVersion": 1, "domains": result}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

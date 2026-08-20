#!/usr/bin/env python3
"""Verify the current Backoffice architecture and BFF/domain operation boundary."""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path


class ContractError(RuntimeError):
    pass


def read(path: Path) -> str:
    if not path.is_file():
        raise ContractError(f"missing {path}")
    return path.read_text(encoding="utf-8-sig")


def operation_ids(spec: Path) -> set[str]:
    doc = json.loads(read(spec))
    result: set[str] = set()
    for item in doc.get("paths", {}).values():
        if not isinstance(item, dict):
            continue
        for method, op in item.items():
            if method.lower() in {"get", "post", "put", "patch", "delete"} and isinstance(op, dict):
                operation_id = op.get("operationId")
                if operation_id:
                    result.add(str(operation_id))
    return result


def validate(root: Path) -> dict[str, int | str]:
    domain = root / "cpf-backoffice"
    web = root / "cpf-backoffice-web"
    if not domain.is_dir() and not web.is_dir():
        return {"state": "ABSENT", "routes": 0, "referenceRoutes": 0, "operations": 0}
    if not domain.is_dir() or not web.is_dir():
        raise ContractError("cpf-backoffice and cpf-backoffice-web must be present or absent together")

    for retired in ("cpf-biz-admin", "cpf-biz-channel", "cpf-biz-frontend"):
        if (root / retired).exists():
            raise ContractError(f"retired Backoffice product root remains: {retired}")

    spec = domain / "openapi/cpf-openapi.json"
    domain_operations = operation_ids(spec)
    if not domain_operations:
        raise ContractError("cpf-backoffice OpenAPI has no canonical operationId")

    online_build = read(domain / "online/build.gradle").lower()
    forbidden_domain = ("bff", "cpf-starter-bff", "cpf-biz-", "internal:file:attachment")
    leaked_domain = [token for token in forbidden_domain if token in online_build]
    if leaked_domain:
        raise ContractError(f"Backoffice Domain still has channel/internal dependency: {leaked_domain}")

    domain_java = list((domain / "online/src/main/java").rglob("*.java"))
    if not domain_java:
        raise ContractError("cpf-backoffice/online has no Java source")
    transaction_ids: set[str] = set()
    openapi_ids: set[str] = set()
    for source_path in domain_java:
        source = read(source_path)
        transaction_ids.update(re.findall(r'@CpfOnlineTransaction\s*\([^)]*?operationId\s*=\s*"([^"]+)"', source, re.S))
        openapi_ids.update(re.findall(r'@Operation\s*\([^)]*?operationId\s*=\s*"([^"]+)"', source, re.S))
    missing_annotation = sorted(domain_operations - transaction_ids)
    missing_openapi_annotation = sorted(domain_operations - openapi_ids)
    if missing_annotation:
        raise ContractError(f"Backoffice OpenAPI operations missing @CpfOnlineTransaction: {missing_annotation[:8]}")
    if missing_openapi_annotation:
        raise ContractError(f"Backoffice OpenAPI operations missing @Operation operationId: {missing_openapi_annotation[:8]}")

    web_build = read(web / "build.gradle").lower()
    forbidden_web_build = (
        "project(", "com.cpf:", "cpf-starter", "jdbc", "jpa", "mybatis", "mariadb", "postgresql", "oracle",
        "flyway", "liquibase", "datasource",
    )
    leaked = [token for token in forbidden_web_build if token in web_build]
    if leaked:
        raise ContractError(f"Backoffice Web must remain DB-less/Pure Spring: {leaked}")

    web_java = list((web / "src/main/java").rglob("*.java"))
    framework_imports: list[str] = []
    for path in web_java:
        for line in read(path).splitlines():
            if line.startswith("import com.cpf.") and not line.startswith("import com.cpf.backoffice.web."):
                framework_imports.append(f"{path.relative_to(root)}:{line}")
    if framework_imports:
        raise ContractError(f"Backoffice Web CPF Java dependency leak: {framework_imports[:5]}")

    web_source = "\n".join(read(path) for path in web_java)
    for header in (
        "X-Transaction-Id", "X-Original-System-Code", "X-System-Code", "X-Caller-System-Code",
        "X-Target-System-Code", "X-Target-Operation-Id",
    ):
        if header not in web_source:
            raise ContractError(f"Backoffice Web canonical six-header contract missing {header}")
    client_source = read(web / "src/main/java/com/cpf/backoffice/web/shared/client/BusinessApiHttpClient.java")
    if "properties.selectedBaseUri()" not in client_source:
        raise ContractError("Backoffice Web client must use one explicitly selected upstream")
    if "CanonicalTransactionHeaders.SYSTEM_CODE" not in client_source:
        raise ContractError("Backoffice Web must serialize canonical X-System-Code")
    if "properties.targetSystemCode()" not in client_source or "properties.callerSystemCode()" not in client_source:
        raise ContractError("Backoffice Web must derive caller/target identity from trusted server-side configuration")

    catalog = web / "src/main/resources/backoffice-routes.tsv"
    rows = [line.split("\t") for line in read(catalog).splitlines() if line and not line.startswith("#")]
    if any(len(row) != 3 for row in rows):
        raise ContractError("invalid Backoffice route catalog")
    if len({(row[0], row[1]) for row in rows}) != len(rows):
        raise ContractError("duplicate Backoffice Web route")
    route_operations = {row[2] for row in rows}
    if route_operations != domain_operations:
        missing = sorted(domain_operations - route_operations)
        extra = sorted(route_operations - domain_operations)
        raise ContractError(f"Backoffice Web/OpenAPI operation drift missing={missing[:8]} extra={extra[:8]}")

    frontend = web / "frontend"
    routes = read(frontend / "src/router/index.ts")
    reference_routes = len(re.findall(r"\bpath\s*:\s*['\"]", routes))
    if reference_routes < 4:
        raise ContractError(f"Backoffice reference frontend must expose representative routes, actual={reference_routes}")
    generator = read(frontend / "scripts/generate-reference-client.mjs")
    if "cpf-openapi.json" not in generator or "OpenAPI operations missing" not in generator:
        raise ContractError("Backoffice frontend is not OpenAPI-generated-client driven")
    frontend_source = "\n".join(
        read(path) for path in (frontend / "src").rglob("*") if path.is_file() and path.suffix in {".ts", ".vue"}
    )
    if "generated/backoffice-api" not in frontend_source:
        raise ContractError("Backoffice frontend has no generated BFF API consumer")
    if "/api/bza" in frontend_source or "cpf-biz-" in frontend_source:
        raise ContractError("Backoffice frontend still uses retired BZA/biz route naming")

    return {
        "state": "PRESENT",
        "routes": len(rows),
        "referenceRoutes": reference_routes,
        "operations": len(domain_operations),
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    args = parser.parse_args()
    result = validate(args.root.resolve())
    print(
        "BACKOFFICE_BOUNDARY_CONTRACT=PASS "
        f"state={result['state']} backendOperations={result['operations']} "
        f"webRoutes={result['routes']} referenceRoutes={result['referenceRoutes']} "
        "dbLess=1 cpfJavaDependency=0 canonicalHeaders=6"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except ContractError as exc:
        print(f"BACKOFFICE_BOUNDARY_CONTRACT=FAIL {exc}", file=sys.stderr)
        raise SystemExit(1)

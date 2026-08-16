#!/usr/bin/env python3
"""Validate CPF OpenAPI supported-profile, generator capability and migration lifecycle."""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import defaultdict
from pathlib import Path, PurePosixPath
from typing import Iterable

SHA = re.compile(r"^[0-9a-f]{40}$")
VERSION = re.compile(r"^3\.(?:1|2)\.\d+$")
HTML_RISK = re.compile(r"<\s*(?:script|iframe|object|embed|link|meta)\b|javascript\s*:|data\s*:\s*text/html|\bon[a-z]+\s*=", re.I)
MARKDOWN_FIELDS = {"description", "summary", "termsOfService"}
ALLOWED_STATUSES = {"SUPPORTED", "MIGRATION_PREVIEW", "RETIRED"}
ALLOWED_GENERATOR_STATUS = {"SUPPORTED", "UNSUPPORTED", "MIGRATION_PREVIEW"}


class OpenApiProfileError(RuntimeError):
    pass


def _safe(raw: str) -> PurePosixPath:
    value = raw.replace("\\", "/")
    path = PurePosixPath(value)
    if path.is_absolute() or not path.parts or any(part in {"", ".", ".."} for part in path.parts) or ":" in path.parts[0]:
        raise OpenApiProfileError(f"unsafe repository path: {raw!r}")
    return path


def _load(path: Path) -> object:
    try:
        return json.loads(path.read_text(encoding="utf-8-sig"))
    except (OSError, json.JSONDecodeError) as exc:
        raise OpenApiProfileError(f"cannot read {path}: {exc}") from exc


def _strings(value: object, field: str, *, allow_empty: bool = False) -> list[str]:
    if not isinstance(value, list) or (not value and not allow_empty):
        raise OpenApiProfileError(f"{field} must be {'a ' if allow_empty else 'a non-empty '}string array")
    if any(not isinstance(item, str) or not item.strip() for item in value):
        raise OpenApiProfileError(f"{field} must contain non-empty strings")
    return [item.strip() for item in value]


def _path_exists(repo_root: Path, raw: str, inventory: dict[str, dict]) -> bool:
    path = _safe(raw).as_posix()
    if (repo_root / path).exists():
        return True
    item = inventory.get(path)
    return bool(item and item.get("type") in {"blob", "tree"} and SHA.fullmatch(str(item.get("sha", ""))))


def _walk(value: object, path: str = ""):
    yield path, value
    if isinstance(value, dict):
        for key, child in value.items():
            yield from _walk(child, f"{path}/{key}")
    elif isinstance(value, list):
        for index, child in enumerate(value):
            yield from _walk(child, f"{path}/{index}")


def _validate_sanitization(document: dict, source: str) -> int:
    checked = 0
    for pointer, value in _walk(document):
        field = pointer.rsplit("/", 1)[-1] if pointer else ""
        if field in MARKDOWN_FIELDS and isinstance(value, str):
            checked += 1
            if HTML_RISK.search(value):
                raise OpenApiProfileError(f"{source}: unsafe Markdown/HTML at {pointer}")
    return checked


def _schema_graph(document: dict, source: str) -> tuple[dict[str, set[str]], list[str]]:
    schemas = ((document.get("components") or {}).get("schemas") or {})
    if not isinstance(schemas, dict):
        raise OpenApiProfileError(f"{source}: components.schemas must be an object")
    graph: dict[str, set[str]] = defaultdict(set)
    external: list[str] = []
    for name, schema in schemas.items():
        if not isinstance(name, str) or not name:
            raise OpenApiProfileError(f"{source}: schema name must be non-empty")
        for pointer, value in _walk(schema):
            if not isinstance(value, dict) or "$ref" not in value:
                continue
            ref = value.get("$ref")
            if not isinstance(ref, str) or not ref:
                raise OpenApiProfileError(f"{source}: invalid $ref at schema {name}{pointer}")
            prefix = "#/components/schemas/"
            if ref.startswith(prefix):
                target = ref[len(prefix):]
                if target not in schemas:
                    raise OpenApiProfileError(f"{source}: unresolved schema $ref {ref}")
                graph[name].add(target)
            else:
                external.append(ref)
    return graph, external


def _cycles(graph: dict[str, set[str]]) -> list[list[str]]:
    visited: set[str] = set()
    active: set[str] = set()
    stack: list[str] = []
    found: list[list[str]] = []
    signatures: set[tuple[str, ...]] = set()

    def dfs(node: str):
        visited.add(node); active.add(node); stack.append(node)
        for target in sorted(graph.get(node, ())):
            if target not in visited:
                dfs(target)
            elif target in active:
                index = stack.index(target)
                cycle = stack[index:] + [target]
                signature = tuple(sorted(set(cycle)))
                if signature not in signatures:
                    signatures.add(signature); found.append(cycle)
        stack.pop(); active.remove(node)

    for node in sorted(graph):
        if node not in visited:
            dfs(node)
    return found


def validate_document(document: object, profile: dict, source: str) -> dict:
    if not isinstance(document, dict):
        raise OpenApiProfileError(f"{source}: document must be an object")
    version = str(document.get("openapi", ""))
    if not VERSION.fullmatch(version):
        raise OpenApiProfileError(f"{source}: invalid OpenAPI version {version!r}")
    status_by_version = {str(v.get("version")): str(v.get("status")) for v in profile["versions"]}
    family = "3.1.*" if version.startswith("3.1.") else "3.2.*"
    status = status_by_version.get(family)
    if status != "SUPPORTED":
        raise OpenApiProfileError(f"{source}: OpenAPI {version} is not supported for release; profile={family} status={status}")
    if not isinstance(document.get("info"), dict) or not str(document["info"].get("title", "")).strip() or not str(document["info"].get("version", "")).strip():
        raise OpenApiProfileError(f"{source}: info.title and info.version are required")
    if not any(key in document for key in ("paths", "components", "webhooks")):
        raise OpenApiProfileError(f"{source}: at least one of paths/components/webhooks is required")
    markdown_count = _validate_sanitization(document, source)
    graph, external = _schema_graph(document, source)
    if external and not profile["reference_policy"]["allow_external_refs"]:
        raise OpenApiProfileError(f"{source}: external $ref is forbidden: {sorted(set(external))[:10]}")
    cycles = _cycles(graph)
    if cycles and profile["reference_policy"]["cycle_policy"] == "REJECT":
        rendered = [" -> ".join(cycle) for cycle in cycles[:10]]
        raise OpenApiProfileError(f"{source}: schema reference cycles rejected: {rendered}")
    operation_ids: list[str] = []
    for _, item in (document.get("paths") or {}).items():
        if not isinstance(item, dict):
            continue
        for method, operation in item.items():
            if method.lower() not in {"get", "post", "put", "patch", "delete", "head", "options", "trace"} or not isinstance(operation, dict):
                continue
            operation_id = str(operation.get("operationId", "")).strip()
            if not operation_id:
                raise OpenApiProfileError(f"{source}: public operationId is required")
            operation_ids.append(operation_id)
    duplicates = sorted({item for item in operation_ids if operation_ids.count(item) > 1})
    if duplicates:
        raise OpenApiProfileError(f"{source}: duplicate operationId={duplicates[:20]}")
    return {
        "openapi_version": version,
        "profile_family": family,
        "operation_count": len(operation_ids),
        "schema_count": len(((document.get("components") or {}).get("schemas") or {})),
        "markdown_field_count": markdown_count,
        "schema_cycle_count": len(cycles),
        "external_ref_count": len(external),
    }


def verify(repo_root: Path, profile_path: Path) -> dict:
    profile = _load(profile_path)
    if not isinstance(profile, dict) or profile.get("schema_version") != "1.0":
        raise OpenApiProfileError("profile schema_version must be 1.0")
    if not SHA.fullmatch(str(profile.get("source_commit_sha", ""))):
        raise OpenApiProfileError("profile source_commit_sha must be a 40-character SHA")
    versions = profile.get("versions")
    if not isinstance(versions, list) or not versions:
        raise OpenApiProfileError("versions must be a non-empty array")
    seen_versions: set[str] = set()
    for index, item in enumerate(versions):
        if not isinstance(item, dict):
            raise OpenApiProfileError(f"versions[{index}] must be an object")
        version = str(item.get("version", ""))
        status = str(item.get("status", ""))
        if version not in {"3.1.*", "3.2.*"} or version in seen_versions:
            raise OpenApiProfileError(f"invalid/duplicate profile version {version!r}")
        seen_versions.add(version)
        if status not in ALLOWED_STATUSES:
            raise OpenApiProfileError(f"versions[{index}].status is invalid")
        if status == "SUPPORTED" and not str(item.get("canonical_patch", "")).startswith(version[:3]):
            raise OpenApiProfileError(f"versions[{index}] canonical_patch must match family")
    if {"3.1.*", "3.2.*"} - seen_versions:
        raise OpenApiProfileError("both 3.1.* and 3.2.* decisions are required")
    reference_policy = profile.get("reference_policy")
    if not isinstance(reference_policy, dict) or reference_policy.get("cycle_policy") not in {"REJECT", "ALLOW_EXPLICIT"} or not isinstance(reference_policy.get("allow_external_refs"), bool):
        raise OpenApiProfileError("reference_policy is invalid")
    sanitization = profile.get("sanitization")
    if not isinstance(sanitization, dict) or sanitization.get("mode") != "FAIL_CLOSED" or not sanitization.get("reject_active_content"):
        raise OpenApiProfileError("sanitization must be fail-closed with active-content rejection")
    generator = profile.get("generator_capabilities")
    if not isinstance(generator, list) or not generator:
        raise OpenApiProfileError("generator_capabilities must be non-empty")
    supported_runtime = {item["version"] for item in versions if item["status"] == "SUPPORTED"}
    for index, capability in enumerate(generator):
        if not isinstance(capability, dict):
            raise OpenApiProfileError(f"generator_capabilities[{index}] must be an object")
        if not str(capability.get("generator", "")).strip() or str(capability.get("status", "")) not in ALLOWED_GENERATOR_STATUS:
            raise OpenApiProfileError(f"generator_capabilities[{index}] invalid")
        families = _strings(capability.get("openapi_profiles"), f"generator_capabilities[{index}].openapi_profiles")
        if capability["status"] == "SUPPORTED" and not set(families) <= supported_runtime:
            raise OpenApiProfileError(f"generator {capability['generator']} claims unsupported OpenAPI profile")
        evidence = _strings(capability.get("evidence_paths"), f"generator_capabilities[{index}].evidence_paths")
        for path in evidence:
            if not _path_exists(repo_root, path, {}):
                raise OpenApiProfileError(f"generator evidence missing: {path}")
    migration = profile.get("migration")
    if not isinstance(migration, dict) or migration.get("from") != "3.1.*" or migration.get("to") != "3.2.*" or migration.get("status") not in {"PLANNED", "READY", "COMPLETE"}:
        raise OpenApiProfileError("3.1.* -> 3.2.* migration lifecycle is required")
    if profile["versions"][1]["status"] == "MIGRATION_PREVIEW" and migration["status"] == "COMPLETE":
        raise OpenApiProfileError("migration cannot be COMPLETE while 3.2.* is preview")
    migration_evidence = _strings(migration.get("evidence_paths"), "migration.evidence_paths")
    for path in migration_evidence:
        if not _path_exists(repo_root, path, {}):
            raise OpenApiProfileError(f"migration evidence missing: {path}")
    documents = profile.get("documents")
    if not isinstance(documents, list) or not documents:
        raise OpenApiProfileError("documents must be a non-empty array")
    document_paths: set[str] = set()
    results: list[dict] = []
    for index, item in enumerate(documents):
        if not isinstance(item, dict):
            raise OpenApiProfileError(f"documents[{index}] must be an object")
        path = _safe(str(item.get("path", ""))).as_posix()
        if path in document_paths:
            raise OpenApiProfileError(f"duplicate document path: {path}")
        document_paths.add(path)
        if not str(item.get("owner", "")).strip():
            raise OpenApiProfileError(f"documents[{index}].owner is required")
        consumers = _strings(item.get("consumers"), f"documents[{index}].consumers")
        missing = [candidate for candidate in consumers if not _path_exists(repo_root, candidate, {})]
        if missing:
            raise OpenApiProfileError(f"documents[{index}] consumer paths missing: {missing}")
        result = validate_document(_load(repo_root / path), profile, path)
        results.append({"path": path, "owner": item["owner"], "consumer_count": len(consumers), **result})
    return {
        "status": "PASS",
        "profile_path": profile_path.relative_to(repo_root).as_posix() if profile_path.is_relative_to(repo_root) else str(profile_path),
        "supported_profiles": sorted(supported_runtime),
        "migration_status": migration["status"],
        "document_count": len(results),
        "documents": results,
    }


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", type=Path, default=Path("."))
    parser.add_argument("--profile", type=Path, default=Path("cpf-tools/contracts/openapi/cpf-openapi-profile.json"))
    parser.add_argument("--output-json", type=Path)
    return parser


def main(argv: Iterable[str] | None = None) -> int:
    args = _parser().parse_args(argv)
    root = args.repo_root.resolve()
    profile = args.profile if args.profile.is_absolute() else root / args.profile
    try:
        result = verify(root, profile)
    except OpenApiProfileError as exc:
        result = {"status": "FAIL", "error": str(exc)}
        code = 2
    else:
        code = 0
    if args.output_json:
        args.output_json.parent.mkdir(parents=True, exist_ok=True)
        args.output_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())

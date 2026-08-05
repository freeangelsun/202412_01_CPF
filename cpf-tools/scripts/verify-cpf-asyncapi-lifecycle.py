#!/usr/bin/env python3
"""Validate CPF AsyncAPI lifecycle documents, source ownership, consumers and migrations."""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path, PurePosixPath
from typing import Iterable

SEMVER = re.compile(r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-[0-9A-Za-z.-]+)?(?:\+[0-9A-Za-z.-]+)?$")
ADDRESS = re.compile(r"^[a-z][a-z0-9._{}-]+$")
SHA = re.compile(r"^[0-9a-f]{40}$")
ALLOWED_COMPATIBILITY = {"BACKWARD", "FORWARD", "FULL"}


class AsyncApiError(RuntimeError):
    pass


def _safe(raw: str) -> PurePosixPath:
    path = PurePosixPath(raw.replace("\\", "/"))
    if path.is_absolute() or not path.parts or any(p in {"", ".", ".."} for p in path.parts) or ":" in path.parts[0]:
        raise AsyncApiError(f"unsafe repository path: {raw!r}")
    return path


def _load(path: Path) -> object:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise AsyncApiError(f"cannot read {path}: {exc}") from exc


def _strings(value: object, field: str) -> list[str]:
    if not isinstance(value, list) or not value or any(not isinstance(v, str) or not v.strip() for v in value):
        raise AsyncApiError(f"{field} must be a non-empty string array")
    return [v.strip() for v in value]


def _path_exists(repo_root: Path, raw: str, inventory: dict[str, dict]) -> bool:
    path = _safe(raw)
    target = repo_root.joinpath(*path.parts)
    if target.exists():
        return True
    item = inventory.get(path.as_posix())
    return bool(item and item.get("type") in {"blob", "tree"} and SHA.fullmatch(str(item.get("sha", ""))))


def validate_document(document: object, supported_versions: set[str], source: str) -> dict:
    if not isinstance(document, dict):
        raise AsyncApiError(f"{source}: document must be an object")
    version = str(document.get("asyncapi", ""))
    if version not in supported_versions:
        raise AsyncApiError(f"{source}: unsupported AsyncAPI version {version!r}")
    info = document.get("info")
    if not isinstance(info, dict) or not str(info.get("title", "")).strip() or not SEMVER.fullmatch(str(info.get("version", ""))):
        raise AsyncApiError(f"{source}: info.title and semantic info.version are required")
    channels = document.get("channels")
    if not isinstance(channels, dict) or not channels:
        raise AsyncApiError(f"{source}: channels must be non-empty")
    addresses: set[str] = set()
    channel_messages: set[str] = set()
    for channel_id, channel in channels.items():
        if not isinstance(channel, dict):
            raise AsyncApiError(f"{source}: channel {channel_id} must be an object")
        address = str(channel.get("address", ""))
        if not ADDRESS.fullmatch(address):
            raise AsyncApiError(f"{source}: channel {channel_id} address is invalid: {address!r}")
        if address in addresses:
            raise AsyncApiError(f"{source}: duplicate channel address {address}")
        addresses.add(address)
        messages = channel.get("messages")
        if not isinstance(messages, dict) or not messages:
            raise AsyncApiError(f"{source}: channel {channel_id} requires messages")
        channel_messages.update(messages.keys())
    components = document.get("components")
    if not isinstance(components, dict):
        raise AsyncApiError(f"{source}: components are required")
    messages = components.get("messages")
    schemas = components.get("schemas")
    if not isinstance(messages, dict) or not messages or not isinstance(schemas, dict) or not schemas:
        raise AsyncApiError(f"{source}: components.messages and components.schemas are required")
    message_ids: set[str] = set()
    for name, message in messages.items():
        if not isinstance(message, dict):
            raise AsyncApiError(f"{source}: message {name} must be an object")
        message_id = str(message.get("messageId", "")).strip()
        if not message_id or message_id in message_ids:
            raise AsyncApiError(f"{source}: messageId must be unique and non-empty")
        message_ids.add(message_id)
        payload = message.get("payload")
        if not isinstance(payload, dict) or not str(payload.get("$ref", "")).startswith("#/components/schemas/"):
            raise AsyncApiError(f"{source}: message {name} payload must reference components.schemas")
        correlation = message.get("correlationId")
        if not isinstance(correlation, dict) or not str(correlation.get("location", "")).strip():
            raise AsyncApiError(f"{source}: message {name} requires correlationId.location")
    operations = document.get("operations")
    if not isinstance(operations, dict) or not operations:
        raise AsyncApiError(f"{source}: operations must be non-empty")
    operation_ids: set[str] = set()
    for operation_id, operation in operations.items():
        if operation_id in operation_ids:
            raise AsyncApiError(f"{source}: duplicate operation id {operation_id}")
        operation_ids.add(operation_id)
        if not isinstance(operation, dict) or operation.get("action") not in {"send", "receive"}:
            raise AsyncApiError(f"{source}: operation {operation_id} action must be send/receive")
        channel_ref = operation.get("channel")
        if not isinstance(channel_ref, dict) or not str(channel_ref.get("$ref", "")).startswith("#/channels/"):
            raise AsyncApiError(f"{source}: operation {operation_id} requires channel $ref")
        refs = operation.get("messages")
        if not isinstance(refs, list) or not refs:
            raise AsyncApiError(f"{source}: operation {operation_id} requires messages")
        for ref in refs:
            if not isinstance(ref, dict) or not str(ref.get("$ref", "")).startswith("#/channels/"):
                raise AsyncApiError(f"{source}: operation {operation_id} message refs must target channel messages")
    return {
        "asyncapi_version": version,
        "info_version": info["version"],
        "channel_count": len(channels),
        "message_count": len(messages),
        "operation_count": len(operations),
        "schema_count": len(schemas),
    }


def verify(repo_root: Path, catalog_path: Path) -> dict:
    catalog = _load(catalog_path)
    if not isinstance(catalog, dict) or catalog.get("schema_version") != "1.0":
        raise AsyncApiError("catalog schema_version must be 1.0")
    commit_sha = str(catalog.get("source_commit_sha", ""))
    if not SHA.fullmatch(commit_sha):
        raise AsyncApiError("catalog source_commit_sha must be a 40-character SHA")
    supported_versions = set(_strings(catalog.get("supported_asyncapi_versions"), "supported_asyncapi_versions"))
    inventory_raw = catalog.get("source_inventory", [])
    if not isinstance(inventory_raw, list):
        raise AsyncApiError("source_inventory must be an array")
    inventory: dict[str, dict] = {}
    for item in inventory_raw:
        if not isinstance(item, dict):
            raise AsyncApiError("source_inventory item must be an object")
        path = _safe(str(item.get("path", ""))).as_posix()
        if path in inventory:
            raise AsyncApiError(f"duplicate source_inventory path: {path}")
        if item.get("type") not in {"blob", "tree"} or not SHA.fullmatch(str(item.get("sha", ""))):
            raise AsyncApiError(f"invalid source_inventory entry: {path}")
        inventory[path] = item
    entries = catalog.get("documents")
    if not isinstance(entries, list) or not entries:
        raise AsyncApiError("documents must be a non-empty array")
    document_paths: set[str] = set()
    results: list[dict] = []
    for index, entry in enumerate(entries):
        field = f"documents[{index}]"
        if not isinstance(entry, dict):
            raise AsyncApiError(f"{field} must be an object")
        path = _safe(str(entry.get("path", ""))).as_posix()
        if path in document_paths:
            raise AsyncApiError(f"duplicate AsyncAPI document path: {path}")
        document_paths.add(path)
        owner = str(entry.get("owner", "")).strip()
        if not owner:
            raise AsyncApiError(f"{field}.owner is required")
        compatibility = str(entry.get("compatibility", ""))
        if compatibility not in ALLOWED_COMPATIBILITY:
            raise AsyncApiError(f"{field}.compatibility is invalid")
        source_contracts = _strings(entry.get("source_contracts"), f"{field}.source_contracts")
        consumers = _strings(entry.get("consumers"), f"{field}.consumers")
        missing_paths = [p for p in source_contracts + consumers if not _path_exists(repo_root, p, inventory)]
        if missing_paths:
            raise AsyncApiError(f"{field} missing source/consumer paths: {', '.join(missing_paths)}")
        doc_path = repo_root / path
        document_result = validate_document(_load(doc_path), supported_versions, path)
        previous = entry.get("previous_schema_version")
        current = document_result["info_version"]
        migration = entry.get("migration")
        if previous and previous != current:
            if not isinstance(migration, dict) or migration.get("status") not in {"READY", "COMPLETE"}:
                raise AsyncApiError(f"{field}: schema version change requires READY/COMPLETE migration")
            evidence = _strings(migration.get("evidence_paths"), f"{field}.migration.evidence_paths")
            missing_evidence = [p for p in evidence if not _path_exists(repo_root, p, inventory)]
            if missing_evidence:
                raise AsyncApiError(f"{field}: migration evidence missing: {', '.join(missing_evidence)}")
        results.append({
            "path": path,
            "owner": owner,
            "compatibility": compatibility,
            "source_contract_count": len(source_contracts),
            "consumer_count": len(consumers),
            **document_result,
        })
    return {
        "status": "PASS",
        "source_commit_sha": commit_sha,
        "supported_asyncapi_versions": sorted(supported_versions),
        "document_count": len(results),
        "documents": results,
    }


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", required=True, type=Path)
    parser.add_argument("--catalog", required=True, type=Path)
    parser.add_argument("--output-json", type=Path)
    return parser


def main(argv: Iterable[str] | None = None) -> int:
    args = _parser().parse_args(argv)
    try:
        result = verify(args.repo_root, args.catalog)
    except AsyncApiError as exc:
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

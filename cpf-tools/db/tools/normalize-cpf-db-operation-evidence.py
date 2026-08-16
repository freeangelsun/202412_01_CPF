#!/usr/bin/env python3
"""Validate and normalize CPF DB operation evidence.

This is the DB-provider-side public contract consumer. It is intentionally
dependency-free and fail-closed so it can run in installation and recovery
environments before the ADM/BZA HTTP consumers are available.
"""
from __future__ import annotations

import argparse
import datetime as dt
import hashlib
import json
import re
import sys
from pathlib import Path
from typing import Any

HEX40 = re.compile(r"^[0-9a-f]{40}$", re.I)
HEX64 = re.compile(r"^[0-9a-f]{64}$", re.I)
TRACE = re.compile(r"^[0-9a-f]{16,32}$", re.I)
SPAN = re.compile(r"^[0-9a-f]{16}$", re.I)
OPERATION_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{2,127}$")

class ContractError(ValueError):
    pass

def load_object(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8-sig"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ContractError(f"cannot load JSON {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise ContractError(f"JSON root must be object: {path}")
    return value

def parse_instant(value: Any, field: str) -> dt.datetime:
    if not isinstance(value, str) or not value.strip():
        raise ContractError(f"{field} must be a non-empty ISO-8601 string")
    candidate = value.replace("Z", "+00:00")
    try:
        parsed = dt.datetime.fromisoformat(candidate)
    except ValueError as exc:
        raise ContractError(f"{field} must be ISO-8601") from exc
    if parsed.tzinfo is None:
        raise ContractError(f"{field} must include timezone")
    return parsed

def iter_secret_paths(value: Any, pattern: re.Pattern[str], path: str = "$"):
    if isinstance(value, dict):
        for key, item in value.items():
            child = f"{path}.{key}"
            if pattern.search(str(key)):
                yield child
            yield from iter_secret_paths(item, pattern, child)
    elif isinstance(value, list):
        for index, item in enumerate(value):
            yield from iter_secret_paths(item, pattern, f"{path}[{index}]")

def validate(contract: dict[str, Any], evidence: dict[str, Any]) -> dict[str, Any]:
    public = contract["publicContract"]
    missing = [name for name in public["requestRequiredFields"] if name not in evidence]
    if missing:
        raise ContractError("missing required fields: " + ",".join(sorted(missing)))

    operation_id = evidence["operationId"]
    if not isinstance(operation_id, str) or not OPERATION_ID.fullmatch(operation_id):
        raise ContractError("operationId format is invalid")

    capability_ids = {item["id"] for item in contract["capabilities"]}
    if evidence["capabilityId"] not in capability_ids:
        raise ContractError("capabilityId is not registered")

    if evidence["vendor"] not in contract["officialVendors"]:
        raise ContractError("vendor is not an official CPF vendor")

    for name in ("environment", "topology", "operator", "reason", "approvalReference", "approvedBy", "runbookRef"):
        if not isinstance(evidence[name], str) or not evidence[name].strip():
            raise ContractError(f"{name} is required")
        if any(ch in evidence[name] for ch in "\r\n\x00"):
            raise ContractError(f"{name} contains a control character")

    if evidence["operator"].casefold() == evidence["approvedBy"].casefold():
        raise ContractError("operator and approvedBy must be different")

    started = parse_instant(evidence["startedAt"], "startedAt")
    finished = parse_instant(evidence["finishedAt"], "finishedAt")
    if finished < started:
        raise ContractError("finishedAt is before startedAt")

    state = evidence["resultStatus"]
    if state not in public["resultStates"]:
        raise ContractError("resultStatus is invalid")
    reconcile = evidence["reconcileRequired"]
    if not isinstance(reconcile, bool):
        raise ContractError("reconcileRequired must be boolean")
    if state == "UNKNOWN" and reconcile is not True:
        raise ContractError("UNKNOWN requires reconcileRequired=true")
    if state == "RECONCILED" and reconcile is not False:
        raise ContractError("RECONCILED requires reconcileRequired=false")

    if not isinstance(evidence["sourceSha"], str) or not HEX40.fullmatch(evidence["sourceSha"]):
        raise ContractError("sourceSha must be a 40-character Git SHA")
    if not isinstance(evidence["evidenceSha256"], str) or not HEX64.fullmatch(evidence["evidenceSha256"]):
        raise ContractError("evidenceSha256 must be SHA-256")

    metrics = evidence["metrics"]
    if not isinstance(metrics, dict):
        raise ContractError("metrics must be object")
    for name in public["metricsRequired"]:
        if name not in metrics:
            raise ContractError(f"metrics.{name} is required")
        value = metrics[name]
        if isinstance(value, bool) or not isinstance(value, (int, float)) or value < 0:
            raise ContractError(f"metrics.{name} must be non-negative number")

    trace = evidence["trace"]
    if not isinstance(trace, dict):
        raise ContractError("trace must be object")
    if not TRACE.fullmatch(str(trace.get("traceId", ""))):
        raise ContractError("trace.traceId format is invalid")
    if not SPAN.fullmatch(str(trace.get("spanId", ""))):
        raise ContractError("trace.spanId format is invalid")

    health = evidence["health"]
    if not isinstance(health, dict):
        raise ContractError("health must be object")
    valid_health = set(public["healthStates"])
    before = health.get("before")
    after = health.get("after")
    if before not in valid_health or after not in valid_health:
        raise ContractError("health state is invalid")
    if state in {"SUCCEEDED", "RECONCILED"} and after not in {"UP", "DEGRADED"}:
        raise ContractError("successful result cannot finish DOWN or UNKNOWN")

    alerts = evidence["alerts"]
    if not isinstance(alerts, list) or not all(isinstance(item, str) and item.strip() for item in alerts):
        raise ContractError("alerts must be a list of non-empty strings")
    if state in {"FAILED", "UNKNOWN"} and not alerts:
        raise ContractError("FAILED or UNKNOWN requires at least one alert")

    secret_pattern = re.compile(public["secretKeyPattern"])
    secret_paths = list(iter_secret_paths(evidence, secret_pattern))
    if secret_paths:
        raise ContractError("secret-bearing evidence keys are prohibited: " + ",".join(secret_paths[:10]))

    duration = int((finished - started).total_seconds() * 1000)
    declared_duration = int(metrics["durationMs"])
    if abs(duration - declared_duration) > 1000:
        raise ContractError("metrics.durationMs does not match timestamps")

    capability = next(item for item in contract["capabilities"] if item["id"] == evidence["capabilityId"])
    return {
        "schemaVersion": 1,
        "operationId": operation_id,
        "capabilityId": evidence["capabilityId"],
        "vendor": evidence["vendor"],
        "environment": evidence["environment"],
        "topology": evidence["topology"],
        "resultStatus": state,
        "reconcileRequired": reconcile,
        "operator": evidence["operator"],
        "approvedBy": evidence["approvedBy"],
        "approvalReference": evidence["approvalReference"],
        "reason": evidence["reason"],
        "startedAt": evidence["startedAt"],
        "finishedAt": evidence["finishedAt"],
        "sourceSha": evidence["sourceSha"].lower(),
        "evidenceSha256": evidence["evidenceSha256"].lower(),
        "metrics": {name: metrics[name] for name in public["metricsRequired"]},
        "trace": {"traceId": trace["traceId"].lower(), "spanId": trace["spanId"].lower()},
        "health": {"before": before, "after": after},
        "alerts": alerts,
        "runbookRef": evidence["runbookRef"],
        "consumer": capability["consumer"],
        "verifier": capability["verifier"],
        "contractSha256": hashlib.sha256(
            json.dumps(contract, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode("utf-8")
        ).hexdigest(),
    }

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--contract", required=True)
    parser.add_argument("--evidence", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    try:
        contract = load_object(Path(args.contract))
        evidence = load_object(Path(args.evidence))
        normalized = validate(contract, evidence)
        output = Path(args.output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(normalized, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(json.dumps({"status": "PASS", "operationId": normalized["operationId"]}, sort_keys=True))
        return 0
    except ContractError as exc:
        print(json.dumps({"status": "FAIL", "error": str(exc)}, ensure_ascii=False, sort_keys=True), file=sys.stderr)
        return 2

if __name__ == "__main__":
    raise SystemExit(main())

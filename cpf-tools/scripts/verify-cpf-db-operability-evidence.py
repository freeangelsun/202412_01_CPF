#!/usr/bin/env python3
"""Validate the common CPF database operation evidence envelope."""
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
TRACE_ID = re.compile(r"^[0-9a-f]{32}$", re.I)
SPAN_ID = re.compile(r"^[0-9a-f]{16}$", re.I)


class EvidenceError(RuntimeError):
    pass


def load(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8-sig"))
    except (OSError, UnicodeError, json.JSONDecodeError) as exc:
        raise EvidenceError(f"cannot read JSON {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise EvidenceError(f"JSON root must be object: {path}")
    return value


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def parse_time(value: Any, field: str, reasons: list[str]) -> dt.datetime | None:
    if not isinstance(value, str) or not value.strip():
        reasons.append(f"{field} is required")
        return None
    try:
        parsed = dt.datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        reasons.append(f"{field} must be ISO-8601 timestamp")
        return None
    if parsed.tzinfo is None:
        reasons.append(f"{field} must include timezone")
        return None
    return parsed


def secret_paths(value: Any, pattern: re.Pattern[str], path: str = "$") -> list[str]:
    findings: list[str] = []
    if isinstance(value, dict):
        for key, child in value.items():
            child_path = f"{path}.{key}"
            if pattern.search(str(key)):
                findings.append(child_path)
            findings.extend(secret_paths(child, pattern, child_path))
    elif isinstance(value, list):
        for index, child in enumerate(value):
            findings.extend(secret_paths(child, pattern, f"{path}[{index}]"))
    return findings


def nonnegative_number(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool) and value >= 0


def evaluate(contract: dict[str, Any], evidence: dict[str, Any]) -> dict[str, Any]:
    reasons: list[str] = []
    public = contract.get("publicContract") or {}
    for field in public.get("requestRequiredFields") or []:
        if field not in evidence or evidence[field] in (None, ""):
            reasons.append(f"missing required field: {field}")

    vendors = contract.get("officialVendors") or []
    if evidence.get("vendor") not in vendors:
        reasons.append("unsupported vendor")
    capability_ids = {item.get("id") for item in contract.get("capabilities") or []}
    if evidence.get("capabilityId") not in capability_ids:
        reasons.append("unsupported capabilityId")
    if len(str(evidence.get("reason", "")).strip()) < 10:
        reasons.append("reason must contain at least 10 characters")
    if not str(evidence.get("approvalReference", "")).strip():
        reasons.append("approvalReference is required")
    if evidence.get("operator") == evidence.get("approvedBy"):
        reasons.append("independent operator and approver are required")
    if evidence.get("sanitized") is not True:
        reasons.append("sanitized must be true")
    if not HEX40.fullmatch(str(evidence.get("sourceSha", ""))):
        reasons.append("sourceSha must be exact 40-hex SHA")
    if not HEX64.fullmatch(str(evidence.get("evidenceSha256", ""))):
        reasons.append("evidenceSha256 must be exact 64-hex SHA-256")

    secret_pattern = re.compile(public.get("secretKeyPattern", r"(?i)(password|secret|token|credential)"))
    leaks = secret_paths(evidence, secret_pattern)
    if leaks:
        reasons.append("secret-bearing evidence keys are prohibited: " + ",".join(leaks))

    started = parse_time(evidence.get("startedAt"), "startedAt", reasons)
    finished = parse_time(evidence.get("finishedAt"), "finishedAt", reasons)
    if started and finished and finished < started:
        reasons.append("finishedAt must not be earlier than startedAt")

    result_status = evidence.get("resultStatus")
    if result_status not in public.get("resultStates", []):
        reasons.append("invalid resultStatus")
    if result_status not in {"SUCCEEDED", "RECONCILED"}:
        reasons.append(f"final DB operation result must be SUCCEEDED or RECONCILED, actual={result_status!r}")
    if result_status in {"SUCCEEDED", "RECONCILED"} and evidence.get("reconcileRequired") is not False:
        reasons.append("successful/reconciled result must clear reconcileRequired")

    metrics = evidence.get("metrics")
    if not isinstance(metrics, dict):
        reasons.append("metrics must be object")
    else:
        for field in public.get("metricsRequired") or []:
            if not nonnegative_number(metrics.get(field)):
                reasons.append(f"metrics.{field} must be non-negative number")
        if started and finished and nonnegative_number(metrics.get("durationMs")):
            elapsed_ms = (finished - started).total_seconds() * 1000
            if abs(float(metrics["durationMs"]) - elapsed_ms) > max(1000, elapsed_ms * 0.10):
                reasons.append("metrics.durationMs does not match timestamps")

    trace = evidence.get("trace")
    if not isinstance(trace, dict):
        reasons.append("trace must be object")
    else:
        if not TRACE_ID.fullmatch(str(trace.get("traceId", ""))):
            reasons.append("trace.traceId must be 32-hex W3C trace id")
        if not SPAN_ID.fullmatch(str(trace.get("spanId", ""))):
            reasons.append("trace.spanId must be 16-hex W3C span id")

    health = evidence.get("health")
    if not isinstance(health, dict):
        reasons.append("health must be object")
    else:
        allowed = set(public.get("healthStates") or [])
        for field in ("before", "after"):
            if health.get(field) not in allowed:
                reasons.append(f"health.{field} invalid")
        if health.get("after") != "UP":
            reasons.append("health.after must be UP for final success")

    alerts = evidence.get("alerts")
    if not isinstance(alerts, list):
        reasons.append("alerts must be array")
    elif any(not isinstance(item, str) or not item.strip() for item in alerts):
        reasons.append("alerts entries must be nonblank strings")
    if not str(evidence.get("runbookRef", "")).strip():
        reasons.append("runbookRef is required")

    return {
        "schemaVersion": 1,
        "status": "PASS" if not reasons else "FAIL",
        "operationId": evidence.get("operationId"),
        "capabilityId": evidence.get("capabilityId"),
        "vendor": evidence.get("vendor"),
        "reasons": reasons,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--contract", required=True, type=Path)
    parser.add_argument("--evidence", required=True, type=Path)
    parser.add_argument("--expected-evidence-sha256", required=True)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    try:
        actual = sha256(args.evidence)
        expected = args.expected_evidence_sha256.strip().lower()
        if not HEX64.fullmatch(expected):
            raise EvidenceError("expected evidence SHA-256 must be exact 64-hex")
        result = (
            {"schemaVersion": 1, "status": "FAIL", "reasons": [f"evidence sha256 mismatch expected={expected} actual={actual}"]}
            if actual != expected
            else evaluate(load(args.contract), load(args.evidence))
        )
        result["envelopeSha256"] = actual
        if args.output:
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(json.dumps(result, ensure_ascii=False, sort_keys=True))
        return 0 if result["status"] == "PASS" else 1
    except (EvidenceError, OSError, ValueError, re.error) as exc:
        print(f"CPF DB operability evidence gate FAILED: {exc}", file=sys.stderr)
        return 3


if __name__ == "__main__":
    raise SystemExit(main())

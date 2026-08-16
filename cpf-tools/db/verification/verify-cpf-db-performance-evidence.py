#!/usr/bin/env python3
"""Fail-closed evaluator for CPF three-vendor DB performance evidence."""
from __future__ import annotations

import argparse
import datetime as dt
import re
import hashlib
import json
import sys
from pathlib import Path
from typing import Any

HEX40 = re.compile(r"^[0-9a-f]{40}$")
HEX64 = re.compile(r"^[0-9a-f]{64}$")

SECRET_KEY_RE = re.compile(r"password|secret|credential|private.?key|access.?token", re.I)


def load_object(path: Path) -> dict[str, Any]:
    with path.open(encoding="utf-8-sig") as handle:
        value = json.load(handle)
    if not isinstance(value, dict):
        raise ValueError(f"JSON root must be object: {path}")
    return value


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def secret_paths(value: Any, path: str = "$") -> list[str]:
    findings: list[str] = []
    if isinstance(value, dict):
        for key, child in value.items():
            child_path = f"{path}.{key}"
            if SECRET_KEY_RE.search(str(key)):
                findings.append(child_path)
            findings.extend(secret_paths(child, child_path))
    elif isinstance(value, list):
        for index, child in enumerate(value):
            findings.extend(secret_paths(child, f"{path}[{index}]"))
    return findings


def evaluate(policy: dict[str, Any], evidence: dict[str, Any]) -> dict[str, Any]:
    reasons: list[str] = []
    required_fields = policy.get("requiredEvidenceFields") or []
    for field in required_fields:
        if field not in evidence:
            reasons.append(f"missing evidence field: {field}")

    vendors = policy.get("officialVendors") or []
    if evidence.get("vendor") not in vendors:
        reasons.append(f"unsupported vendor: {evidence.get('vendor')!r}")
    source_sha = str(evidence.get("sourceSha", "")).lower()
    result_sha = str(evidence.get("resultSha", "")).lower()
    if not HEX40.fullmatch(source_sha):
        reasons.append("sourceSha must be exact lowercase 40-hex SHA")
    if not HEX40.fullmatch(result_sha):
        reasons.append("resultSha must be exact lowercase 40-hex SHA")
    if source_sha and result_sha and source_sha != result_sha:
        reasons.append("resultSha must equal sourceSha")
    if not str(evidence.get("databaseVersion", "")).strip():
        reasons.append("databaseVersion is required")
    try:
        started = dt.datetime.fromisoformat(str(evidence.get("startedAt", "")).replace("Z", "+00:00"))
        ended = dt.datetime.fromisoformat(str(evidence.get("endedAt", "")).replace("Z", "+00:00"))
        if ended < started:
            reasons.append("endedAt must not precede startedAt")
    except ValueError:
        reasons.append("startedAt/endedAt must be ISO-8601 timestamps")
    if evidence.get("sanitized") is not True:
        reasons.append("sanitized must be true")
    leaked_paths = secret_paths(evidence)
    if leaked_paths:
        reasons.append("secret-bearing evidence keys are prohibited: " + ",".join(leaked_paths))

    scales = policy.get("representativeDataScales") or {}
    scale = evidence.get("dataScale")
    if scale not in scales:
        reasons.append(f"unknown representative data scale: {scale!r}")

    statistics = evidence.get("statistics")
    required_statistics_fields = (policy.get("statisticsPolicy") or {}).get("evidenceFields") or []
    if not isinstance(statistics, dict):
        reasons.append("statistics must be an object")
    else:
        for field in required_statistics_fields:
            if field not in statistics:
                reasons.append(f"missing statistics field: {field}")
        if statistics.get("statisticsExitCode") != 0:
            reasons.append("statisticsExitCode must be 0")

    query_classes = policy.get("queryClasses") or {}
    query_results = evidence.get("queryResults")
    if not isinstance(query_results, list) or not query_results:
        reasons.append("queryResults must be a non-empty array")
        query_results = []

    seen_query_ids: set[str] = set()
    covered_classes: set[str] = set()
    result_summaries: list[dict[str, Any]] = []
    for index, result in enumerate(query_results):
        prefix = f"queryResults[{index}]"
        if not isinstance(result, dict):
            reasons.append(f"{prefix} must be an object")
            continue
        query_id = str(result.get("queryId", "")).strip()
        if not query_id:
            reasons.append(f"{prefix}.queryId is required")
        elif query_id in seen_query_ids:
            reasons.append(f"duplicate queryId: {query_id}")
        seen_query_ids.add(query_id)
        query_class = result.get("queryClass")
        criteria = query_classes.get(query_class)
        if not isinstance(criteria, dict):
            reasons.append(f"{prefix}.queryClass is unsupported: {query_class!r}")
            continue
        covered_classes.add(str(query_class))
        status = result.get("status")
        if status not in policy.get("resultStates", []):
            reasons.append(f"{prefix}.status is invalid: {status!r}")
        if status != "PASS":
            reasons.append(f"{prefix}.status must be PASS, actual={status!r}")
        latency = result.get("latencyMs")
        examined_rows = result.get("examinedRows")
        if not isinstance(latency, (int, float)) or latency < 0:
            reasons.append(f"{prefix}.latencyMs must be non-negative number")
        elif latency > criteria.get("maxLatencyMs", latency):
            reasons.append(f"{prefix}.latencyMs exceeds {criteria['maxLatencyMs']}")
        if not isinstance(examined_rows, int) or examined_rows < 0:
            reasons.append(f"{prefix}.examinedRows must be non-negative integer")
        elif examined_rows > criteria.get("maxExaminedRows", examined_rows):
            reasons.append(f"{prefix}.examinedRows exceeds {criteria['maxExaminedRows']}")
        if criteria.get("requireIndexedAccess") and result.get("indexedAccess") is not True:
            reasons.append(f"{prefix}.indexedAccess must be true")
        if criteria.get("requireStableSort") and result.get("stableSort") is not True:
            reasons.append(f"{prefix}.stableSort must be true")
        if criteria.get("requireLockEvidence") and not str(result.get("lockEvidence", "")).strip():
            reasons.append(f"{prefix}.lockEvidence is required")
        if criteria.get("requireBoundedBatch") and result.get("boundedBatch") is not True:
            reasons.append(f"{prefix}.boundedBatch must be true")
        plan_sha = str(result.get("planSha256", "")).lower()
        if not HEX64.fullmatch(plan_sha):
            reasons.append(f"{prefix}.planSha256 must be exact lowercase 64-hex SHA-256")
        if result.get("bindValuesSanitized") is not True:
            reasons.append(f"{prefix}.bindValuesSanitized must be true")
        result_summaries.append({
            "queryId": query_id,
            "queryClass": query_class,
            "status": status,
            "latencyMs": latency,
            "examinedRows": examined_rows,
        })

    missing_classes = sorted(set(query_classes) - covered_classes)
    if missing_classes:
        reasons.append(f"query class coverage missing: {missing_classes}")

    status = "PASS" if not reasons else "FAIL"
    return {
        "schemaVersion": 1,
        "status": status,
        "runtimeClaim": "RUNTIME_EVIDENCE_EVALUATION",
        "vendor": evidence.get("vendor"),
        "dataScale": evidence.get("dataScale"),
        "queryCount": len(query_results),
        "coveredQueryClasses": sorted(covered_classes),
        "reasons": reasons,
        "queryResults": result_summaries,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--policy", required=True)
    parser.add_argument("--evidence", required=True)
    parser.add_argument("--expected-evidence-sha256", required=True)
    parser.add_argument("--output")
    args = parser.parse_args()
    policy_path = Path(args.policy)
    evidence_path = Path(args.evidence)
    actual_sha = sha256(evidence_path)
    expected_sha = args.expected_evidence_sha256.strip().lower()
    if actual_sha != expected_sha:
        result = {
            "schemaVersion": 1,
            "status": "FAIL",
            "runtimeClaim": "RUNTIME_EVIDENCE_EVALUATION",
            "reasons": [f"evidence sha256 mismatch expected={expected_sha} actual={actual_sha}"],
        }
    else:
        result = evaluate(load_object(policy_path), load_object(evidence_path))
    result["evidenceSha256"] = actual_sha
    if args.output:
        output = Path(args.output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, sort_keys=True))
    return 0 if result["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())

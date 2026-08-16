#!/usr/bin/env python3
"""Fail-closed validator for CPF DataSource routing, concurrency, and capacity evidence."""
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
SECRET = re.compile(r"password|passwd|secret|credential|access.?token|private.?key", re.I)


def load(path: Path) -> dict[str, Any]:
    value = json.loads(path.read_text(encoding="utf-8-sig"))
    if not isinstance(value, dict):
        raise ValueError(f"JSON root must be object: {path}")
    return value


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def secret_paths(value: Any, path: str = "$") -> list[str]:
    findings: list[str] = []
    if isinstance(value, dict):
        for key, child in value.items():
            child_path = f"{path}.{key}"
            if SECRET.search(str(key)):
                findings.append(child_path)
            findings.extend(secret_paths(child, child_path))
    elif isinstance(value, list):
        for index, child in enumerate(value):
            findings.extend(secret_paths(child, f"{path}[{index}]"))
    return findings


def positive_int(value: Any) -> bool:
    return isinstance(value, int) and not isinstance(value, bool) and value > 0


def nonnegative_int(value: Any) -> bool:
    return isinstance(value, int) and not isinstance(value, bool) and value >= 0


def parse_time(value: Any, field: str, reasons: list[str]) -> dt.datetime | None:
    try:
        parsed = dt.datetime.fromisoformat(str(value).replace("Z", "+00:00"))
    except ValueError:
        reasons.append(f"{field} must be ISO-8601 timestamp")
        return None
    if parsed.tzinfo is None:
        reasons.append(f"{field} must include timezone")
        return None
    return parsed


def evaluate(policy: dict[str, Any], evidence: dict[str, Any]) -> dict[str, Any]:
    reasons: list[str] = []
    if evidence.get("vendor") not in policy.get("officialVendors", []):
        reasons.append("unsupported vendor")
    if not HEX40.fullmatch(str(evidence.get("sourceSha", ""))):
        reasons.append("sourceSha must be exact 40-hex SHA")
    if not str(evidence.get("operationId", "")).strip():
        reasons.append("operationId is required")
    if len(str(evidence.get("reason", "")).strip()) < 10:
        reasons.append("reason must contain at least 10 characters")
    if not evidence.get("operator") or not evidence.get("approvedBy"):
        reasons.append("operator and approvedBy are required")
    if evidence.get("operator") == evidence.get("approvedBy"):
        reasons.append("independent approval is required")
    if evidence.get("sanitized") is not True:
        reasons.append("sanitized must be true")
    leaks = secret_paths(evidence)
    if leaks:
        reasons.append("secret-bearing evidence keys are prohibited: " + ",".join(leaks))
    if evidence.get("status") not in policy.get("resultStates", []):
        reasons.append("invalid status")
    if evidence.get("status") != "PASS":
        reasons.append("runtime evidence status must be PASS")
    started = parse_time(evidence.get("startedAt"), "startedAt", reasons)
    ended = parse_time(evidence.get("endedAt"), "endedAt", reasons)
    if started and ended and ended < started:
        reasons.append("endedAt must not be earlier than startedAt")

    route = evidence.get("routing")
    if not isinstance(route, dict):
        reasons.append("routing must be object")
    else:
        if route.get("writeTarget") != "PRIMARY":
            reasons.append("writes must target PRIMARY")
        if route.get("readTarget") not in {"PRIMARY", "REPLICA"}:
            reasons.append("readTarget must be PRIMARY or REPLICA")
        consistency = route.get("consistency")
        if consistency not in policy["readReplicaRouting"]["allowedConsistency"]:
            reasons.append("unsupported consistency")
        if consistency == "STRONG" and route.get("readTarget") != "PRIMARY":
            reasons.append("STRONG reads must target PRIMARY")
        if route.get("readTarget") == "REPLICA" and route.get("readOnly") is not True:
            reasons.append("replica route must be readOnly")
        lag_known = route.get("lagKnown", True)
        lag = route.get("replicaLagMs")
        if lag_known is False:
            if route.get("readTarget") != policy["replicaLag"]["unknownLagAction"]:
                reasons.append("unknown replica lag must route to PRIMARY")
        elif not nonnegative_int(lag):
            reasons.append("replicaLagMs must be non-negative integer")
        elif lag > policy["replicaLag"]["maxLagMs"] and route.get("readTarget") != policy["replicaLag"]["exceededLagAction"]:
            reasons.append("lagged replica must fall back to PRIMARY")
        if not str(route.get("freshnessEvidence", "")).strip():
            reasons.append("replica freshness evidence is required")
        if not str(route.get("decisionAuditId", "")).strip():
            reasons.append("route decision audit is required")

    multi = evidence.get("multiDataSource")
    if not isinstance(multi, dict):
        reasons.append("multiDataSource must be object")
    else:
        if not str(multi.get("owner", "")).strip():
            reasons.append("DataSource owner is required")
        if multi.get("crossOwnerWrite") is True:
            reasons.append("cross-owner write is prohibited")
        resources = multi.get("resourceCount")
        if not positive_int(resources):
            reasons.append("resourceCount must be positive integer")
        elif resources > 1 and multi.get("writeOperation") is True and not str(multi.get("compensationPlan", "")).strip():
            reasons.append("multi-resource write requires compensation plan")

    pool = evidence.get("connectionPool")
    if not isinstance(pool, dict):
        reasons.append("connectionPool must be object")
    else:
        size, instances = pool.get("maxPoolSize"), pool.get("instanceCount")
        budget, reserved = pool.get("databaseConnectionBudget"), pool.get("reservedConnections")
        if not all(positive_int(value) for value in (size, instances, budget)) or not nonnegative_int(reserved):
            reasons.append("connection pool numeric fields are invalid")
        else:
            if size < policy["connectionPool"]["minimumSize"] or size > policy["connectionPool"]["maximumPoolPerInstance"]:
                reasons.append("maxPoolSize outside policy")
            if reserved >= budget or size * instances > budget - reserved:
                reasons.append("connection pool exceeds database connection budget")
        if not positive_int(pool.get("connectionTimeoutMs")) or pool["connectionTimeoutMs"] > policy["connectionPool"]["connectionTimeoutMsMax"]:
            reasons.append("connectionTimeoutMs outside policy")
        if not positive_int(pool.get("validationTimeoutMs")) or pool["validationTimeoutMs"] > policy["connectionPool"]["validationTimeoutMsMax"]:
            reasons.append("validationTimeoutMs outside policy")
        if not positive_int(pool.get("maxLifetimeMs")) or pool["maxLifetimeMs"] < policy["connectionPool"]["maxLifetimeMsMin"]:
            reasons.append("maxLifetimeMs outside policy")

    transaction = evidence.get("transaction")
    if not isinstance(transaction, dict):
        reasons.append("transaction must be object")
    else:
        isolation = transaction.get("isolation")
        if isolation not in policy["transactionIsolation"]["allowed"]:
            reasons.append("unsupported transaction isolation")
        if isolation == "SERIALIZABLE" and len(str(transaction.get("serializableReason", "")).strip()) < 10:
            reasons.append("SERIALIZABLE requires reason")
        outcome = transaction.get("commitOutcome")
        if outcome not in {"COMMITTED", "ROLLED_BACK", "UNKNOWN"}:
            reasons.append("commitOutcome is invalid")
        if outcome == "UNKNOWN":
            reasons.append("UNKNOWN commit outcome requires reconciliation and cannot pass")
            if transaction.get("retried") is True:
                reasons.append("UNKNOWN commit must not be blindly retried")
        if transaction.get("deadlockDetected") is True:
            if not transaction.get("idempotencyKey"):
                reasons.append("deadlock retry requires idempotencyKey")
            attempts = transaction.get("attemptCount")
            if not positive_int(attempts) or attempts > policy["deadlockHandling"]["maxAttempts"]:
                reasons.append("deadlock attemptCount outside policy")

    timeouts = evidence.get("timeouts")
    if not isinstance(timeouts, dict):
        reasons.append("timeouts must be object")
    else:
        query_timeout, lock_timeout = timeouts.get("queryTimeoutMs"), timeouts.get("lockTimeoutMs")
        if not positive_int(query_timeout) or query_timeout > policy["timeouts"]["queryTimeoutMsMax"]:
            reasons.append("queryTimeoutMs outside policy")
        if not positive_int(lock_timeout) or lock_timeout > policy["timeouts"]["lockTimeoutMsMax"]:
            reasons.append("lockTimeoutMs outside policy")

    slow = evidence.get("slowQuery")
    if not isinstance(slow, dict):
        reasons.append("slowQuery must be object")
    else:
        duration = slow.get("durationMs")
        if isinstance(duration, bool) or not isinstance(duration, (int, float)) or duration < 0:
            reasons.append("slowQuery.durationMs invalid")
        elif duration >= policy["slowQueryAlert"]["thresholdMs"]:
            if slow.get("alertState") not in {"WARN", "CRITICAL"}:
                reasons.append("slow query above threshold requires alert")
            if not HEX64.fullmatch(str(slow.get("planSha256", ""))):
                reasons.append("slow query planSha256 required")
        elif slow.get("alertState") not in {"NORMAL", "WARN"}:
            reasons.append("slow query below threshold has invalid alert state")
        if slow.get("bindValuesSanitized") is not True:
            reasons.append("slow query bind values must be sanitized")

    capacity = evidence.get("capacityForecast")
    if not isinstance(capacity, dict):
        reasons.append("capacityForecast must be object")
    else:
        if not positive_int(capacity.get("observationDays")) or capacity["observationDays"] < policy["capacityForecast"]["minimumObservationDays"]:
            reasons.append("capacity observation window too short")
        if capacity.get("forecastHorizonDays") != policy["capacityForecast"]["forecastHorizonDays"]:
            reasons.append("capacity forecast horizon mismatch")
        headroom = capacity.get("headroomPercent")
        if isinstance(headroom, bool) or not isinstance(headroom, (int, float)) or headroom < policy["capacityForecast"]["headroomPercentMin"]:
            reasons.append("capacity headroom below policy")
        growth = capacity.get("growthRatePerDay")
        if isinstance(growth, bool) or not isinstance(growth, (int, float)):
            reasons.append("capacity growthRatePerDay required")
        elif growth > 0 and not str(capacity.get("estimatedExhaustionDate", "")).strip():
            reasons.append("positive growth requires estimatedExhaustionDate")
        state = capacity.get("state")
        if state not in policy["capacityForecast"]["allowedStates"]:
            reasons.append("capacity state invalid")
        if state == "UNKNOWN":
            reasons.append("UNKNOWN capacity state cannot pass")

    return {"schemaVersion": 1, "status": "PASS" if not reasons else "FAIL", "vendor": evidence.get("vendor"), "operationId": evidence.get("operationId"), "reasons": reasons}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--policy", required=True, type=Path)
    parser.add_argument("--evidence", required=True, type=Path)
    parser.add_argument("--expected-evidence-sha256", required=True)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    try:
        actual = sha(args.evidence)
        expected = args.expected_evidence_sha256.strip().lower()
        if not HEX64.fullmatch(expected):
            raise ValueError("expected evidence SHA-256 must be exact 64-hex")
        result = {"schemaVersion": 1, "status": "FAIL", "reasons": [f"evidence sha256 mismatch expected={expected} actual={actual}"]} if actual != expected else evaluate(load(args.policy), load(args.evidence))
        result["evidenceSha256"] = actual
        if args.output:
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(json.dumps(result, ensure_ascii=False, sort_keys=True))
        return 0 if result["status"] == "PASS" else 1
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"CPF DataSource evidence gate FAILED: {exc}", file=sys.stderr)
        return 3


if __name__ == "__main__":
    raise SystemExit(main())

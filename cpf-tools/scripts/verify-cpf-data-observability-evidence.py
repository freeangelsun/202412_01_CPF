#!/usr/bin/env python3
"""Fail-closed validator for CPF lineage, data-quality, and reconciliation evidence."""
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
SECRET = re.compile(r"password|passwd|secret|credential|access.?token|private.?key|raw.?row", re.I)


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


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def secret_paths(value: Any, path: str = "$") -> list[str]:
    out: list[str] = []
    if isinstance(value, dict):
        for key, child in value.items():
            child_path = f"{path}.{key}"
            if SECRET.search(str(key)):
                out.append(child_path)
            out.extend(secret_paths(child, child_path))
    elif isinstance(value, list):
        for index, child in enumerate(value):
            out.extend(secret_paths(child, f"{path}[{index}]"))
    return out


def required(obj: dict[str, Any], fields: list[str], prefix: str, reasons: list[str]) -> None:
    for field in fields:
        if field not in obj or obj[field] in (None, ""):
            reasons.append(f"{prefix}.{field} is required")


def nonnegative_int(value: Any) -> bool:
    return isinstance(value, int) and not isinstance(value, bool) and value >= 0


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


def threshold_passes(rule: dict[str, Any], result: dict[str, Any]) -> bool | None:
    threshold_type = rule.get("thresholdType")
    threshold = rule.get("thresholdValue")
    invalid = result.get("invalidCount")
    total = result.get("totalCount")
    if isinstance(threshold, bool) or not isinstance(threshold, (int, float)):
        return None
    if not nonnegative_int(invalid) or not nonnegative_int(total) or invalid > total:
        return None
    if threshold_type == "MAX_INVALID_COUNT":
        return invalid <= threshold
    if threshold_type == "MAX_INVALID_RATIO":
        return total > 0 and invalid / total <= threshold
    if threshold_type == "MIN_VALID_RATIO":
        return total > 0 and (total - invalid) / total >= threshold
    if threshold_type == "EXACT_COUNT":
        actual = result.get("actualCount")
        return nonnegative_int(actual) and actual == threshold
    return None


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
    if not str(evidence.get("operator", "")).strip() or not str(evidence.get("approvedBy", "")).strip():
        reasons.append("operator and approvedBy are required")
    if evidence.get("operator") == evidence.get("approvedBy"):
        reasons.append("independent approval is required")
    if evidence.get("sanitized") is not True:
        reasons.append("sanitized must be true")
    leaks = secret_paths(evidence)
    if leaks:
        reasons.append("secret-bearing evidence keys are prohibited: " + ",".join(leaks))

    started = parse_time(evidence.get("startedAt"), "startedAt", reasons)
    ended = parse_time(evidence.get("endedAt"), "endedAt", reasons)
    if started and ended and ended < started:
        reasons.append("endedAt must not be earlier than startedAt")

    state = evidence.get("state")
    if state not in policy.get("stateModel", []):
        reasons.append("invalid state")
    if state in {"PARTIAL", "UNKNOWN"} and not str(evidence.get("reconcilePlan", "")).strip():
        reasons.append("reconcilePlan is required for partial/unknown")
    if state != "PASS":
        reasons.append(f"final observability evidence state must be PASS, actual={state!r}")

    lineage = evidence.get("lineage")
    if not isinstance(lineage, dict):
        reasons.append("lineage must be object")
    else:
        if lineage.get("contractVersion") != policy["lineage"]["contractVersion"]:
            reasons.append("lineage.contractVersion mismatch")
        nodes = lineage.get("nodes")
        edges = lineage.get("edges")
        if not isinstance(nodes, list) or len(nodes) < policy["lineage"]["minimumNodeCount"]:
            reasons.append("lineage.nodes is below minimum")
            nodes = [] if not isinstance(nodes, list) else nodes
        if not isinstance(edges, list) or len(edges) < policy["lineage"]["minimumEdgeCount"]:
            reasons.append("lineage.edges is below minimum")
            edges = [] if not isinstance(edges, list) else edges
        node_ids: set[str] = set()
        for index, node in enumerate(nodes):
            if not isinstance(node, dict):
                reasons.append(f"lineage.nodes[{index}] must be object")
                continue
            required(node, policy["lineage"]["requiredNodeFields"], f"lineage.nodes[{index}]", reasons)
            if node.get("objectType") not in policy["lineage"]["allowedObjectTypes"]:
                reasons.append(f"lineage.nodes[{index}].objectType invalid")
            if not HEX64.fullmatch(str(node.get("schemaHash", ""))):
                reasons.append(f"lineage.nodes[{index}].schemaHash invalid")
            node_id = str(node.get("nodeId", ""))
            if node_id in node_ids:
                reasons.append(f"duplicate lineage nodeId: {node_id}")
            node_ids.add(node_id)
        edge_ids: set[str] = set()
        for index, edge in enumerate(edges):
            if not isinstance(edge, dict):
                reasons.append(f"lineage.edges[{index}] must be object")
                continue
            required(edge, policy["lineage"]["requiredEdgeFields"], f"lineage.edges[{index}]", reasons)
            if edge.get("operation") not in policy["lineage"]["allowedOperations"]:
                reasons.append(f"lineage.edges[{index}].operation invalid")
            if edge.get("sourceNodeId") not in node_ids or edge.get("targetNodeId") not in node_ids:
                reasons.append(f"lineage.edges[{index}] references unknown node")
            if not HEX64.fullmatch(str(edge.get("mappingHash", ""))):
                reasons.append(f"lineage.edges[{index}].mappingHash invalid")
            edge_id = str(edge.get("edgeId", ""))
            if edge_id in edge_ids:
                reasons.append(f"duplicate lineage edgeId: {edge_id}")
            edge_ids.add(edge_id)

    quality = evidence.get("quality")
    if not isinstance(quality, dict):
        reasons.append("quality must be object")
    else:
        if quality.get("contractVersion") != policy["quality"]["contractVersion"]:
            reasons.append("quality.contractVersion mismatch")
        rules = quality.get("rules")
        results = quality.get("results")
        if not isinstance(rules, list) or not rules:
            reasons.append("quality.rules must be non-empty")
            rules = []
        if not isinstance(results, list) or not results:
            reasons.append("quality.results must be non-empty")
            results = []
        rule_map: dict[str, dict[str, Any]] = {}
        for index, rule in enumerate(rules):
            if not isinstance(rule, dict):
                reasons.append(f"quality.rules[{index}] must be object")
                continue
            required(rule, policy["quality"]["requiredRuleFields"], f"quality.rules[{index}]", reasons)
            if rule.get("severity") not in policy["quality"]["severityLevels"]:
                reasons.append(f"quality.rules[{index}].severity invalid")
            if rule.get("thresholdType") not in policy["quality"]["allowedThresholdTypes"]:
                reasons.append(f"quality.rules[{index}].thresholdType invalid")
            rule_id = str(rule.get("ruleId", ""))
            if rule_id in rule_map:
                reasons.append(f"duplicate quality ruleId: {rule_id}")
            rule_map[rule_id] = rule
        result_ids: set[str] = set()
        for index, result in enumerate(results):
            if not isinstance(result, dict):
                reasons.append(f"quality.results[{index}] must be object")
                continue
            required(result, ["ruleId", "status", "invalidCount", "totalCount"], f"quality.results[{index}]", reasons)
            rule_id = str(result.get("ruleId", ""))
            if rule_id in result_ids:
                reasons.append(f"duplicate quality result ruleId: {rule_id}")
            result_ids.add(rule_id)
            rule = rule_map.get(rule_id)
            if rule is None:
                reasons.append(f"quality.results[{index}] references unknown rule")
                continue
            if result.get("status") not in {"PASS", "FAIL", "UNKNOWN"}:
                reasons.append(f"quality.results[{index}].status invalid")
            computed = threshold_passes(rule, result)
            if computed is None:
                reasons.append(f"quality.results[{index}] threshold evidence invalid")
            elif computed is not True or result.get("status") != "PASS":
                reasons.append(f"quality rule did not pass: {rule_id}")
        missing_results = sorted(set(rule_map) - result_ids)
        if missing_results:
            reasons.append(f"quality results missing for rules: {missing_results}")

    reconciliation = evidence.get("reconciliation")
    if not isinstance(reconciliation, dict):
        reasons.append("reconciliation must be object")
    else:
        if reconciliation.get("contractVersion") != policy["reconciliation"]["contractVersion"]:
            reasons.append("reconciliation.contractVersion mismatch")
        comparisons = reconciliation.get("comparisons")
        if not isinstance(comparisons, list) or not comparisons:
            reasons.append("reconciliation.comparisons must be non-empty")
            comparisons = []
        comparison_ids: set[str] = set()
        for index, comparison in enumerate(comparisons):
            if not isinstance(comparison, dict):
                reasons.append(f"reconciliation.comparisons[{index}] must be object")
                continue
            required(
                comparison,
                policy["reconciliation"]["requiredComparisonFields"],
                f"reconciliation.comparisons[{index}]",
                reasons,
            )
            comparison_id = str(comparison.get("comparisonId", ""))
            if comparison_id in comparison_ids:
                reasons.append(f"duplicate reconciliation comparisonId: {comparison_id}")
            comparison_ids.add(comparison_id)
            for field in ("leftHash", "rightHash"):
                if not HEX64.fullmatch(str(comparison.get(field, ""))):
                    reasons.append(f"reconciliation.comparisons[{index}].{field} invalid")
            for field in ("leftCount", "rightCount", "mismatchCount"):
                if not nonnegative_int(comparison.get(field)):
                    reasons.append(f"reconciliation.comparisons[{index}].{field} invalid")
            if (
                comparison.get("mismatchCount") != 0
                or comparison.get("leftCount") != comparison.get("rightCount")
                or comparison.get("leftHash") != comparison.get("rightHash")
            ):
                reasons.append(f"reconciliation comparison must match: {comparison_id}")

    return {
        "schemaVersion": 1,
        "status": "PASS" if not reasons else "FAIL",
        "vendor": evidence.get("vendor"),
        "operationId": evidence.get("operationId"),
        "reasons": reasons,
    }


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
            raise EvidenceError("expected evidence SHA-256 must be exact 64-hex")
        result = (
            {"schemaVersion": 1, "status": "FAIL", "reasons": [f"evidence sha256 mismatch expected={expected} actual={actual}"]}
            if actual != expected
            else evaluate(load(args.policy), load(args.evidence))
        )
        result["evidenceSha256"] = actual
        if args.output:
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(json.dumps(result, ensure_ascii=False, sort_keys=True))
        return 0 if result["status"] == "PASS" else 1
    except (EvidenceError, OSError, ValueError) as exc:
        print(f"CPF data observability evidence gate FAILED: {exc}", file=sys.stderr)
        return 3


if __name__ == "__main__":
    raise SystemExit(main())

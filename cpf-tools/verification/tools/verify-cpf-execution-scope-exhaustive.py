#!/usr/bin/env python3
"""Fail-closed exhaustive audit for a logical CPF execution-sequence range.

This gate joins Execution Sequence, Requirement Master, and Scenario Master and
produces one auditable row for every scoped requirement.  It deliberately keeps
metadata/traceability validation separate from runtime verification: a row may
only become COMPLETE when concrete source, consumer, positive/negative test,
and exact-HEAD evidence are all present.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import subprocess
import sys
from collections import Counter, defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

SHA_RE = re.compile(r"^(?:[0-9a-f]{40}|[0-9a-f]{64})$")
PLACEHOLDER_RE = re.compile(r"(?:TODO|TBD|<HEAD>|<SHA>|REPLACE_ME|YOUR_|추후|미정|나중에)", re.I)
EXECUTION_RE = re.compile(r"^(?:\d{2}-\d{8}|\d{2}-99999999)$")
REQ_RE = re.compile(r"^CPF-(?:FR-\d{6}|GATE-\d{2})$")
SC_RE = re.compile(r"^CPF-SC-\d{6}$")

REQ_REQUIRED = (
    "requirement", "priority", "owner_module", "source_basis", "change_target",
    "actual_consumer", "acceptance_criteria", "verification_method",
    "regression_protection", "error_handling", "concurrency_control",
    "retry_policy", "unknown_result_policy", "recovery_policy",
    "security_control", "audit_requirement", "db_vendor_impact", "api_contract",
    "frontend_contract", "completion_prohibited_when", "execution_phase_id",
    "execution_order", "work_package_id",
)
SCENARIO_REQUIRED = (
    "scenario_id", "linked_requirement_id", "scenario_type", "title",
    "preconditions", "steps", "expected_result", "failure_criteria",
    "environment", "topology", "required_evidence", "execution_phase_id",
    "work_package_id",
)

GROUP_SOURCE_MAP = {
    "ADM_UI": ["cpf-admin/frontend/src/app/routes.ts", "cpf-admin/frontend/src/generated/cpf-operation-contract.ts", "cpf-admin/frontend/src/features", "cpf-admin/src/main/java/com/cpf/admin", "cpf-admin/src/test"],
    "MBW_WEB": ["cpf-backoffice-web/frontend/src/router/index.ts", "cpf-backoffice-web/frontend/scripts/generate-reference-client.mjs", "cpf-backoffice-web/frontend/src/features", "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online", "cpf-backoffice/online/src/test"],
    "FRONTEND": ["cpf-admin/frontend", "cpf-backoffice-web/frontend"],
    "TEST": ["cpf-tools/testing", "cpf-tools/verification"],
    "QUALITY": ["cpf-tools/testing", "cpf-tools/verification", "cpf-docs/work/qa"],
    "RELEASE": ["build.gradle", "settings.gradle", "cpf-tools/release", "deploy"],
    "SECURITY": ["cpf-core/src/main/java/com/cpf/core/api/security", "cpf-core/src/main/java/com/cpf/core/common", "cpf-admin/src/main/java/com/cpf/admin", "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online"],
    "PRODUCT": ["cpf-core", "cpf-common", "cpf-admin", "cpf-backoffice/online", "cpf-batch", "cpf-tools/generator"],
    "BATCH": ["cpf-batch", "cpf-admin/frontend/src/features/batch", "cpf-tools/db/vendor"],
    "DOC": ["cpf-docs"],
    "CORE": ["cpf-core/src/main/java/com/cpf/core/api", "cpf-core/src/main/java/com/cpf/core/spi", "cpf-core/src/main/java/com/cpf/core/internal"],
    "OPS": ["cpf-admin/src/main/java/com/cpf/admin/opr", "cpf-admin/frontend/src/features/operations"],
    "MESSAGING": ["cpf-core/src/main/java/com/cpf/core/api/broker", "cpf-starters/integration"],
    "MESSAGING_PROVIDER": ["cpf-starters/integration", "cpf-tools/scripts"],
    "STARTER": ["cpf-starters", "cpf-tools/generator/contracts/cpf-starter-catalog.json", "settings.gradle"],
    "GATEWAY": ["cpf-gateway", "cpf-admin/frontend/src/features/gateway-operations"],
    "EXTERNAL": ["cpf-education", "cpf-core/src/main/java/com/cpf/core/api/servicecall"],
    "RELIABILITY": ["cpf-core/src/main/java/com/cpf/core/api/reliability", "cpf-core/src/main/java/com/cpf/core/api/resilience", "cpf-admin/frontend/src/features/reliability", "cpf-admin/frontend/src/features/recovery-center"],
    "INTEGRATION": ["cpf-core/src/main/java/com/cpf/core/api/http", "cpf-core/src/main/java/com/cpf/core/api/servicecall", "cpf-starters/integration"],
    "OBSERVABILITY": ["cpf-core/src/main/java/com/cpf/core/api/observability", "cpf-core/src/main/java/com/cpf/core/api/logging", "cpf-admin/frontend/src/features/logs"],
    "COMMON": ["cpf-common", "cpf-core/src/main/java/com/cpf/core/api"],
    "RUNTIME_CONTROL": ["cpf-core/src/main/java/com/cpf/core/api/runtimecontrol", "cpf-admin/src/main/java/com/cpf/admin/opr", "cpf-starters"],
    "FILE": ["cpf-core/src/main/java/com/cpf/core/api/filetransfer", "cpf-core/src/main/java/com/cpf/core/api/attachment", "cpf-admin/frontend/src/features/file-jobs"],
    "API": ["cpf-core/src/main/java/com/cpf/core/api", "cpf-admin/frontend/openapi", "cpf-backoffice/online/openapi"],
    "GOV": ["cpf-docs/governance", "cpf-docs/work/current"],
    # Current modernization governance groups are canonical Requirement Master values, not aliases.
    # Keep them explicit so the exhaustive gate cannot false-fail after Requirement currentization.
    "DOCUMENTATION GOVERNANCE": ["cpf-docs/governance", "cpf-docs/work/current"],
    "REPOSITORY HYGIENE": ["cpf-tools/verification", "cpf-docs/work/GARBAGE_SWEEP_DECISIONS.csv", "cpf-docs/deliverables/DELETE_MANIFEST.csv"],
    "EVIDENCE": ["cpf-tools/verification", "cpf-docs/work/evidence"],
    "QA GOVERNANCE": ["cpf-tools/testing", "cpf-tools/verification", "cpf-docs/work/current"],
    "HANDOVER": ["cpf-docs/work", "cpf-docs/deliverables"],
    "EXECUTION GOVERNANCE": ["cpf-tools/verification/tools", "cpf-docs/work/current"],
}

class AuditError(RuntimeError):
    pass


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    if not path.is_file():
        raise AuditError(f"missing CSV: {path}")
    with path.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        fields = list(reader.fieldnames or [])
        return fields, [{k: (v or "").strip() for k, v in row.items()} for row in reader]


def discover_parts(root: Path, stem: str) -> list[Path]:
    parts = root / "cpf-docs" / "work" / "current" / f"{stem}.parts"
    files = sorted(parts.glob("*.csv"))
    if not files:
        raise AuditError(f"no split parts: {parts}")
    return files


def load_parts(files: Iterable[Path], id_field: str) -> tuple[list[dict[str, str]], dict[str, str]]:
    rows: list[dict[str, str]] = []
    hashes: dict[str, str] = {}
    ids: set[str] = set()
    for path in files:
        _, part = read_csv(path)
        if not part:
            raise AuditError(f"empty split part: {path}")
        for row in part:
            value = row.get(id_field, "")
            if not value or value in ids:
                raise AuditError(f"missing/duplicate {id_field}: {value!r} in {path}")
            ids.add(value)
            rows.append(row)
        hashes[path.as_posix()] = hashlib.sha256(path.read_bytes()).hexdigest()
    return rows, hashes


def git_head(root: Path) -> str:
    p = subprocess.run(["git", "-C", str(root), "rev-parse", "HEAD"], text=True, capture_output=True)
    if p.returncode:
        raise AuditError("git HEAD unavailable: " + p.stderr.strip())
    return p.stdout.strip()


def path_state(root: Path, rel: str) -> str:
    p = root / rel
    return "FILE" if p.is_file() else "DIR" if p.is_dir() else "MISSING"


def split_ops(text: str) -> set[str]:
    m = re.search(r"export\s+type\s+CpfOperationId\s*=\s*(.*?);", text, re.S)
    return set(re.findall(r'"([^"]+)"', m.group(1))) if m else set()


def parse_route_contract(path: Path, mode: str) -> tuple[set[str], set[str], set[str]]:
    if mode == "adm":
        texts = [path.read_text(encoding="utf-8")]
        route_dir = path.parent / "routes"
        if route_dir.is_dir():
            texts.extend(p.read_text(encoding="utf-8") for p in sorted(route_dir.glob("*.ts")) if p.name != "types.ts")
        text = "\n".join(texts)
        route_ids = set(re.findall(r'routeId:\s*"([^"]+)"', text))
        components = set(re.findall(r'import\("(?:\.\./|\.\./\.\./)features/([^"]+)"\)', text))
        expected = set()
        for block in re.findall(r"expectedOperationIds:\s*\[([^\]]*)\]", text, re.S):
            expected.update(re.findall(r'"([^"]+)"', block))
    else:
        text = path.read_text(encoding="utf-8")
        route_ids = set(re.findall(r"\bpath\s*:\s*['\"]([^'\"]+)['\"]", text))
        components = set(re.findall(r"from\s+['\"]\.\./features/([^'\"]+)['\"]", text))
        openapi = path.parents[2] / "openapi/cpf-openapi.json"
        if not openapi.is_file():
            raise AuditError(f"Backoffice OpenAPI source missing: {openapi}")
        document = json.loads(openapi.read_text(encoding="utf-8"))
        expected = {
            str(operation.get("operationId"))
            for item in (document.get("paths") or {}).values()
            if isinstance(item, dict)
            for operation in item.values()
            if isinstance(operation, dict) and operation.get("operationId")
        }
    if not route_ids or not components or not expected:
        raise AuditError(f"route contract is sparse or unparsable: {path}")
    return route_ids, components, expected


def canonical_order_key(value: str) -> tuple[int, int]:
    if not EXECUTION_RE.fullmatch(value):
        raise AuditError(f"invalid execution order: {value}")
    phase, seq = value.split("-", 1)
    return int(phase), int(seq)


def verify(args: argparse.Namespace) -> dict:
    root = Path(args.root).resolve()
    expected_sha = args.expected_sha
    if not SHA_RE.fullmatch(expected_sha):
        raise AuditError("expected source identity must be exactly 40 or 64 lowercase hex characters")
    actual_sha = args.source_head or git_head(root)
    if actual_sha != expected_sha:
        raise AuditError(f"HEAD mismatch expected={expected_sha} actual={actual_sha}")

    execution, execution_hashes = load_parts(discover_parts(root, "CPF_EXECUTION_SEQUENCE"), "execution_order")
    requirements, requirement_hashes = load_parts(discover_parts(root, "CPF_REQUIREMENT_MASTER"), "requirement_id")
    scenarios, scenario_hashes = load_parts(discover_parts(root, "CPF_SCENARIO_MASTER"), "scenario_id")
    if args.expected_total_execution is not None and len(execution) != args.expected_total_execution:
        raise AuditError(f"total execution count mismatch: expected={args.expected_total_execution} actual={len(execution)}")
    scope = execution[args.start_row - 1:]
    expected_scope = args.expected_scope if args.expected_scope is not None else len(scope)
    if len(scope) != expected_scope:
        raise AuditError(f"scope count mismatch: expected={expected_scope} actual={len(scope)}")

    requirement_by_id = {r["requirement_id"]: r for r in requirements}
    scenario_by_req: dict[str, list[dict[str, str]]] = defaultdict(list)
    for scenario in scenarios:
        if not SC_RE.fullmatch(scenario["scenario_id"]):
            raise AuditError(f"invalid scenario id: {scenario['scenario_id']}")
        scenario_by_req[scenario["linked_requirement_id"]].append(scenario)

    # Sequence order must be strictly ascending and work-package rows contiguous.
    keys = [canonical_order_key(r["execution_order"]) for r in scope]
    if any(a >= b for a, b in zip(keys, keys[1:])):
        raise AuditError("scope execution order is not strictly ascending")
    wp_positions: dict[str, list[int]] = defaultdict(list)
    for index, row in enumerate(scope):
        wp_positions[row["work_package_id"]].append(index)
    non_contiguous = [wp for wp, pos in wp_positions.items() if pos != list(range(min(pos), max(pos) + 1))]
    if non_contiguous:
        raise AuditError(f"non-contiguous work packages: {non_contiguous[:10]}")

    # Route/OpenAPI contracts are product consumer evidence for the UI phases.
    adm_route = root / "cpf-admin/frontend/src/app/routes.ts"
    bza_route = root / "cpf-backoffice-web/frontend/src/router/index.ts"
    adm_contract = root / "cpf-admin/frontend/src/generated/cpf-operation-contract.ts"
    bza_contract = root / "cpf-backoffice/online/openapi/cpf-openapi.json"
    adm_route_ids, adm_components, adm_expected = parse_route_contract(adm_route, "adm")
    bza_route_ids, bza_components, bza_expected = parse_route_contract(bza_route, "bza")
    adm_ops = split_ops(adm_contract.read_text(encoding="utf-8"))
    bza_spec = json.loads(bza_contract.read_text(encoding="utf-8"))
    bza_ops = {op.get("operationId") for item in bza_spec.get("paths", {}).values() for op in item.values() if isinstance(op, dict) and op.get("operationId")}
    missing_adm_ops = sorted(adm_expected - adm_ops)
    missing_bza_ops = sorted(bza_expected - bza_ops)
    if missing_adm_ops or missing_bza_ops:
        raise AuditError(f"route operationId drift: adm={missing_adm_ops[:10]} bza={missing_bza_ops[:10]}")

    audit_rows: list[dict[str, str]] = []
    counters = Counter()
    seen_req: set[str] = set()
    expected_wp = Counter(r["work_package_id"] for r in scope)
    for seq_row in scope:
        rid = seq_row["requirement_id"]
        if not REQ_RE.fullmatch(rid):
            raise AuditError(f"invalid requirement id in scope: {rid}")
        if rid in seen_req:
            raise AuditError(f"duplicate scoped requirement: {rid}")
        seen_req.add(rid)
        req = requirement_by_id.get(rid)
        if req is None:
            raise AuditError(f"execution row has no Requirement Master row: {rid}")
        mismatch = [name for name in ("execution_order", "work_package_id", "owner_module", "requirement_group", "capability", "feature", "function_type") if req.get(name, "") != seq_row.get(name, "")]
        if mismatch:
            raise AuditError(f"{rid}: execution/master mismatch {mismatch}")
        missing_req = [name for name in REQ_REQUIRED if not req.get(name, "")]
        if missing_req:
            raise AuditError(f"{rid}: missing required requirement fields {missing_req}")
        if PLACEHOLDER_RE.search("\n".join(req.get(name, "") for name in REQ_REQUIRED)):
            raise AuditError(f"{rid}: placeholder in requirement contract")
        linked = scenario_by_req.get(rid, [])
        if not linked:
            raise AuditError(f"{rid}: no linked scenario")
        scenario_types: set[str] = set()
        scenario_ids: list[str] = []
        scenario_error = ""
        for sc in linked:
            missing_sc = [name for name in SCENARIO_REQUIRED if not sc.get(name, "")]
            if missing_sc:
                scenario_error = f"{sc['scenario_id']}: missing {','.join(missing_sc)}"
                break
            if sc["work_package_id"] != seq_row["work_package_id"] or sc["execution_phase_id"] != req["execution_phase_id"]:
                scenario_error = f"{sc['scenario_id']}: phase/work-package mismatch"
                break
            if sc["scenario_type"] == "DIRECT_VERIFICATION":
                expected_title = f"[{rid}] {req['feature']} / {req['function_type']} 직접 검증"
                if sc["title"] != expected_title:
                    scenario_error = f"{sc['scenario_id']}: direct-verification requirement/title mismatch"
                    break
            scenario_types.add(sc["scenario_type"])
            scenario_ids.append(sc["scenario_id"])
        if scenario_error:
            raise AuditError(f"{rid}: {scenario_error}")

        group = req["requirement_group"]
        source_candidates = GROUP_SOURCE_MAP.get(group, [])
        if not source_candidates:
            raise AuditError(f"{rid}: no source map for requirement group {group}")
        states = {rel: path_state(root, rel) for rel in source_candidates}
        existing = [rel for rel, state in states.items() if state != "MISSING"]
        missing = [rel for rel, state in states.items() if state == "MISSING"]
        source_trace = "PASS" if existing else "FAIL"
        # Missing optional candidates do not fail a row when at least one canonical owner path exists,
        # but the full list is preserved for work-package review.
        consumer_trace = "PASS" if req["actual_consumer"] and req["acceptance_criteria"] else "FAIL"
        ui_contract = "N/A"
        if group == "ADM_UI":
            ui_contract = "PASS" if adm_route_ids and adm_components and adm_expected else "FAIL"
        elif group == "MBW_WEB":
            ui_contract = "PASS" if bza_route_ids and bza_components and bza_expected else "FAIL"
        metadata_status = "PASS"
        scenario_status = "PASS"
        # This gate executes structural/contract validation only. Product runtime remains separate.
        runtime_status = "NOT_EXECUTED"
        completion = "미완료"
        verification_status = "미검증"
        incomplete_reason = "Requirement·Scenario·Source owner·UI/OpenAPI contract 전수 대조 완료; 해당 Requirement의 실제 Unit/Integration/Browser/DB/Fault Runtime 결과가 exact HEAD로 직접 연결되지 않음"
        counters["metadata_pass"] += 1
        counters["scenario_pass"] += 1
        counters["source_trace_pass" if source_trace == "PASS" else "source_trace_fail"] += 1
        counters["consumer_trace_pass" if consumer_trace == "PASS" else "consumer_trace_fail"] += 1
        counters["ui_contract_pass"] += int(ui_contract == "PASS")
        counters["runtime_not_executed"] += 1
        audit_rows.append({
            "execution_order": seq_row["execution_order"],
            "requirement_id": rid,
            "work_package_id": seq_row["work_package_id"],
            "phase_id": seq_row["phase_id"],
            "requirement_group": group,
            "capability": req["capability"],
            "feature": req["feature"],
            "function_type": req["function_type"],
            "owner_module": req["owner_module"],
            "actual_consumer": req["actual_consumer"],
            "metadata_status": metadata_status,
            "scenario_status": scenario_status,
            "scenario_count": str(len(linked)),
            "scenario_types": ";".join(sorted(scenario_types)),
            "scenario_ids": ";".join(scenario_ids),
            "source_trace_status": source_trace,
            "source_evidence_paths": ";".join(existing),
            "missing_optional_source_candidates": ";".join(missing),
            "consumer_trace_status": consumer_trace,
            "ui_openapi_contract_status": ui_contract,
            "runtime_verification_status": runtime_status,
            "development_status": completion,
            "verification_status": verification_status,
            "개발GPT_상태": completion,
            "개발GPT_수행여부": "예",
            "개발GPT_수행내용": "실행순서·Requirement Master·Scenario Master·Owner Source·Consumer·UI/OpenAPI 계약 전수 대조",
            "개발GPT_미완료사유": incomplete_reason,
            "개발GPT_실행및검증": "verify-cpf-execution-scope-exhaustive.py exact-SHA structural/contract audit",
            "개발GPT_evidence": args.audit_csv,
            "개발GPT_자체검수여부": "예",
            "개발GPT_자체검수상태": completion,
            "개발GPT_자체검수결과": "구조·Traceability PASS, Requirement별 실제 Runtime Evidence 미연결",
            "baseline_sha": actual_sha,
        })

    if len(audit_rows) != expected_scope or len(seen_req) != expected_scope:
        raise AuditError("audit row count or uniqueness mismatch")
    actual_wp = Counter(r["work_package_id"] for r in audit_rows)
    if actual_wp != expected_wp:
        raise AuditError("work-package audit aggregation mismatch")

    audit_path = Path(args.audit_csv)
    audit_path = audit_path if audit_path.is_absolute() else root / audit_path
    audit_path.parent.mkdir(parents=True, exist_ok=True)
    with audit_path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=list(audit_rows[0]))
        writer.writeheader(); writer.writerows(audit_rows)

    wp_path = Path(args.work_package_csv)
    wp_path = wp_path if wp_path.is_absolute() else root / wp_path
    wp_path.parent.mkdir(parents=True, exist_ok=True)
    grouped: dict[str, list[dict[str, str]]] = defaultdict(list)
    for row in audit_rows:
        grouped[row["work_package_id"]].append(row)
    wp_rows = []
    for wp, rows in sorted(grouped.items(), key=lambda item: canonical_order_key(item[1][0]["execution_order"])):
        wp_rows.append({
            "work_package_id": wp,
            "first_execution_order": rows[0]["execution_order"],
            "last_execution_order": rows[-1]["execution_order"],
            "requirement_count": str(len(rows)),
            "metadata_pass": str(sum(r["metadata_status"] == "PASS" for r in rows)),
            "scenario_pass": str(sum(r["scenario_status"] == "PASS" for r in rows)),
            "source_trace_pass": str(sum(r["source_trace_status"] == "PASS" for r in rows)),
            "runtime_verified": str(sum(r["runtime_verification_status"] == "PASS" for r in rows)),
            "completed": str(sum(r["개발GPT_상태"] == "완료" for r in rows)),
            "incomplete": str(sum(r["개발GPT_상태"] == "미완료" for r in rows)),
            "baseline_sha": actual_sha,
        })
    with wp_path.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=list(wp_rows[0]))
        writer.writeheader(); writer.writerows(wp_rows)

    return {
        "status": "PASS",
        "verifiedAgainstSha": actual_sha,
        "totalExecutionRows": len(execution),
        "scopeStartRow": args.start_row,
        "scopeRows": len(audit_rows),
        "firstExecutionOrder": audit_rows[0]["execution_order"],
        "lastExecutionOrder": audit_rows[-1]["execution_order"],
        "workPackages": len(wp_rows),
        "linkedScenarios": sum(int(r["scenario_count"]) for r in audit_rows),
        "routeContracts": {
            "admRoutes": len(adm_route_ids), "admComponents": len(adm_components), "admExpectedOperations": len(adm_expected), "admGeneratedOperations": len(adm_ops),
            "bzaRoutes": len(bza_route_ids), "bzaComponents": len(bza_components), "bzaExpectedOperations": len(bza_expected), "bzaGeneratedOperations": len(bza_ops),
        },
        "counts": dict(counters),
        "requirementStatus": {"complete": 0, "incomplete": len(audit_rows), "runtimeUnverified": len(audit_rows)},
        "inputPartHashes": {**execution_hashes, **requirement_hashes, **scenario_hashes},
        "auditCsv": audit_path.relative_to(root).as_posix(),
        "workPackageCsv": wp_path.relative_to(root).as_posix(),
    }


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--root", default=".")
    p.add_argument("--expected-sha", required=True)
    p.add_argument("--source-head", help="Use an exact remote source SHA when the audit root is an exact fetched snapshot rather than a Git checkout")
    p.add_argument("--start-row", type=int, default=20001)
    p.add_argument("--expected-total-execution", type=int)
    p.add_argument("--expected-scope", type=int)
    p.add_argument("--audit-csv", required=True)
    p.add_argument("--work-package-csv", required=True)
    p.add_argument("--json-output")
    args = p.parse_args()
    try:
        result = verify(args); code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}; code = 1
    text = json.dumps(result, ensure_ascii=False, indent=2)
    if args.json_output:
        out = Path(args.json_output)
        out = out if out.is_absolute() else Path(args.root).resolve() / out
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(text + "\n", encoding="utf-8")
    print(text)
    return code

if __name__ == "__main__":
    raise SystemExit(main())

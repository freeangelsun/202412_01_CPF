#!/usr/bin/env python3
from __future__ import annotations
from pathlib import Path
import csv, hashlib, json, os, re

DEFAULT_H = Path(__file__).resolve().parents[1]
H = Path(os.environ.get("CPF_HARNESS_ROOT", str(DEFAULT_H))).resolve()
ROOT = Path(os.environ.get("CPF_REPOSITORY_ROOT", str(H.parents[2]))).resolve()
REGISTRY = H / "current/CURRENT_WORK_ITEM_REGISTRY.csv"
HARNESS = H / "CPF_DEVELOPMENT_HARNESS.md"
MERGE_STATE = H / "current/CURRENT_MERGE_CONTROL_STATE.json"
SESSIONS = H / "evidence"
ALLOWED_ROLE = {"DEVGPT", "CLAUDE", "CODEX", "QA", "HARNESS"}
ALLOWED_MERGE = {"UNMERGED", "PARTIAL", "MERGED", "CONFLICT", "REJECTED"}
REQUIRED_TOP = {
    "schemaVersion", "sessionKey", "role", "startedAt", "endedAt", "sourceIdentity",
    "sourceBasis", "registrySha256AtStart", "reportPath", "reportSha256", "workItems",
    "evidenceFiles", "gitWriteExecuted", "mergeStatus", "pendingReasons", "conflicts", "rerunConditions"
}
REQUIRED_WI = {"workItemId", "proposedStatus", "evidence", "acceptanceMapping", "reportAnchor"}
REQUIRED_ACCEPTANCE_ENV = {"prerequisiteSource", "requiredEnvironment", "actualEnvironment"}
CLOSED_LIKE = {"CLOSED", "PASS"}


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load_registry_ids() -> set[str]:
    with REGISTRY.open(encoding="utf-8-sig", newline="") as f:
        return {r["work_item_id"].strip() for r in csv.DictReader(f)}


def rel_path(value: str) -> Path:
    p = (ROOT / value).resolve()
    try:
        p.relative_to(ROOT)
    except ValueError as e:
        raise ValueError(f"path_escape:{value}") from e
    return p


def parse_merge_state() -> dict[str, str]:
    # Mutable Merge Control must not live in canonical Harness source bytes: doing so makes
    # Source Identity currentization circular (state update -> source hash update -> state update).
    # The canonical Harness owns the rule; current/ owns only the volatile state projection.
    if not MERGE_STATE.is_file():
        return {}
    try:
        data = json.loads(MERGE_STATE.read_text(encoding="utf-8"))
    except Exception:
        return {}
    return {str(k): str(v) for k, v in data.items()}


def csv_set(value: str) -> set[str]:
    v = value.strip()
    if not v or v == "NONE":
        return set()
    return {x.strip() for x in v.split(",") if x.strip()}


def main() -> int:
    failures: list[str] = []
    registry_ids = load_registry_ids()
    discovered: dict[str, str] = {}
    session_dirs = sorted(p for p in SESSIONS.glob("*/current/sessions/*") if p.is_dir())
    for d in session_dirs:
        key = d.name
        mf = d / "SESSION_MANIFEST.json"
        if not mf.is_file():
            failures.append(f"MANIFEST_MISSING:{key}")
            discovered[key] = "UNMERGED"
            continue
        try:
            data = json.loads(mf.read_text(encoding="utf-8"))
        except Exception as e:
            failures.append(f"MANIFEST_INVALID_JSON:{key}:{e}")
            discovered[key] = "CONFLICT"
            continue
        missing = sorted(REQUIRED_TOP - set(data))
        if missing:
            failures.append(f"MANIFEST_SCHEMA_MISSING:{key}:{','.join(missing)}")
        if data.get("sessionKey") != key:
            failures.append(f"MANIFEST_SESSION_KEY_MISMATCH:{key}")
        role = str(data.get("role", "")).upper()
        if role not in ALLOWED_ROLE:
            failures.append(f"MANIFEST_ROLE_INVALID:{key}:{role}")
        status = str(data.get("mergeStatus", ""))
        if status not in ALLOWED_MERGE:
            failures.append(f"MANIFEST_MERGE_STATUS_INVALID:{key}:{status}")
        discovered[key] = status if status in ALLOWED_MERGE else "CONFLICT"

        report_value = str(data.get("reportPath", ""))
        try:
            report = rel_path(report_value)
        except Exception as e:
            failures.append(f"MANIFEST_REPORT_PATH_INVALID:{key}:{e}")
            report = None
        report_text = ""
        if report is None or not report.is_file():
            failures.append(f"MANIFEST_REPORT_MISSING:{key}:{report_value}")
        else:
            report_text = report.read_text(encoding="utf-8", errors="replace")
            if data.get("reportSha256") != sha(report):
                failures.append(f"MANIFEST_REPORT_SHA_MISMATCH:{key}")

        evidence_files = data.get("evidenceFiles") if isinstance(data.get("evidenceFiles"), list) else []
        evidence_index: set[str] = set()
        for ev in evidence_files:
            if not isinstance(ev, dict) or not ev.get("path") or not ev.get("sha256"):
                failures.append(f"MANIFEST_EVIDENCE_SCHEMA_INVALID:{key}")
                continue
            ev_path = str(ev["path"])
            evidence_index.add(ev_path)
            try:
                ep = rel_path(ev_path)
            except Exception as e:
                failures.append(f"MANIFEST_EVIDENCE_PATH_INVALID:{key}:{e}")
                continue
            if not ep.is_file():
                failures.append(f"MANIFEST_EVIDENCE_MISSING:{key}:{ev_path}")
            elif sha(ep) != str(ev["sha256"]):
                failures.append(f"MANIFEST_EVIDENCE_SHA_MISMATCH:{key}:{ev_path}")

        wis = data.get("workItems") if isinstance(data.get("workItems"), list) else []
        if not wis:
            failures.append(f"MANIFEST_WORK_ITEMS_EMPTY:{key}")
        seen: set[str] = set()
        anchors: set[str] = set()
        for wi in wis:
            if not isinstance(wi, dict):
                failures.append(f"MANIFEST_WORK_ITEM_INVALID:{key}")
                continue
            wm = REQUIRED_WI - set(wi)
            if wm:
                failures.append(f"MANIFEST_WORK_ITEM_SCHEMA_MISSING:{key}:{','.join(sorted(wm))}")
            wid = str(wi.get("workItemId", "")).strip()
            if not wid:
                failures.append(f"MANIFEST_WORK_ITEM_ID_EMPTY:{key}")
            if wid in seen:
                failures.append(f"MANIFEST_BULK_DUPLICATE_WORK_ITEM:{key}:{wid}")
            seen.add(wid)
            if wid not in registry_ids:
                failures.append(f"MANIFEST_UNKNOWN_WORK_ITEM:{key}:{wid}")

            evidence = wi.get("evidence")
            if not isinstance(evidence, list) or not evidence:
                failures.append(f"MANIFEST_WORK_ITEM_EVIDENCE_EMPTY:{key}:{wid}")
            else:
                for ev_ref in evidence:
                    if str(ev_ref) not in evidence_index:
                        failures.append(f"MANIFEST_WORK_ITEM_EVIDENCE_UNDECLARED:{key}:{wid}:{ev_ref}")

            mapping = wi.get("acceptanceMapping")
            if not isinstance(mapping, dict) or not mapping:
                failures.append(f"MANIFEST_WORK_ITEM_ACCEPTANCE_EMPTY:{key}:{wid}")
            else:
                missing_env = sorted(REQUIRED_ACCEPTANCE_ENV - set(mapping))
                if missing_env:
                    failures.append(f"MANIFEST_WORK_ITEM_PREREQUISITE_MISSING:{key}:{wid}:{','.join(missing_env)}")
                proposed = str(wi.get("proposedStatus", "")).upper()
                if proposed in CLOSED_LIKE:
                    review = mapping.get("finalSelfReview")
                    if not isinstance(review, dict) or review.get("complete") is not True:
                        failures.append(f"MANIFEST_FINAL_SELF_REVIEW_INCOMPLETE:{key}:{wid}")

            anchor = str(wi.get("reportAnchor", "")).strip()
            if not anchor or anchor in anchors:
                failures.append(f"MANIFEST_WORK_ITEM_ANCHOR_INVALID:{key}:{wid}")
            anchors.add(anchor)
            if report_text and anchor and report_text.count(anchor) != 1:
                failures.append(f"MANIFEST_WORK_ITEM_ANCHOR_CARDINALITY:{key}:{wid}:{report_text.count(anchor)}")

        if status == "MERGED":
            for f in ("mergedBySessionKey", "mergedAt", "mergeTargetSourceIdentity"):
                if not str(data.get(f, "")).strip():
                    failures.append(f"MANIFEST_MERGED_FIELD_MISSING:{key}:{f}")
        if status in {"UNMERGED", "PARTIAL"} and not data.get("pendingReasons"):
            failures.append(f"MANIFEST_PENDING_REASON_MISSING:{key}")
        if status == "CONFLICT" and not data.get("conflicts"):
            failures.append(f"MANIFEST_CONFLICT_REASON_MISSING:{key}")

    state = parse_merge_state()
    required_state = {
        "merge_protocol_version", "merge_baseline_source_identity", "last_merged_session_key",
        "merged_session_set_digest", "pending_session_keys", "conflict_session_keys",
        "last_merge_review_at", "last_merge_reviewer_session_key"
    }
    for field in sorted(required_state - set(state)):
        failures.append(f"MERGE_CONTROL_FIELD_MISSING:{field}")
    placeholders = {"CURRENTIZE_REQUIRED", "DISCOVERY_REQUIRED", "NOT_INITIALIZED"}
    for field, value in state.items():
        if value in placeholders:
            failures.append(f"MERGE_CONTROL_NOT_CURRENT:{field}:{value}")

    actual_pending = {k for k, v in discovered.items() if v in {"UNMERGED", "PARTIAL"}}
    actual_conflicts = {k for k, v in discovered.items() if v == "CONFLICT"}
    if csv_set(state.get("pending_session_keys", "")) != actual_pending:
        failures.append("MERGE_CONTROL_PENDING_MISMATCH")
    if csv_set(state.get("conflict_session_keys", "")) != actual_conflicts:
        failures.append("MERGE_CONTROL_CONFLICT_MISMATCH")
    merged = sorted(k for k, v in discovered.items() if v == "MERGED")
    digest = hashlib.sha256("\n".join(merged).encode("utf-8")).hexdigest()
    if state.get("merged_session_set_digest") != digest:
        failures.append("MERGE_CONTROL_DIGEST_MISMATCH")

    if failures:
        print("SESSION_MERGE_PROTOCOL=FAIL errors=" + str(len(failures)))
        for x in failures:
            print(x)
        return 1
    print(
        f"SESSION_MERGE_PROTOCOL=PASS sessions={len(discovered)} merged={len(merged)} "
        f"pending={len(actual_pending)} conflicts={len(actual_conflicts)}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

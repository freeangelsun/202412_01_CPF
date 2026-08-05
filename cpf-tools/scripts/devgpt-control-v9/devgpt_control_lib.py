#!/usr/bin/env python3
"""Shared library for CPF DevGPT Control V9.

No third-party dependency is required. All writes are deterministic UTF-8 CSV/JSON.
The library intentionally manages only Development GPT-owned state. QA and Codex
state remain in their own canonical QA ledger and are consumed only as reopen input.
"""
from __future__ import annotations

import csv
import hashlib
import json
import re
from collections import Counter, defaultdict, deque
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, Iterator, List, Mapping, MutableMapping, Sequence, Tuple

ACTIVE_TARGET_STATES = {"작업 대상", "재개발 대상", "재검수 대상"}
SKIP_TARGET_STATES = {"완료 스킵", "해당 없음 스킵"}
VALID_TARGET_STATES = ACTIVE_TARGET_STATES | SKIP_TARGET_STATES | {"소유권 검토", "외부환경 차단"}
VALID_DEV_STATES = {"완료", "미완료", "재개발 요청", "재검수 요청", "해당 없음"}

REQ_ID_ALIASES = ("requirement_id", "id", "requirementId")
SCENARIO_ID_ALIASES = ("scenario_id", "id", "scenarioId")
CANONICAL_ID_ALIASES = ("canonical_requirement_id", "canonical_requirement_ids", "canonical_id", "canonicalRequirementId")
SCENARIO_REQUIREMENT_ALIASES = ("requirement_id", "linked_requirement_id", "parent_requirement_id", "derived_requirement_id")
WORK_ITEM_ALIASES = ("primary_work_item_id", "primary_entity_id", "work_item_id", "development_work_item_id", "work_package_id")
TEXT_FIELDS = (
    "requirement", "requirement_text", "title", "description", "source_basis", "change_target",
    "actual_consumer", "acceptance_criteria", "verification_method", "regression_protection",
    "scenario", "scenario_title", "precondition", "steps", "expected_result", "failure_criteria",
    "owner_module", "owner_package", "category", "scenario_class", "gate_id", "tags"
)

TOKEN_RE = re.compile(r"[A-Za-z][A-Za-z0-9_.:/-]{2,}|[가-힣]{2,}")
STOP_TOKENS = {
    "한다", "해야", "대한", "통해", "위한", "기준", "실제", "검증", "구현", "확인", "관리",
    "지원", "기능", "경우", "정의", "적용", "결과", "사용", "현재", "전체", "요구", "작업",
    "requirement", "scenario", "system", "actual", "result", "support", "required", "current"
}

class ManagementError(RuntimeError):
    pass


def read_csv(path: Path) -> List[dict]:
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def write_csv(path: Path, rows: Sequence[Mapping[str, object]], fieldnames: Sequence[str] | None = None) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if fieldnames is None:
        if not rows:
            raise ManagementError(f"fieldnames required for empty CSV: {path}")
        fieldnames = list(rows[0].keys())
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(fieldnames), extrasaction="ignore")
        writer.writeheader()
        for row in rows:
            writer.writerow({key: row.get(key, "") for key in fieldnames})


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def first_value(row: Mapping[str, str], aliases: Sequence[str], *, required: bool = False, label: str = "field") -> str:
    for key in aliases:
        value = (row.get(key) or "").strip()
        if value:
            return value
    if required:
        raise ManagementError(f"missing {label}; accepted columns={aliases}; row keys={list(row.keys())}")
    return ""


def split_values(value: str) -> List[str]:
    if not value:
        return []
    return [part.strip() for part in re.split(r"[;,|\n]+", value) if part.strip()]


def split_identifier_values(value: str) -> List[str]:
    """Split identifier lists without changing generic path/value splitting semantics."""
    if not value:
        return []
    return [part.strip() for part in re.split(r"[;,|/\n]+", value) if part.strip()]


def normalized_tokens(value: str) -> set[str]:
    return {token.lower() for token in TOKEN_RE.findall(value or "") if token.lower() not in STOP_TOKENS}


def row_text(row: Mapping[str, str]) -> str:
    values = [(row.get(field) or "") for field in TEXT_FIELDS]
    if not any(values):
        values = [v for v in row.values() if isinstance(v, str)]
    return " ".join(values)


def read_split_index(index_path: Path, repo_root: Path) -> Tuple[List[dict], dict]:
    if not index_path.exists():
        raise ManagementError(f"split index not found: {index_path}")
    index_rows = read_csv(index_path)
    required = {"part_path", "part_record_count", "sha256", "part_sequence"}
    if not index_rows or not required.issubset(index_rows[0]):
        raise ManagementError(f"invalid split index schema: {index_path}")
    rows: List[dict] = []
    header: List[str] | None = None
    part_results = []
    for meta in sorted(index_rows, key=lambda r: int(r["part_sequence"])):
        part_path = repo_root / meta["part_path"]
        if not part_path.exists():
            raise ManagementError(f"split part missing: {part_path}")
        actual_sha = sha256_file(part_path)
        expected_sha = meta["sha256"].strip().lower()
        if expected_sha and actual_sha != expected_sha:
            raise ManagementError(f"split part SHA mismatch: {part_path}; expected={expected_sha}; actual={actual_sha}")
        with part_path.open("r", encoding="utf-8-sig", newline="") as handle:
            reader = csv.DictReader(handle)
            current_header = reader.fieldnames or []
            if header is None:
                header = current_header
            elif current_header != header:
                raise ManagementError(f"split header mismatch: {part_path}")
            part_rows = list(reader)
        expected_count = int(meta["part_record_count"])
        if len(part_rows) != expected_count:
            raise ManagementError(f"split part row mismatch: {part_path}; expected={expected_count}; actual={len(part_rows)}")
        for row in part_rows:
            row["__source_part"] = meta["part_path"]
            rows.append(row)
        part_results.append({"part_path": meta["part_path"], "rows": len(part_rows), "sha256": actual_sha})
    expected_total = int(index_rows[0].get("logical_record_count") or len(rows))
    if len(rows) != expected_total:
        raise ManagementError(f"logical dataset count mismatch: {index_path}; expected={expected_total}; actual={len(rows)}")
    return rows, {"index_path": str(index_path), "logical_rows": len(rows), "parts": part_results, "header": header or []}


def load_work_items(management_dir: Path) -> Tuple[List[dict], Dict[str, dict], Dict[str, List[dict]]]:
    index = read_csv(management_dir / "DEVELOPMENT_ITEM_INDEX.csv")
    work_items: List[dict] = []
    by_id: Dict[str, dict] = {}
    by_canonical: Dict[str, List[dict]] = defaultdict(list)
    ledger_cache: Dict[str, Dict[str, dict]] = {}
    for entity in index:
        by_id[entity["entity_id"]] = entity
        if entity["entity_type"] != "WORK_PACKAGE":
            continue
        ledger_path = entity.get("ledger_part", "")
        if ledger_path not in ledger_cache:
            ledger_cache[ledger_path] = {r["work_item_id"]: r for r in read_csv(management_dir / ledger_path)}
        detailed = dict(entity)
        detailed.update(ledger_cache[ledger_path].get(entity["entity_id"], {}))
        work_items.append(detailed)
        by_id[entity["entity_id"]] = detailed
        by_canonical[entity["canonical_requirement_id"]].append(detailed)
    for items in by_canonical.values():
        items.sort(key=lambda r: r["entity_id"])
    return work_items, by_id, by_canonical


def choose_primary(row: Mapping[str, str], candidates: Sequence[Mapping[str, str]], valid_ids: set[str]) -> Tuple[str, List[str], str, int, bool]:
    explicit = first_value(row, WORK_ITEM_ALIASES)
    if explicit:
        explicit_ids = split_values(explicit)
        valid = [item for item in explicit_ids if item in valid_ids]
        if valid:
            primary = valid[0]
            supporting = [item for item in valid[1:] if item != primary]
            return primary, supporting, "EXPLICIT_SOURCE_MAPPING", 1000, False
    if not candidates:
        return "", [], "NO_CANONICAL_WORK_ITEM", 0, True
    source_text = row_text(row)
    source_tokens = normalized_tokens(source_text)
    lowered = source_text.lower()
    scored = []
    for item in candidates:
        target_text = " ".join([
            item.get("axis", ""), item.get("axis_title", ""), item.get("work_type", ""),
            item.get("mandatory_results", ""), item.get("implementation_proposals", ""),
            item.get("scenario_classes", ""), item.get("owner", "")
        ])
        target_tokens = item.get("__normalized_tokens") or normalized_tokens(target_text)
        score = 4 * len(source_tokens & target_tokens)
        axis = (item.get("axis") or "").upper()
        work_type = (item.get("work_type") or "").upper()
        # Stable semantic boosts. These do not weaken the canonical Acceptance; they only select a primary slice.
        boosts = [
            (("contract", "ownership", "api", "spi", "계약", "소유"), ("CONTRACT", "OWNERSHIP"), 20),
            (("consumer", "provider", "implementation", "adapter", "구현", "호출", "사용처"), ("IMPLEMENTATION", "CONSUMER"), 18),
            (("test", "evidence", "verify", "검증", "증적", "회귀"), ("VERIFICATION", "EVIDENCE", "TEST"), 18),
            (("security", "audit", "mask", "secret", "보안", "감사", "마스킹", "권한"), ("SECURITY", "OPERATIONS"), 16),
            (("migration", "rollback", "schema", "database", "db", "마이그레이션", "롤백"), ("DB", "MIGRATION", "ROLLBACK"), 16),
            (("runtime", "reconcile", "retry", "recovery", "운영", "복구", "재시도"), ("RUNTIME", "RECOVERY", "RECONCILE", "OPERATIONS"), 14),
            (("frontend", "ui", "accessibility", "화면", "접근성"), ("FRONTEND", "UI", "OPERATIONS"), 14),
            (("generator", "generated", "sample", "생성기", "샘플"), ("GENERATOR", "SAMPLE", "GENERATED"), 14),
        ]
        for words, axis_words, boost in boosts:
            if any(word in lowered for word in words) and any(word in axis or word in work_type for word in axis_words):
                score += boost
        if work_type == "PHASE_GATE":
            score -= 8
        scored.append((score, item["entity_id"]))
    scored.sort(key=lambda pair: (-pair[0], pair[1]))
    best_score, primary = scored[0]
    supporting = [item["entity_id"] for item in candidates if item["entity_id"] != primary]
    manual = best_score <= 0 or (len(scored) > 1 and scored[0][0] == scored[1][0])
    basis = "SEMANTIC_AXIS_SCORE" if best_score > 0 else "CANONICAL_DETERMINISTIC_FALLBACK"
    return primary, supporting, basis, best_score, manual


def topo_sort(nodes: Sequence[str], edges: Sequence[Tuple[str, str]]) -> Tuple[List[str], List[str]]:
    node_set = set(nodes)
    incoming = {node: 0 for node in nodes}
    outgoing: Dict[str, List[str]] = defaultdict(list)
    for source, target in edges:
        if source in node_set and target in node_set:
            outgoing[source].append(target)
            incoming[target] += 1
    ready = deque(sorted([node for node, count in incoming.items() if count == 0]))
    ordered: List[str] = []
    while ready:
        node = ready.popleft()
        ordered.append(node)
        for target in sorted(outgoing[node]):
            incoming[target] -= 1
            if incoming[target] == 0:
                ready.append(target)
    cycles = sorted(node for node, count in incoming.items() if count > 0)
    ordered.extend(node for node in sorted(nodes) if node not in set(ordered))
    return ordered, cycles


def effective_target_state(row: Mapping[str, str]) -> str:
    current = (row.get("개발GPT_작업대상상태") or "작업 대상").strip()
    if current == "해당 없음 스킵":
        return current
    if (row.get("qa_reopen_action") or "").strip():
        action = row["qa_reopen_action"].strip().upper()
        if action in {"REDEVELOP", "재개발 요청"}:
            return "재개발 대상"
        if action in {"REREVIEW", "재검수 요청"}:
            return "재검수 대상"
    if (row.get("impact_invalidated") or "false").lower() == "true":
        return "재검수 대상"
    if (row.get("owner_resolved") or "true").lower() != "true":
        return "소유권 검토"
    if (row.get("external_blocked") or "false").lower() == "true":
        return "외부환경 차단"
    completed = (
        row.get("개발GPT_수행상태") == "완료"
        and row.get("개발GPT_자체검수상태") == "완료"
        and (row.get("evidence_valid") or "false").lower() == "true"
        and bool((row.get("개발GPT_완료기준SHA") or "").strip())
    )
    return "완료 스킵" if completed else (current if current in VALID_TARGET_STATES else "작업 대상")


def refresh_views(management_dir: Path) -> dict:
    state_path = management_dir / "DEVELOPMENT_ITEM_STATE.csv"
    rows = read_csv(state_path)
    for row in rows:
        row["개발GPT_작업대상상태"] = effective_target_state(row)
    write_csv(state_path, rows, rows[0].keys())
    active = [row for row in rows if row["개발GPT_작업대상상태"] in ACTIVE_TARGET_STATES]
    skipped = [row for row in rows if row["개발GPT_작업대상상태"] in SKIP_TARGET_STATES]
    write_csv(management_dir / "ACTIVE_DEVELOPMENT_SCOPE.csv", active, rows[0].keys())
    write_csv(management_dir / "COMPLETED_SKIP_SCOPE.csv", skipped, rows[0].keys())
    return {"total": len(rows), "active": len(active), "skipped": len(skipped), "states": dict(Counter(r["개발GPT_작업대상상태"] for r in rows))}

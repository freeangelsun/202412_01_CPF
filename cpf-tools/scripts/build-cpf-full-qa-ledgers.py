#!/usr/bin/env python3
"""Build Current CPF QA ledgers from canonical split masters.

The builder validates the source identity before reading or writing any ledger.
It is a coverage/metadata builder only; it never marks a Requirement or Scenario
as reviewed or passed.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import re
import subprocess
import tempfile
from collections import defaultdict
from pathlib import Path
from typing import Iterable

SHA_RE = re.compile(r"^[0-9a-f]{40}$")
SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
INDEX_REQUIRED = {
    "part_sequence", "part_path", "part_record_count", "first_record_id",
    "last_record_id", "size_bytes", "sha256", "logical_record_count",
}
SOURCE_SHA_COLUMNS = ("baseline_sha", "exact_sha", "head_sha", "commit_sha", "source_sha")
SOURCE_PATH_COLUMNS = ("path", "relative_path", "file_path", "source_path")
SOURCE_HASH_COLUMNS = ("sha256", "file_sha256", "source_sha256")

REQUIREMENT_FIELDS = [
    "requirement_id", "requirement", "priority", "owner_module", "owner_package",
    "source_basis", "change_target", "actual_consumer", "acceptance_criteria",
    "verification_method", "regression_protection", "execution_order",
    "work_package_id", "scenario_ids", "source_paths", "call_path",
    "verification_level", "verified_acceptance", "unverified_acceptance",
    "개발GPT_수행여부", "개발GPT_상태", "개발GPT_수행내용", "개발GPT_미완료사유",
    "개발GPT_실행및검증", "개발GPT_필요환경및권한", "개발GPT_evidence",
    "개발GPT_자체검수여부", "개발GPT_자체검수상태", "개발GPT_자체검사내용",
    "개발GPT_자체검수결과", "개발GPT_자체검수미완료사유", "개발GPT_자체검수evidence",
    "Codex_검수보완여부", "Codex_검수보완상태", "Codex_검수내용", "Codex_검수결과",
    "Codex_보완개발사유", "Codex_실행및검증", "Codex_미완료사유", "Codex_evidence",
    "QA_검수여부", "QA_검수회차", "QA_검사내용", "QA_검수결과", "QA_미통과사유",
    "QA_재개발요청여부", "QA_재개발대상역할", "QA_재개발요청사유", "QA_재개발요청내용",
    "QA_재개발대상파일", "QA_재실행명령", "QA_성공기대결과", "QA_실패판정기준",
    "QA_요구Evidence", "QA_미조치위험", "QA_검수evidence", "QA_검수이력경로",
    "QA_직접보완여부", "QA_직접보완상태", "QA_직접보완내용", "QA_직접보완파일",
    "QA_수정전evidence", "QA_수정후evidence", "개발GPT_교차검토상태",
    "Codex_교차검토상태", "독립QA_재검수상태", "development_status",
    "verification_status", "baseline_sha", "evidence_path", "evidence_sha256",
    "open_issue", "next_action", "state_revision", "updated_at", "updated_by",
]
SCENARIO_FIELDS = [
    "scenario_id", "linked_requirement_id", "work_package_id", "execution_order",
    "scenario_type", "title", "preconditions", "steps", "expected_result",
    "failure_criteria", "environment", "topology", "required_evidence",
    "verification_level", "verified_scope", "unverified_scope", "QA_검수여부",
    "QA_검수회차", "QA_검사내용", "QA_검수결과", "QA_미통과사유",
    "QA_검수evidence", "baseline_sha", "evidence_path", "evidence_sha256",
    "open_issue", "next_action", "state_revision", "updated_at", "updated_by",
]
REQ_METADATA_FIELDS = {
    "requirement_id", "requirement", "priority", "owner_module", "owner_package",
    "source_basis", "change_target", "actual_consumer", "acceptance_criteria",
    "verification_method", "regression_protection", "execution_order",
    "work_package_id", "scenario_ids", "baseline_sha",
}
SC_METADATA_FIELDS = {
    "scenario_id", "linked_requirement_id", "work_package_id", "execution_order",
    "scenario_type", "title", "preconditions", "steps", "expected_result",
    "failure_criteria", "environment", "topology", "required_evidence", "baseline_sha",
}


class LedgerError(RuntimeError):
    pass


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    if not path.is_file():
        raise LedgerError(f"missing CSV: {path}")
    with path.open(encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        fields = list(reader.fieldnames or [])
        rows = [{k: (v or "").strip() for k, v in row.items()} for row in reader]
    return fields, rows


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def run_git(root: Path, *args: str) -> str:
    result = subprocess.run(
        ["git", "-C", str(root), *args], text=True, capture_output=True, check=False
    )
    if result.returncode != 0:
        raise LedgerError(f"git {' '.join(args)} failed: {result.stderr.strip() or result.stdout.strip()}")
    return result.stdout.strip()


def first_present(fields: Iterable[str], candidates: Iterable[str]) -> str | None:
    available = set(fields)
    return next((candidate for candidate in candidates if candidate in available), None)


def verify_manifest_entries(root: Path, manifest_path: Path, rows: list[dict[str, str]], fields: list[str]) -> int:
    path_column = first_present(fields, SOURCE_PATH_COLUMNS)
    hash_column = first_present(fields, SOURCE_HASH_COLUMNS)
    if not path_column or not hash_column:
        raise LedgerError("source manifest must include a path column and a SHA-256 column")
    verified = 0
    root_resolved = root.resolve()
    manifest_resolved = manifest_path.resolve()
    for line, row in enumerate(rows, 2):
        relative = row.get(path_column, "").strip()
        expected = row.get(hash_column, "").strip().lower()
        if not relative or not expected:
            continue
        if not SHA256_RE.fullmatch(expected):
            raise LedgerError(f"source manifest line {line}: invalid SHA-256")
        candidate = (root / relative).resolve()
        if candidate == manifest_resolved:
            continue
        if root_resolved != candidate and root_resolved not in candidate.parents:
            raise LedgerError(f"source manifest line {line}: unsafe path {relative}")
        if not candidate.is_file():
            raise LedgerError(f"source manifest line {line}: missing file {relative}")
        actual = sha256(candidate)
        if actual != expected:
            raise LedgerError(f"source manifest line {line}: hash mismatch {relative}")
        verified += 1
    if verified == 0:
        raise LedgerError("source manifest did not verify any source file")
    return verified


def verify_source_identity(
    root: Path,
    expected_sha: str,
    source_manifest: str | None,
    source_manifest_sha256: str | None,
    identity_mode: str = "auto",
) -> dict[str, object]:
    if not SHA_RE.fullmatch(expected_sha):
        raise LedgerError("expected SHA must be exactly 40 lowercase hex")
    if identity_mode not in {"auto", "git-clean", "source-manifest"}:
        raise LedgerError(f"unsupported identity mode: {identity_mode}")

    git_dir = root / ".git"
    if identity_mode == "auto":
        identity_mode = "source-manifest" if source_manifest or source_manifest_sha256 else "git-clean"

    if identity_mode == "git-clean":
        if not git_dir.exists():
            raise LedgerError("git-clean identity requires a .git directory")
        actual = run_git(root, "rev-parse", "HEAD")
        if actual != expected_sha:
            raise LedgerError(f"HEAD mismatch expected={expected_sha} actual={actual}")
        dirty = run_git(root, "status", "--porcelain=v1", "--untracked-files=all")
        if dirty:
            sample = " | ".join(dirty.splitlines()[:10])
            raise LedgerError(f"dirty Working Tree is not accepted: {sample}")
        return {"mode": "git-clean", "actualHead": actual, "workingTree": "clean"}

    if not source_manifest or not source_manifest_sha256:
        raise LedgerError(
            "source-manifest identity requires --source-manifest and --source-manifest-sha256"
        )
    if not SHA256_RE.fullmatch(source_manifest_sha256.lower()):
        raise LedgerError("source manifest SHA-256 must be exactly 64 lowercase hex")
    manifest_path = Path(source_manifest)
    manifest_path = manifest_path if manifest_path.is_absolute() else root / manifest_path
    if not manifest_path.is_file():
        raise LedgerError(f"missing source manifest: {manifest_path}")
    actual_manifest_hash = sha256(manifest_path)
    if actual_manifest_hash != source_manifest_sha256.lower():
        raise LedgerError(
            f"source manifest hash mismatch expected={source_manifest_sha256.lower()} actual={actual_manifest_hash}"
        )
    fields, rows = read_csv(manifest_path)
    sha_column = first_present(fields, SOURCE_SHA_COLUMNS)
    if not sha_column:
        raise LedgerError(
            "source manifest must carry one of baseline_sha/exact_sha/head_sha/commit_sha/source_sha"
        )
    declared = {row.get(sha_column, "").strip().lower() for row in rows if row.get(sha_column, "").strip()}
    if not declared:
        raise LedgerError(f"source manifest has no declared {sha_column}")
    if declared != {expected_sha}:
        raise LedgerError(f"source manifest baseline mismatch declared={sorted(declared)} expected={expected_sha}")
    verified_files = verify_manifest_entries(root, manifest_path, rows, fields)
    result: dict[str, object] = {
        "mode": "source-manifest",
        "manifest": manifest_path.relative_to(root).as_posix()
        if root.resolve() in manifest_path.resolve().parents else str(manifest_path),
        "manifestSha256": actual_manifest_hash,
        "verifiedFiles": verified_files,
    }
    if git_dir.exists():
        actual = run_git(root, "rev-parse", "HEAD")
        if actual != expected_sha:
            raise LedgerError(f"overlay base HEAD mismatch expected={expected_sha} actual={actual}")
        dirty = run_git(root, "status", "--porcelain=v1", "--untracked-files=all")
        result.update({
            "baseHead": actual,
            "workingTree": "dirty-overlay" if dirty else "clean",
            "workingTreeEntryCount": len(dirty.splitlines()) if dirty else 0,
        })
    else:
        result.update({"baseHead": expected_sha, "workingTree": "archive"})
    return result


def load_split_master(root: Path, stem: str, id_field: str) -> list[dict[str, str]]:
    index_path = root / "cpf-docs/work/current" / f"{stem}.csv"
    fields, index = read_csv(index_path)
    missing = INDEX_REQUIRED - set(fields)
    if missing:
        raise LedgerError(f"{index_path}: missing index columns {sorted(missing)}")
    if not index:
        raise LedgerError(f"{index_path}: empty index")
    sequences = [int(row["part_sequence"]) for row in index]
    if sequences != list(range(1, len(index) + 1)):
        raise LedgerError(f"{index_path}: non-contiguous part_sequence")
    logical_counts = {int(row["logical_record_count"]) for row in index}
    if len(logical_counts) != 1:
        raise LedgerError(f"{index_path}: inconsistent logical_record_count")

    result: list[dict[str, str]] = []
    seen: set[str] = set()
    root_resolved = root.resolve()
    for item in index:
        relative = item["part_path"]
        part = (root / relative).resolve()
        if root_resolved not in part.parents or not part.is_file():
            raise LedgerError(f"{index_path}: unsafe/missing part {relative}")
        part_fields, rows = read_csv(part)
        if id_field not in part_fields:
            raise LedgerError(f"{relative}: missing {id_field}")
        ids = [row[id_field] for row in rows]
        if not ids or any(not value for value in ids):
            raise LedgerError(f"{relative}: empty/blank {id_field}")
        duplicate = next((value for value in ids if value in seen), None)
        if duplicate:
            raise LedgerError(f"{relative}: duplicate {id_field}={duplicate}")
        seen.update(ids)
        if len(rows) != int(item["part_record_count"]):
            raise LedgerError(f"{relative}: part count mismatch")
        if ids[0] != item["first_record_id"] or ids[-1] != item["last_record_id"]:
            raise LedgerError(f"{relative}: first/last id mismatch")
        if part.stat().st_size != int(item["size_bytes"]) or sha256(part) != item["sha256"]:
            raise LedgerError(f"{relative}: size/hash mismatch")
        result.extend(rows)
    declared = next(iter(logical_counts))
    if len(result) != declared:
        raise LedgerError(f"{index_path}: logical count mismatch declared={declared} actual={len(result)}")
    return result


def unique(rows: Iterable[dict[str, str]], key: str, label: str) -> dict[str, dict[str, str]]:
    result: dict[str, dict[str, str]] = {}
    for line, row in enumerate(rows, 2):
        value = row.get(key, "")
        if not value:
            raise LedgerError(f"{label}:{line}: blank {key}")
        if value in result:
            raise LedgerError(f"{label}:{line}: duplicate {key}={value}")
        result[value] = row
    return result


def existing_rows(path: Path, key: str) -> dict[str, dict[str, str]]:
    if not path.exists():
        return {}
    _, rows = read_csv(path)
    return unique(rows, key, str(path))


def atomic_write(path: Path, fields: list[str], rows: list[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, temporary_name = tempfile.mkstemp(prefix=path.name + ".", suffix=".tmp", dir=path.parent)
    temporary = Path(temporary_name)
    try:
        with os.fdopen(fd, "w", encoding="utf-8-sig", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=fields, extrasaction="ignore")
            writer.writeheader()
            writer.writerows({field: row.get(field, "") for field in fields} for row in rows)
            handle.flush()
            os.fsync(handle.fileno())
        os.replace(temporary, path)
    finally:
        if temporary.exists():
            temporary.unlink()


def value(row: dict[str, str], *names: str) -> str:
    for name in names:
        candidate = row.get(name, "").strip()
        if candidate:
            return candidate
    return ""


def changed_metadata(previous: dict[str, str], current: dict[str, str], fields: set[str]) -> bool:
    return any(previous.get(field, "") != current.get(field, "") for field in fields)


def next_revision(previous: dict[str, str], changed: bool) -> str:
    if not previous:
        return "0"
    current = previous.get("state_revision", "0")
    try:
        revision = int(current)
    except ValueError as exc:
        raise LedgerError(f"invalid state_revision={current!r}") from exc
    return str(revision + 1 if changed else revision)


def build(args: argparse.Namespace) -> dict[str, object]:
    root = Path(args.root).resolve()
    source_identity = verify_source_identity(
        root,
        args.expected_sha,
        getattr(args, "source_manifest", None),
        getattr(args, "source_manifest_sha256", None),
        getattr(args, "identity_mode", "auto"),
    )
    requirements = load_split_master(root, "CPF_REQUIREMENT_MASTER", "requirement_id")
    scenarios = load_split_master(root, "CPF_SCENARIO_MASTER", "scenario_id")
    executions = load_split_master(root, "CPF_EXECUTION_SEQUENCE", "execution_order")
    requirement_by_id = unique(requirements, "requirement_id", "Requirement Master")
    scenario_by_id = unique(scenarios, "scenario_id", "Scenario Master")
    execution_by_requirement = unique(executions, "requirement_id", "Execution Sequence")
    if set(requirement_by_id) != set(execution_by_requirement):
        missing = sorted(set(requirement_by_id) - set(execution_by_requirement))
        extra = sorted(set(execution_by_requirement) - set(requirement_by_id))
        raise LedgerError(f"execution/requirement mismatch missing={missing[:5]} extra={extra[:5]}")

    scenarios_by_requirement: dict[str, list[str]] = defaultdict(list)
    for scenario in scenarios:
        sid = scenario["scenario_id"]
        rid = scenario.get("linked_requirement_id", "")
        if rid not in requirement_by_id:
            raise LedgerError(f"{sid}: unknown linked requirement {rid}")
        scenarios_by_requirement[rid].append(sid)
    without_scenario = [rid for rid in requirement_by_id if not scenarios_by_requirement[rid]]
    if without_scenario:
        raise LedgerError(f"requirements without scenario: {without_scenario[:5]}")

    requirement_output = root / args.requirement_output
    scenario_output = root / args.scenario_output
    old_requirements = existing_rows(requirement_output, "requirement_id")
    old_scenarios = existing_rows(scenario_output, "scenario_id")
    unknown_old_req = sorted(set(old_requirements) - set(requirement_by_id))
    unknown_old_sc = sorted(set(old_scenarios) - set(scenario_by_id))
    if unknown_old_req or unknown_old_sc:
        raise LedgerError(
            f"existing ledger has unknown IDs requirements={unknown_old_req[:5]} scenarios={unknown_old_sc[:5]}"
        )

    requirement_rows: list[dict[str, str]] = []
    requirement_changes = 0
    for master in requirements:
        rid = master["requirement_id"]
        execution = execution_by_requirement[rid]
        previous = dict(old_requirements.get(rid, {}))
        current = {field: previous.get(field, "") for field in REQUIREMENT_FIELDS}
        current.update({
            "requirement_id": rid,
            "requirement": value(master, "requirement", "title"),
            "priority": value(master, "priority"),
            "owner_module": value(master, "owner_module", "owner"),
            "owner_package": value(master, "owner_package"),
            "source_basis": value(master, "source_basis"),
            "change_target": value(master, "change_target"),
            "actual_consumer": value(master, "actual_consumer", "consumer"),
            "acceptance_criteria": value(master, "acceptance_criteria"),
            "verification_method": value(master, "verification_method"),
            "regression_protection": value(master, "regression_protection"),
            "execution_order": execution["execution_order"],
            "work_package_id": execution.get("work_package_id", ""),
            "scenario_ids": ";".join(scenarios_by_requirement[rid]),
            "baseline_sha": args.expected_sha,
        })
        if not previous:
            current.update({
                "개발GPT_수행여부": "미완료", "개발GPT_상태": "미완료",
                "개발GPT_자체검수여부": "미완료", "개발GPT_자체검수상태": "미완료",
                "Codex_검수보완여부": "미완료", "Codex_검수보완상태": "미완료",
                "QA_검수여부": "아니오", "QA_재개발요청여부": "아니오",
                "QA_직접보완여부": "아니오", "QA_직접보완상태": "미완료",
                "개발GPT_교차검토상태": "미완료", "Codex_교차검토상태": "미완료",
                "독립QA_재검수상태": "미완료", "development_status": "미구현",
                "verification_status": "미검증", "open_issue": "QA 개별 검수 미착수",
                "next_action": "logical execution order에 따라 Requirement 개별 QA 검수",
                "updated_at": args.generated_at, "updated_by": args.updated_by,
            })
        changed = bool(previous) and changed_metadata(previous, current, REQ_METADATA_FIELDS)
        current["state_revision"] = next_revision(previous, changed)
        if changed:
            current["updated_at"] = args.generated_at
            current["updated_by"] = args.updated_by
            requirement_changes += 1
        requirement_rows.append(current)

    scenario_rows: list[dict[str, str]] = []
    scenario_changes = 0
    for master in scenarios:
        sid = master["scenario_id"]
        rid = master["linked_requirement_id"]
        execution = execution_by_requirement[rid]
        previous = dict(old_scenarios.get(sid, {}))
        current = {field: previous.get(field, "") for field in SCENARIO_FIELDS}
        master_wp = value(master, "work_package_id")
        execution_wp = execution.get("work_package_id", "")
        if master_wp and master_wp != execution_wp:
            raise LedgerError(f"{sid}: scenario/execution work package mismatch {master_wp}/{execution_wp}")
        current.update({
            "scenario_id": sid, "linked_requirement_id": rid,
            "work_package_id": execution_wp, "execution_order": execution["execution_order"],
            "scenario_type": value(master, "scenario_type"), "title": value(master, "title"),
            "preconditions": value(master, "preconditions"), "steps": value(master, "steps"),
            "expected_result": value(master, "expected_result"),
            "failure_criteria": value(master, "failure_criteria"),
            "environment": value(master, "environment"), "topology": value(master, "topology"),
            "required_evidence": value(master, "required_evidence"), "baseline_sha": args.expected_sha,
        })
        if not previous:
            current.update({
                "QA_검수여부": "아니오", "open_issue": "QA Scenario 개별 검수 미착수",
                "next_action": "연결 Requirement와 함께 Scenario 개별 QA 검수",
                "state_revision": "0", "updated_at": args.generated_at, "updated_by": args.updated_by,
            })
        changed = bool(previous) and changed_metadata(previous, current, SC_METADATA_FIELDS)
        current["state_revision"] = next_revision(previous, changed)
        if changed:
            current["updated_at"] = args.generated_at
            current["updated_by"] = args.updated_by
            scenario_changes += 1
        scenario_rows.append(current)

    atomic_write(requirement_output, REQUIREMENT_FIELDS, requirement_rows)
    atomic_write(scenario_output, SCENARIO_FIELDS, scenario_rows)
    return {
        "status": "PASS", "verifiedAgainstSha": args.expected_sha,
        "sourceIdentity": source_identity,
        "requirements": len(requirement_rows), "scenarios": len(scenario_rows),
        "requirementMetadataChanges": requirement_changes,
        "scenarioMetadataChanges": scenario_changes,
        "preservedRequirementRows": len(old_requirements),
        "preservedScenarioRows": len(old_scenarios),
        "requirementOutput": requirement_output.relative_to(root).as_posix(),
        "scenarioOutput": scenario_output.relative_to(root).as_posix(),
        "meaning": "Ledger coverage/metadata build only; individual QA completion is separate",
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--expected-sha", required=True)
    parser.add_argument("--identity-mode", choices=("auto", "git-clean", "source-manifest"), default="auto")
    parser.add_argument("--source-manifest")
    parser.add_argument("--source-manifest-sha256")
    parser.add_argument("--generated-at", required=True)
    parser.add_argument("--updated-by", default="QA GPT")
    parser.add_argument("--requirement-output", default="cpf-docs/work/current/REQUIREMENT_STATUS.csv")
    parser.add_argument("--scenario-output", default="cpf-docs/work/current/SCENARIO_STATUS.csv")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        result = build(args)
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    text = json.dumps(result, ensure_ascii=False, indent=2)
    if args.json_output:
        output = Path(args.json_output)
        output = output if output.is_absolute() else Path(args.root).resolve() / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(text + "\n", encoding="utf-8")
    print(text)
    return code


if __name__ == "__main__":
    raise SystemExit(main())

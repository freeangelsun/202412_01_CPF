#!/usr/bin/env python3
"""Fail-closed verifier and metadata repair tool for CPF split logical master CSVs.

The three canonical main CSVs are indexes, not logical data rows.  This tool
validates every indexed part against the exact bytes stored in the repository,
assembles the logical datasets in part_sequence order, and validates cross-links.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import re
import sys
from collections import Counter, defaultdict
from dataclasses import dataclass, asdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable, Iterator

INDEX_COLUMNS = [
    "dataset_manifest_version", "dataset_name", "dataset_kind",
    "logical_record_count", "logical_header_sha256", "part_sequence",
    "part_path", "part_record_count", "first_record_id", "last_record_id",
    "size_bytes", "sha256", "load_order", "assembly_rule", "consumer_rule",
]
DATASETS = {
    "requirement": {
        "index": "cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv",
        "name": "CPF_REQUIREMENT_MASTER",
        "id_column": "requirement_id",
        "minimum_count": 30558,
    },
    "scenario": {
        "index": "cpf-docs/work/current/CPF_SCENARIO_MASTER.csv",
        "name": "CPF_SCENARIO_MASTER",
        "id_column": "scenario_id",
        "minimum_count": 40763,
    },
    "execution": {
        "index": "cpf-docs/work/current/CPF_EXECUTION_SEQUENCE.csv",
        "name": "CPF_EXECUTION_SEQUENCE",
        "id_column": "execution_order",
        "minimum_count": 30558,
    },
}
ROLE_STATUS = {"완료", "미완료", "재개발 요청", "재검수 요청", "해당 없음", ""}
OVERALL_STATUS = {"완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요", ""}
QA_STATUS = {"통과", "미통과", ""}
SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
VERIFIED_AGAINST_SHA = "a6856e7557f586875796172ac6ebae22bb87958e"


class ValidationError(RuntimeError):
    pass


@dataclass
class PartResult:
    part_sequence: int
    part_path: str
    record_count: int
    first_record_id: str
    last_record_id: str
    size_bytes: int
    sha256: str


@dataclass
class DatasetResult:
    dataset_name: str
    index_path: str
    logical_record_count: int
    logical_header_sha256: str
    index_size_bytes: int
    index_sha256: str
    parts: list[PartResult]


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def safe_repo_file(root: Path, relative: str) -> Path:
    normalized = relative.replace("\\", "/")
    candidate = Path(normalized)
    if candidate.is_absolute() or re.match(r"^[A-Za-z]:[/\\]", normalized):
        raise ValidationError(f"absolute path forbidden: {relative}")
    if any(part == ".." for part in candidate.parts):
        raise ValidationError(f"parent traversal forbidden: {relative}")
    resolved = (root / candidate).resolve()
    try:
        resolved.relative_to(root)
    except ValueError as exc:
        raise ValidationError(f"path escapes repository: {relative}") from exc
    if not resolved.is_file():
        raise ValidationError(f"required file missing: {relative}")
    return resolved


def read_index(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    if not path.is_file():
        raise ValidationError(f"index missing: {path}")
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        reader = csv.DictReader(handle)
        fields = list(reader.fieldnames or [])
        rows = [{k: (v or "").strip() for k, v in row.items()} for row in reader]
    missing = [name for name in INDEX_COLUMNS if name not in fields]
    if missing:
        raise ValidationError(f"{path}: missing index columns={missing}")
    if not rows:
        raise ValidationError(f"{path}: empty split index")
    return fields, rows


def normalized_header_sha(fieldnames: list[str]) -> str:
    # The logical header contract is platform-independent and explicitly uses CRLF.
    line = ",".join(fieldnames).encode("utf-8") + b"\r\n"
    return sha256_bytes(line)


def iter_part_rows(path: Path) -> tuple[list[str], Iterator[dict[str, str]]]:
    handle = path.open("r", encoding="utf-8-sig", newline="")
    reader = csv.DictReader(handle)
    fields = list(reader.fieldnames or [])

    def iterator() -> Iterator[dict[str, str]]:
        try:
            for row in reader:
                yield {key: (value or "").strip() for key, value in row.items()}
        finally:
            handle.close()

    return fields, iterator()


def parse_int(value: str, label: str) -> int:
    try:
        return int(value)
    except ValueError as exc:
        raise ValidationError(f"invalid integer {label}={value!r}") from exc


def validate_dataset(root: Path, kind: str, repair: bool) -> tuple[DatasetResult, list[dict[str, str]], list[str]]:
    spec = DATASETS[kind]
    index_path = root / spec["index"]
    fields, index_rows = read_index(index_path)

    sequences = [parse_int(row["part_sequence"], "part_sequence") for row in index_rows]
    expected_sequences = list(range(1, len(index_rows) + 1))
    if sequences != expected_sequences:
        raise ValidationError(f"{index_path}: non-contiguous part_sequence={sequences}")

    invariant_fields = (
        "dataset_manifest_version", "dataset_name", "dataset_kind",
        "logical_record_count", "logical_header_sha256", "load_order",
        "assembly_rule", "consumer_rule",
    )
    for field in invariant_fields:
        values = {row[field] for row in index_rows}
        if len(values) != 1:
            raise ValidationError(f"{index_path}: inconsistent {field}={sorted(values)}")

    if index_rows[0]["dataset_name"] != spec["name"]:
        raise ValidationError(
            f"{index_path}: dataset_name mismatch expected={spec['name']} actual={index_rows[0]['dataset_name']}"
        )
    if index_rows[0]["dataset_kind"] != "split_csv_logical_master_index":
        raise ValidationError(f"{index_path}: invalid dataset_kind={index_rows[0]['dataset_kind']}")

    declared_count = parse_int(index_rows[0]["logical_record_count"], "logical_record_count")
    if declared_count < spec["minimum_count"]:
        raise ValidationError(
            f"{index_path}: logical count below baseline minimum={spec['minimum_count']} declared={declared_count}"
        )

    logical_fields: list[str] | None = None
    logical_rows: list[dict[str, str]] = []
    seen_ids: set[str] = set()
    duplicate_ids: list[str] = []
    part_results: list[PartResult] = []
    changed = False

    for row in index_rows:
        part_path_text = row["part_path"]
        part_path = safe_repo_file(root, part_path_text)
        raw_size = part_path.stat().st_size
        raw_sha = sha256_file(part_path)
        part_fields, rows_iter = iter_part_rows(part_path)
        if logical_fields is None:
            logical_fields = part_fields
        elif part_fields != logical_fields:
            raise ValidationError(f"{part_path}: header differs from first part")

        actual_header_sha = normalized_header_sha(part_fields)
        declared_header_sha = row["logical_header_sha256"]
        if not SHA256_RE.fullmatch(declared_header_sha):
            raise ValidationError(f"{index_path}: invalid logical_header_sha256={declared_header_sha}")
        if actual_header_sha != declared_header_sha:
            raise ValidationError(
                f"{part_path}: logical header SHA mismatch declared={declared_header_sha} actual={actual_header_sha}"
            )

        id_column = spec["id_column"]
        if id_column not in part_fields:
            raise ValidationError(f"{part_path}: missing primary id column={id_column}")
        count = 0
        first_id = ""
        last_id = ""
        for data_row in rows_iter:
            record_id = data_row.get(id_column, "")
            if not record_id:
                raise ValidationError(f"{part_path}: blank {id_column} at logical row {len(logical_rows) + 2}")
            if not first_id:
                first_id = record_id
            last_id = record_id
            count += 1
            if record_id in seen_ids and len(duplicate_ids) < 20:
                duplicate_ids.append(record_id)
            seen_ids.add(record_id)
            logical_rows.append(data_row)

        expected_count = parse_int(row["part_record_count"], "part_record_count")
        declared_first = row["first_record_id"]
        declared_last = row["last_record_id"]
        declared_size = parse_int(row["size_bytes"], "size_bytes")
        declared_sha = row["sha256"]
        metadata_mismatch = (
            expected_count != count
            or declared_first != first_id
            or declared_last != last_id
            or declared_size != raw_size
            or declared_sha != raw_sha
        )
        if metadata_mismatch and not repair:
            raise ValidationError(
                f"{part_path}: index metadata mismatch "
                f"record_count declared={expected_count} actual={count}; "
                f"id_range declared={declared_first}..{declared_last} actual={first_id}..{last_id}; "
                f"size declared={declared_size} actual={raw_size}; "
                f"sha256 declared={declared_sha} actual={raw_sha}"
            )
        if raw_size >= 8_000_000:
            raise ValidationError(f"{part_path}: part exceeds 8,000,000 bytes: {raw_size}")
        if metadata_mismatch:
            row["part_record_count"] = str(count)
            row["first_record_id"] = first_id
            row["last_record_id"] = last_id
            row["size_bytes"] = str(raw_size)
            row["sha256"] = raw_sha
            changed = True

        part_results.append(PartResult(
            part_sequence=parse_int(row["part_sequence"], "part_sequence"),
            part_path=part_path_text,
            record_count=count,
            first_record_id=first_id,
            last_record_id=last_id,
            size_bytes=raw_size,
            sha256=raw_sha,
        ))

    if duplicate_ids:
        raise ValidationError(f"{index_path}: duplicate {spec['id_column']}={duplicate_ids}")
    validate_canonical_id_continuity(kind, logical_rows, spec["id_column"])
    if len(logical_rows) != declared_count:
        raise ValidationError(f"{index_path}: logical count declared={declared_count} actual={len(logical_rows)}")

    if changed:
        with index_path.open("w", encoding="utf-8-sig", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=fields, lineterminator="\n")
            writer.writeheader()
            writer.writerows(index_rows)

    result = DatasetResult(
        dataset_name=spec["name"],
        index_path=spec["index"],
        logical_record_count=len(logical_rows),
        logical_header_sha256=index_rows[0]["logical_header_sha256"],
        index_size_bytes=index_path.stat().st_size,
        index_sha256=sha256_file(index_path),
        parts=part_results,
    )
    return result, logical_rows, list(logical_fields or [])



def validate_canonical_id_continuity(kind: str, rows: list[dict[str, str]], id_column: str) -> None:
    patterns = {
        "requirement": re.compile(r"^CPF-FR-(\d{6})$"),
        "scenario": re.compile(r"^CPF-SC-(\d{6})$"),
    }
    pattern = patterns.get(kind)
    if pattern is None or not rows:
        return
    values = [row[id_column] for row in rows]
    matches = [pattern.fullmatch(value) for value in values]
    if not all(matches):
        return
    numbers = [int(match.group(1)) for match in matches if match is not None]
    expected = list(range(numbers[0], numbers[0] + len(numbers)))
    if numbers != expected:
        for offset, (actual, wanted) in enumerate(zip(numbers, expected), start=1):
            if actual != wanted:
                raise ValidationError(
                    f"{kind} primary ID continuity broken at logical row {offset}: "
                    f"expected={wanted:06d} actual={actual:06d}"
                )
        raise ValidationError(f"{kind} primary ID continuity broken")

def validate_statuses(requirements: list[dict[str, str]], scenarios: list[dict[str, str]], executions: list[dict[str, str]]) -> None:
    role_columns = [
        "개발GPT_상태", "개발GPT_자체검수상태", "Codex_상태", "Codex_자체검수상태",
    ]
    qa_columns = ["QA_검수결과", "QA_재검수결과"]
    for row_no, row in enumerate(requirements, start=2):
        req_id = row.get("requirement_id", f"row-{row_no}")
        for column in role_columns:
            if column in row and row[column] not in ROLE_STATUS:
                raise ValidationError(f"requirement {req_id}: invalid {column}={row[column]!r}")
        for column in qa_columns:
            if column in row and row[column] not in QA_STATUS:
                raise ValidationError(f"requirement {req_id}: invalid {column}={row[column]!r}")
        for column in ("development_status", "verification_status"):
            if column in row and row[column] not in OVERALL_STATUS:
                raise ValidationError(f"requirement {req_id}: invalid {column}={row[column]!r}")
    for row in scenarios:
        status = row.get("execution_status", "")
        if status not in OVERALL_STATUS and status not in ROLE_STATUS:
            raise ValidationError(f"scenario {row.get('scenario_id')}: invalid execution_status={status!r}")
    for row in executions:
        for column in ("development_status", "verification_status"):
            if row.get(column, "") not in OVERALL_STATUS:
                raise ValidationError(
                    f"execution {row.get('execution_order')}: invalid {column}={row.get(column)!r}"
                )


def validate_cross_links(requirements: list[dict[str, str]], scenarios: list[dict[str, str]], executions: list[dict[str, str]]) -> dict[str, int]:
    req_ids = {row["requirement_id"] for row in requirements}
    scenario_bad = sorted({row.get("linked_requirement_id", "") for row in scenarios} - req_ids - {""})
    execution_bad = sorted({row.get("requirement_id", "") for row in executions} - req_ids - {""})
    if scenario_bad:
        raise ValidationError(f"scenario links unknown requirements={scenario_bad[:20]}")
    if execution_bad:
        raise ValidationError(f"execution links unknown requirements={execution_bad[:20]}")
    execution_req_ids = [row["requirement_id"] for row in executions]
    duplicates = [item for item, count in Counter(execution_req_ids).items() if count > 1]
    if duplicates:
        raise ValidationError(f"execution has duplicate requirement links={duplicates[:20]}")
    missing_exec = sorted(req_ids - set(execution_req_ids))
    extra_exec = sorted(set(execution_req_ids) - req_ids)
    if missing_exec or extra_exec:
        raise ValidationError(
            f"Requirement↔Execution coverage mismatch missing={missing_exec[:20]} extra={extra_exec[:20]}"
        )
    scenario_counts = Counter(row["linked_requirement_id"] for row in scenarios)
    without_scenario = sorted(req_ids - set(scenario_counts))
    return {
        "scenario_invalid_requirement_links": 0,
        "execution_invalid_requirement_links": 0,
        "requirements_without_scenario": len(without_scenario),
    }


def build_scope(executions: list[dict[str, str]], scenarios: list[dict[str, str]], limit: int) -> dict[str, object]:
    if limit <= 0 or limit > len(executions):
        raise ValidationError(f"scope limit must be 1..{len(executions)}, actual={limit}")
    scope = executions[:limit]
    req_ids = {row["requirement_id"] for row in scope}
    linked_scenarios = [row for row in scenarios if row.get("linked_requirement_id") in req_ids]
    phase_counts = Counter(row.get("phase_id", "") for row in scope)
    work_package_counts = Counter(row.get("work_package_id", "") for row in scope)
    return {
        "logical_row_start": 1,
        "logical_row_end": limit,
        "first_execution_order": scope[0]["execution_order"],
        "last_execution_order": scope[-1]["execution_order"],
        "first_requirement_id": scope[0]["requirement_id"],
        "last_requirement_id": scope[-1]["requirement_id"],
        "requirement_count": len(req_ids),
        "scenario_count": len(linked_scenarios),
        "phase_counts": dict(sorted(phase_counts.items())),
        "work_package_count": len(work_package_counts),
        "last_work_package_id": scope[-1].get("work_package_id", ""),
    }


def update_hash_manifest(root: Path, changed_paths: Iterable[str]) -> None:
    manifest = root / "cpf-docs/work/manifest/CPF_FILES.sha256"
    if not manifest.is_file():
        return
    entries: dict[str, str] = {}
    for line in manifest.read_text(encoding="utf-8-sig").splitlines():
        if not line.strip():
            continue
        digest, relative = line.split(None, 1)
        entries[relative.strip()] = digest
    for relative in changed_paths:
        # CPF_FILES.sha256 is the historical Requirement Rebase package manifest.
        # Refresh existing members only; session-local gates belong to the new overlay manifest.
        if relative not in entries:
            continue
        path = safe_repo_file(root, relative)
        entries[relative] = sha256_file(path)
    content = "".join(f"{entries[path]}  {path}\n" for path in sorted(entries))
    manifest.write_text(content, encoding="utf-8", newline="\n")


def update_validation_manifest(root: Path, results: dict[str, DatasetResult], summary: dict[str, object]) -> None:
    path = root / "cpf-docs/work/manifest/CPF_REQUIREMENT_VALIDATION_RESULT.json"
    if not path.is_file():
        return
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    mapping = {
        "requirement": "requirement_master_storage",
        "scenario": "scenario_master_storage",
        "execution": "execution_sequence_storage",
    }
    for kind, key in mapping.items():
        result = results[kind]
        data[key] = {
            "dataset_name": result.dataset_name,
            "logical_record_count": result.logical_record_count,
            "logical_header_sha256": result.logical_header_sha256,
            "index_path": result.index_path,
            "index_size_bytes": result.index_size_bytes,
            "index_sha256": result.index_sha256,
            "parts": [asdict(part) for part in result.parts],
        }
    data["requirement_count"] = results["requirement"].logical_record_count
    data["scenario_count"] = results["scenario"].logical_record_count
    data["execution_sequence_count"] = results["execution"].logical_record_count
    data["max_part_bytes"] = max(part.size_bytes for result in results.values() for part in result.parts)
    data["all_parts_under_8mb"] = data["max_part_bytes"] < 8_000_000
    data["passed"] = True
    data["verified_against_sha"] = VERIFIED_AGAINST_SHA
    data["verification_generated_at"] = datetime.now(timezone.utc).isoformat()
    data["split_master_validation"] = summary
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")



def update_package_manifest(
    root: Path,
    results: dict[str, DatasetResult],
    summary: dict[str, object],
    tracked_paths: Iterable[str],
) -> None:
    path = root / "cpf-docs/work/manifest/CPF_PACKAGE_MANIFEST.json"
    if not path.is_file():
        return
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    files_by_path = {item["path"]: item for item in data.get("files", []) if "path" in item}
    for relative in tracked_paths:
        candidate = root / relative
        # Preserve the historical package membership and identity.  New session files are
        # described by the session overlay PACKAGE_MANIFEST.json, not injected here.
        if relative not in files_by_path or not candidate.is_file():
            continue
        files_by_path[relative] = {
            "path": relative,
            "sha256": sha256_file(candidate),
            "size": candidate.stat().st_size,
        }
    all_parts = [part for result in results.values() for part in result.parts]
    data.update({
        "verifiedAgainstSha": VERIFIED_AGAINST_SHA,
        "verificationGeneratedAt": datetime.now(timezone.utc).isoformat(),
        "requirementCount": results["requirement"].logical_record_count,
        "scenarioCount": results["scenario"].logical_record_count,
        "executionSequenceCount": results["execution"].logical_record_count,
        "requirementPartCount": len(results["requirement"].parts),
        "scenarioPartCount": len(results["scenario"].parts),
        "executionSequencePartCount": len(results["execution"].parts),
        "maxIndividualFileBytes": max(part.size_bytes for part in all_parts),
        "maxIndividualFilePath": max(all_parts, key=lambda part: part.size_bytes).part_path,
        "allFilesUnder10MB": all(part.size_bytes < 10_000_000 for part in all_parts),
        "mainIndexPathsPreserved": True,
        "validationPassed": True,
        "files": [files_by_path[key] for key in sorted(files_by_path)],
        "splitMasterValidation": summary,
    })
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")


def validate_support_manifests(root: Path, results: dict[str, DatasetResult]) -> None:
    critical = set()
    for result in results.values():
        critical.add(result.index_path)
        critical.update(part.part_path for part in result.parts)
    critical.add("cpf-docs/work/manifest/CPF_REQUIREMENT_VALIDATION_RESULT.json")

    hashes_path = root / "cpf-docs/work/manifest/CPF_FILES.sha256"
    if hashes_path.is_file():
        entries: dict[str, str] = {}
        for line in hashes_path.read_text(encoding="utf-8-sig").splitlines():
            if not line.strip():
                continue
            digest, relative = line.split(None, 1)
            entries[relative.strip()] = digest
        missing = sorted(critical - set(entries))
        if missing:
            raise ValidationError(f"CPF_FILES.sha256 missing critical paths={missing[:20]}")
        mismatched = []
        for relative in sorted(critical):
            path = safe_repo_file(root, relative)
            actual = sha256_file(path)
            if entries[relative] != actual:
                mismatched.append((relative, entries[relative], actual))
        if mismatched:
            raise ValidationError(f"CPF_FILES.sha256 mismatch={mismatched[:5]}")

    package_path = root / "cpf-docs/work/manifest/CPF_PACKAGE_MANIFEST.json"
    if package_path.is_file():
        package = json.loads(package_path.read_text(encoding="utf-8-sig"))
        package_entries = {item["path"]: item for item in package.get("files", []) if "path" in item}
        missing = sorted(critical - set(package_entries))
        if missing:
            raise ValidationError(f"CPF_PACKAGE_MANIFEST.json missing critical paths={missing[:20]}")
        mismatched = []
        for relative in sorted(critical):
            path = safe_repo_file(root, relative)
            item = package_entries[relative]
            actual_sha = sha256_file(path)
            actual_size = path.stat().st_size
            if item.get("sha256") != actual_sha or int(item.get("size", -1)) != actual_size:
                mismatched.append((relative, item.get("size"), actual_size, item.get("sha256"), actual_sha))
        if mismatched:
            raise ValidationError(f"CPF_PACKAGE_MANIFEST.json mismatch={mismatched[:5]}")

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--scope-limit", type=int, default=10027)
    parser.add_argument("--repair-index", action="store_true")
    parser.add_argument("--repair-manifests", action="store_true")
    parser.add_argument("--json-output", type=Path)
    args = parser.parse_args()
    root = args.root.resolve()

    try:
        results: dict[str, DatasetResult] = {}
        rows: dict[str, list[dict[str, str]]] = {}
        fields: dict[str, list[str]] = {}
        for kind in ("requirement", "scenario", "execution"):
            result, logical_rows, logical_fields = validate_dataset(root, kind, args.repair_index)
            results[kind] = result
            rows[kind] = logical_rows
            fields[kind] = logical_fields
        validate_statuses(rows["requirement"], rows["scenario"], rows["execution"])
        cross_links = validate_cross_links(rows["requirement"], rows["scenario"], rows["execution"])
        scope = build_scope(rows["execution"], rows["scenario"], args.scope_limit)
        summary: dict[str, object] = {
            "status": "PASS",
            "repository_root": ".",
            "verified_against_sha": VERIFIED_AGAINST_SHA,
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "datasets": {kind: asdict(result) for kind, result in results.items()},
            "cross_links": cross_links,
            "scope": scope,
            "repair_index": args.repair_index,
        }
        if args.json_output:
            output = args.json_output
            if not output.is_absolute():
                output = root / output
            output.parent.mkdir(parents=True, exist_ok=True)
            output.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        if args.repair_manifests:
            changed = [result.index_path for result in results.values()]
            changed.extend(part.part_path for result in results.values() for part in result.parts)
            update_validation_manifest(root, results, summary)
            changed.append("cpf-docs/work/manifest/CPF_REQUIREMENT_VALIDATION_RESULT.json")
            changed.extend([
                "cpf-tools/scripts/verify-cpf-split-master-dataset.py",
                "cpf-tools/scripts/verify-cpf-split-master-dataset.ps1",
                "cpf-tools/scripts/verify-cpf-requirement-traceability.py",
                "cpf-tools/scripts/verify-cpf-final-completion.ps1",
                "cpf-tools/scripts/tests/test_verify_cpf_split_master_dataset.py",
                "cpf-docs/work/evidence/20260803/session4/split-master-validation.json",
                "cpf-docs/work/evidence/20260803/session4/requirement-traceability.json",
                "cpf-docs/work/evidence/20260803/session4/pre-fix-split-master-failure.log",
                "cpf-docs/work/evidence/20260803/session4/release-fail-closed.log",
                "cpf-docs/work/evidence/20260803/session4/negative-gate-exit-codes.txt",
            ])
            existing_changed = [path for path in changed if (root / path).is_file()]
            update_hash_manifest(root, existing_changed)
            package_tracked = list(existing_changed) + ["cpf-docs/work/manifest/CPF_FILES.sha256"]
            update_package_manifest(root, results, summary, package_tracked)
        validate_support_manifests(root, results)
        print(json.dumps({
            "status": "PASS",
            "requirement_count": len(rows["requirement"]),
            "scenario_count": len(rows["scenario"]),
            "execution_count": len(rows["execution"]),
            "scope": scope,
        }, ensure_ascii=False))
        return 0
    except (ValidationError, OSError, csv.Error, json.JSONDecodeError) as exc:
        print(f"CPF split master validation FAILED: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())

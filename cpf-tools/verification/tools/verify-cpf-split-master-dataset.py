#!/usr/bin/env python3
"""Strict split-master verifier with fail-closed exact-SHA provenance.

Default mode verifies the local Git HEAD and optional clean working tree. Connector snapshot mode
accepts only an explicit provenance document that binds all three canonical index files to an
exact repository SHA by both Git blob SHA-1 and SHA-256; all referenced part files are then
verified by the canonical index metadata. Snapshot mode never claims local working-tree cleanliness.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import subprocess
from pathlib import Path

INDEXES = {
    "requirement": (
        "cpf-docs/governance/development-harness/current/CPF_REQUIREMENT_MASTER.csv",
        "requirement_id",
        re.compile(r"^CPF-(?:FR|NFR|QA|SELF|GATE)-[A-Z0-9-]+$"),
    ),
    "scenario": (
        "cpf-docs/governance/development-harness/current/CPF_SCENARIO_MASTER.csv",
        "scenario_id",
        re.compile(r"^CPF-SC-[A-Z0-9-]+$"),
    ),
    "execution": (
        "cpf-docs/governance/development-harness/current/CPF_EXECUTION_SEQUENCE.csv",
        "execution_order",
        re.compile(r"^\d{2}-\d{8}$"),
    ),
}
REQUIRED_INDEX = {
    "part_sequence",
    "part_path",
    "part_record_count",
    "first_record_id",
    "last_record_id",
    "size_bytes",
    "sha256",
    "logical_record_count",
}
PROVENANCE_SOURCE_MODE = "github-connector-exact-sha"
SHA1_RE = re.compile(r"^[0-9a-f]{40}$")
SHA256_RE = re.compile(r"^[0-9a-f]{64}$")


class GateError(RuntimeError):
    pass


def git(root: Path, *args: str) -> str:
    process = subprocess.run(
        ["git", "-C", str(root), *args], text=True, capture_output=True
    )
    if process.returncode:
        raise GateError(f"git {' '.join(args)} failed: {process.stderr.strip()}")
    return process.stdout.strip()


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    digest.update(path.read_bytes())
    return digest.hexdigest()


def git_blob_sha1(path: Path) -> str:
    content = path.read_bytes()
    header = f"blob {len(content)}\0".encode("ascii")
    return hashlib.sha1(header + content).hexdigest()


def rows(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    if not path.is_file():
        raise GateError(f"required CSV missing: {path}")
    with path.open(encoding="utf-8-sig", newline="") as stream:
        reader = csv.DictReader(stream)
        return list(reader.fieldnames or []), [
            {key: (value or "").strip() for key, value in row.items()}
            for row in reader
        ]


def load_snapshot_provenance(
    root: Path, provenance_path: Path, expected_sha: str | None
) -> tuple[str, dict[str, object]]:
    if not expected_sha or not SHA1_RE.fullmatch(expected_sha):
        raise GateError("snapshot provenance mode requires a 40-character --expected-sha")
    if not provenance_path.is_file():
        raise GateError(f"snapshot provenance missing: {provenance_path}")
    try:
        provenance = json.loads(provenance_path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        raise GateError(f"invalid snapshot provenance JSON: {exc}") from exc
    if provenance.get("sourceMode") != PROVENANCE_SOURCE_MODE:
        raise GateError("snapshot provenance sourceMode is not github-connector-exact-sha")
    if provenance.get("baselineSha") != expected_sha:
        raise GateError(
            "snapshot provenance baseline mismatch "
            f"expected={expected_sha} actual={provenance.get('baselineSha')}"
        )
    files = provenance.get("files")
    if not isinstance(files, list):
        raise GateError("snapshot provenance files must be a list")
    entries: dict[str, dict[str, object]] = {}
    for raw in files:
        if not isinstance(raw, dict):
            raise GateError("snapshot provenance contains a non-object file entry")
        relative = raw.get("path")
        if not isinstance(relative, str) or not relative or relative in entries:
            raise GateError(f"snapshot provenance contains invalid/duplicate path: {relative!r}")
        entries[relative] = raw

    required_index_paths = {relative for relative, _, _ in INDEXES.values()}
    missing = sorted(required_index_paths - set(entries))
    extra = sorted(set(entries) - required_index_paths)
    if missing:
        raise GateError(f"snapshot provenance missing canonical index paths: {missing}")
    if extra:
        raise GateError(f"snapshot provenance contains unexpected paths: {extra}")

    verified_files: list[dict[str, object]] = []
    for relative in sorted(required_index_paths):
        entry = entries[relative]
        path = (root / relative).resolve()
        if root not in path.parents or not path.is_file():
            raise GateError(f"unsafe/missing provenance file: {relative}")
        declared_blob = str(entry.get("gitBlobSha") or "")
        declared_sha256 = str(entry.get("sha256") or "")
        if not SHA1_RE.fullmatch(declared_blob):
            raise GateError(f"invalid Git blob SHA for {relative}")
        if not SHA256_RE.fullmatch(declared_sha256):
            raise GateError(f"invalid SHA-256 for {relative}")
        actual_blob = git_blob_sha1(path)
        actual_sha256 = sha256(path)
        if actual_blob != declared_blob:
            raise GateError(
                f"Git blob mismatch {relative}: declared={declared_blob} actual={actual_blob}"
            )
        if actual_sha256 != declared_sha256:
            raise GateError(
                f"SHA-256 mismatch {relative}: declared={declared_sha256} actual={actual_sha256}"
            )
        declared_size = entry.get("sizeBytes")
        if declared_size is not None and int(declared_size) != path.stat().st_size:
            raise GateError(f"size mismatch {relative}")
        verified_files.append(
            {
                "path": relative,
                "gitBlobSha": actual_blob,
                "sha256": actual_sha256,
                "sizeBytes": path.stat().st_size,
            }
        )
    return expected_sha, {
        "sourceMode": PROVENANCE_SOURCE_MODE,
        "repository": provenance.get("repository", ""),
        "provenancePath": provenance_path.as_posix(),
        "verifiedIndexFiles": verified_files,
    }


def verify(
    root: Path,
    expected_sha: str | None = None,
    require_clean: bool = False,
    snapshot_provenance: Path | None = None,
) -> dict[str, object]:
    if snapshot_provenance is not None:
        if require_clean:
            raise GateError("--require-clean is not valid in connector snapshot mode")
        head, provenance_result = load_snapshot_provenance(
            root, snapshot_provenance.resolve(), expected_sha
        )
        working_tree_clean: bool | None = None
        source_mode = PROVENANCE_SOURCE_MODE
    else:
        if (root / ".git").exists():
            head = git(root, "rev-parse", "HEAD")
            if expected_sha and head != expected_sha:
                raise GateError(f"HEAD mismatch expected={expected_sha} actual={head}")
            status = git(root, "status", "--porcelain")
            if require_clean and status:
                raise GateError("working tree is not clean")
            working_tree_clean = not bool(status)
            source_mode = "local-git-worktree"
        else:
            if require_clean:
                raise GateError("--require-clean requires a Git working tree")
            # User-provided Local Working Tree ZIP is a first-class Source Identity.
            # This verifier validates the split datasets by byte/hash metadata and cross-links;
            # Git provenance must not be fabricated when the authoritative source has no .git.
            source_state = root / "cpf-tools/verification/tools/cpf-source-state.py"
            identity = hashlib.sha256()
            for relative, _, _ in INDEXES.values():
                path = root / relative
                identity.update(relative.encode("utf-8"))
                identity.update(b"\0")
                identity.update(path.read_bytes())
                identity.update(b"\0")
            head = "local-zip-index-sha256:" + identity.hexdigest()
            if expected_sha and expected_sha != head:
                raise GateError(f"local ZIP identity mismatch expected={expected_sha} actual={head}")
            working_tree_clean = None
            source_mode = "local-working-tree-zip"
        provenance_result = None

    result: dict[str, object] = {
        "status": "PASS",
        "verifiedAgainstSha": head,
        "sourceMode": source_mode,
        "workingTreeClean": working_tree_clean,
        "datasets": {},
        "findings": [],
    }
    if provenance_result is not None:
        result["snapshotProvenance"] = provenance_result

    logical: dict[str, list[dict[str, str]]] = {}
    for kind, (index_relative, id_column, id_pattern) in INDEXES.items():
        index_path = root / index_relative
        fields, index = rows(index_path)
        missing_columns = REQUIRED_INDEX - set(fields)
        if missing_columns:
            raise GateError(
                f"{index_relative}: missing columns {sorted(missing_columns)}"
            )
        if not index:
            raise GateError(f"{index_relative}: index is empty")
        sequence = [int(item["part_sequence"]) for item in index]
        if sequence != list(range(1, len(index) + 1)):
            raise GateError(f"{index_relative}: part sequence is not contiguous")

        all_rows: list[dict[str, str]] = []
        seen: set[str] = set()
        part_metadata: list[dict[str, object]] = []
        for item in index:
            relative = item["part_path"]
            part_path = (root / relative).resolve()
            if root not in part_path.parents or not part_path.is_file():
                raise GateError(f"unsafe/missing part {relative}")
            part_fields, part_rows = rows(part_path)
            if id_column not in part_fields:
                raise GateError(f"{relative}: missing {id_column}")
            ids: list[str] = []
            for line_number, row in enumerate(part_rows, 2):
                record_id = row.get(id_column, "")
                if not id_pattern.fullmatch(record_id):
                    raise GateError(
                        f"{relative}:{line_number}: malformed {id_column}={record_id!r}"
                    )
                if record_id in seen:
                    raise GateError(
                        f"{relative}:{line_number}: duplicate {id_column}={record_id}"
                    )
                seen.add(record_id)
                ids.append(record_id)
                all_rows.append(row)
            if len(part_rows) != int(item["part_record_count"]) or (
                ids
                and (
                    ids[0] != item["first_record_id"]
                    or ids[-1] != item["last_record_id"]
                )
            ):
                raise GateError(f"{relative}: declared part metadata mismatch")
            actual_sha256 = sha256(part_path)
            if (
                part_path.stat().st_size != int(item["size_bytes"])
                or actual_sha256 != item["sha256"]
            ):
                raise GateError(f"{relative}: byte/hash metadata mismatch")
            part_metadata.append(
                {
                    "path": relative,
                    "count": len(part_rows),
                    "sha256": actual_sha256,
                    "sizeBytes": part_path.stat().st_size,
                }
            )
        declared_count = int(index[0]["logical_record_count"])
        if declared_count != len(all_rows):
            raise GateError(
                f"{index_relative}: logical count mismatch "
                f"declared={declared_count} actual={len(all_rows)}"
            )
        logical[kind] = all_rows
        result["datasets"][kind] = {
            "count": len(all_rows),
            "indexGitBlobSha": git_blob_sha1(index_path),
            "indexSha256": sha256(index_path),
            "parts": part_metadata,
        }

    requirement_ids = {row["requirement_id"] for row in logical["requirement"]}
    scenario_ids = {row["scenario_id"] for row in logical["scenario"]}
    previous_order: tuple[int, int] | None = None
    execution_requirements: set[str] = set()
    work_package_positions: dict[str, list[int]] = {}
    for position, row in enumerate(logical["execution"], 1):
        order = row["execution_order"]
        phase = row.get("phase_id", "")
        if phase and order[:2] != phase.removeprefix("P").zfill(2):
            raise GateError(
                f"execution row {position}: phase/order mismatch {phase}/{order}"
            )
        numeric_order = (int(order[:2]), int(order[3:]))
        if previous_order is not None and numeric_order <= previous_order:
            raise GateError(
                f"execution row {position}: order not strictly ascending {order}"
            )
        previous_order = numeric_order
        requirement_id = row.get("requirement_id", "")
        scenario_id = row.get("scenario_id", "")
        if requirement_id not in requirement_ids:
            raise GateError(
                f"execution row {position}: unknown requirement {requirement_id}"
            )
        if requirement_id in execution_requirements:
            raise GateError(
                f"execution row {position}: duplicate requirement {requirement_id}"
            )
        execution_requirements.add(requirement_id)
        if scenario_id and scenario_id not in scenario_ids:
            raise GateError(f"execution row {position}: unknown scenario {scenario_id}")
        work_package = row.get("work_package_id", "")
        if work_package:
            work_package_positions.setdefault(work_package, []).append(position)

    missing_requirements = sorted(requirement_ids - execution_requirements)
    if missing_requirements:
        raise GateError(
            "execution sequence missing requirements "
            f"count={len(missing_requirements)} first={missing_requirements[:5]}"
        )
    for work_package, positions in work_package_positions.items():
        if positions != list(range(min(positions), max(positions) + 1)):
            raise GateError(f"work package not contiguous: {work_package}")

    result["crossLinks"] = {
        "requirementCount": len(requirement_ids),
        "scenarioCount": len(scenario_ids),
        "executionCount": len(logical["execution"]),
        "missing": 0,
    }
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--expected-sha")
    parser.add_argument("--require-clean", action="store_true")
    parser.add_argument("--snapshot-provenance")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    provenance_path = (
        Path(args.snapshot_provenance) if args.snapshot_provenance else None
    )
    if provenance_path is not None and not provenance_path.is_absolute():
        provenance_path = root / provenance_path
    try:
        result = verify(
            root,
            args.expected_sha,
            args.require_clean,
            provenance_path,
        )
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    if args.json_output:
        output = Path(args.json_output)
        output = output if output.is_absolute() else root / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(
            json.dumps(result, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())

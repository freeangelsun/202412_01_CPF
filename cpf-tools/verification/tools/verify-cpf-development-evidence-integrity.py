#!/usr/bin/env python3
"""Fail-closed validation for CPF development evidence and desired-state package metadata.

The verifier intentionally separates three identities:
* baselineSourceZipSha256: provenance of the input ZIP when an exact Git SHA is unavailable.
* sourceIdentity: deterministic identity of the source tree excluding identity-bearing evidence metadata.
* packagePayloadIdentity: deterministic identity of all desired-state files except the three self-referential package metadata files.

This separation prevents evidence files from hashing their own identity while still validating every packaged byte.
"""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass

import argparse
import csv
import hashlib
import importlib.util
import json
import re
import sys
from pathlib import Path

SHA1_RE = re.compile(r"^[0-9a-f]{40}$")
SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
PLACEHOLDERS = ("TODO", "TBD", "<HEAD>", "<SHA>", "YOUR_", "REPLACE_ME", "추후", "나중에 실행", "미정")
DEFAULT_METADATA_NAMES = ("PACKAGE_MANIFEST.json", "CHANGE_MANIFEST.csv", "SHA256SUMS.txt")


class GateError(RuntimeError):
    pass


def _sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def _rows(path: Path):
    if not path.is_file():
        raise GateError(f"missing csv: {path}")
    with path.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        return list(reader.fieldnames or []), [
            {key: (value or "").strip() for key, value in row.items()} for row in reader
        ]


def _safe(root: Path, rel: str) -> Path:
    rel = rel.replace("\\", "/").strip()
    if not rel or rel.startswith(("/", "\\")) or ".." in Path(rel).parts:
        raise GateError(f"unsafe evidence path: {rel!r}")
    target = (root / rel).resolve()
    if target != root and root not in target.parents:
        raise GateError(f"evidence escapes root: {rel}")
    return target


def _load_source_state_module(root: Path):
    tool = root / "cpf-tools/verification/tools/cpf-source-state.py"
    if not tool.is_file():
        return None
    spec = importlib.util.spec_from_file_location("cpf_source_state_for_evidence", tool)
    if spec is None or spec.loader is None:
        raise GateError("cannot load canonical source identity tool")
    mod = importlib.util.module_from_spec(spec)
    previous = sys.dont_write_bytecode
    sys.dont_write_bytecode = True
    try:
        spec.loader.exec_module(mod)
    finally:
        sys.dont_write_bytecode = previous
    return mod


def _all_file_paths(root: Path) -> list[str]:
    # Package payload는 실제 Deliverable ZIP 대상이다. cpf-source-state.py의 정본 ephemeral
    # 판정을 재사용해서 .git 내부/.gradle/node_modules/build 캐시가 실제 개발 중인 Working
    # Tree에 존재하더라도 payload로 잘못 집계되지 않게 한다(작은 격리 Fixture에는 이 tool
    # 트리가 없을 수 있으므로 그 경우에만 raw listing으로 fallback한다).
    mod = _load_source_state_module(root)
    is_generated = mod._is_generated if mod is not None else (lambda rel: False)
    return sorted(
        path.relative_to(root).as_posix()
        for path in root.rglob("*")
        if path.is_file() and not is_generated(path.relative_to(root).as_posix())
    )


def _identity_from_entries(entries: list[tuple[str, str]]) -> tuple[str, str]:
    """Hash a sorted hash/path inventory; avoids platform metadata and filesystem ordering drift."""
    material = "".join(f"{digest}  {rel}\n" for rel, digest in sorted(entries)).encode("utf-8")
    return hashlib.sha1(material).hexdigest(), hashlib.sha256(material).hexdigest()


def _canonical_source_snapshot(root: Path, documented_exclusions: list[str]) -> dict:
    mod = _load_source_state_module(root)
    if mod is not None:
        return mod.snapshot(root, "source")

    # Small isolated verifier fixtures do not contain the full CPF tool tree.  In that case only,
    # treat the manifest's documented exclusions as exact paths and use the legacy inventory
    # algorithm used by the fixture.  A real CPF checkout always contains cpf-source-state.py.
    excluded = {str(value).replace("\\", "/") for value in documented_exclusions}
    entries: list[tuple[str, str]] = []
    total_bytes = 0
    for rel in _all_file_paths(root):
        if rel in excluded:
            continue
        target = root / rel
        entries.append((rel, _sha256(target)))
        total_bytes += target.stat().st_size
    sha1, sha256 = _identity_from_entries(entries)
    return {"contentSha1": sha1, "contentSha256": sha256, "fileCount": len(entries), "totalBytes": total_bytes}


def _sha_list(path: Path, root: Path) -> dict[str, str]:
    if not path.is_file():
        raise GateError(f"missing sha list: {path}")
    out: dict[str, str] = {}
    for lineno, line in enumerate(path.read_text(encoding="utf-8-sig", errors="replace").splitlines(), 1):
        if not line.strip():
            continue
        parts = line.strip().split(None, 1)
        if len(parts) != 2 or not SHA256_RE.fullmatch(parts[0].lower()):
            raise GateError(f"invalid SHA256SUMS line {lineno}")
        rel = parts[1].strip().lstrip("*").replace("\\", "/")
        if rel in out:
            raise GateError(f"duplicate SHA256SUMS path: {rel}")
        target = _safe(root, rel)
        if not target.is_file():
            raise GateError(f"SHA256SUMS file missing: {rel}")
        actual = _sha256(target)
        if actual != parts[0].lower():
            raise GateError(f"SHA256SUMS hash mismatch: {rel}")
        out[rel] = actual
    return out


def _verify_change_manifest(path: Path, root: Path, payload_paths: set[str], package_rel: str) -> dict[str, dict]:
    fields, rows = _rows(path)
    required = {"path", "change_type", "size_bytes", "sha256", "baseline_sha256"}
    missing = required - set(fields)
    if missing:
        raise GateError(f"change manifest missing columns: {sorted(missing)}")
    out: dict[str, dict] = {}
    for row in rows:
        rel = row["path"].replace("\\", "/").strip()
        if not rel or rel in out:
            raise GateError(f"missing/duplicate change path: {rel!r}")
        change = row["change_type"].upper()
        if change not in {"ADDED", "MODIFIED", "DELETED"}:
            raise GateError(f"{rel}: invalid change_type={change}")
        target = _safe(root, rel)
        baseline_digest = row["baseline_sha256"].lower()
        if change == "DELETED":
            if target.exists():
                raise GateError(f"{rel}: DELETED path still exists")
            if not SHA256_RE.fullmatch(baseline_digest):
                raise GateError(f"{rel}: DELETED baseline_sha256 missing/invalid")
            if row["sha256"] or row["size_bytes"] not in {"", "0"}:
                raise GateError(f"{rel}: DELETED target metadata must be blank/0")
        else:
            if not target.is_file():
                raise GateError(f"change manifest file missing: {rel}")
            try:
                size = int(row["size_bytes"])
            except Exception as exc:
                raise GateError(f"{rel}: invalid size_bytes") from exc
            digest = row["sha256"].lower()
            if size != target.stat().st_size:
                raise GateError(f"change manifest size mismatch: {rel}")
            if not SHA256_RE.fullmatch(digest) or digest != _sha256(target):
                raise GateError(f"change manifest hash mismatch: {rel}")
            if change == "ADDED" and baseline_digest:
                raise GateError(f"{rel}: ADDED baseline_sha256 must be blank")
            if change == "MODIFIED" and not SHA256_RE.fullmatch(baseline_digest):
                raise GateError(f"{rel}: MODIFIED baseline_sha256 missing/invalid")
            if rel not in payload_paths and rel != package_rel:
                raise GateError(f"change manifest path is not packaged: {rel}")
        out[rel] = row
    return out


def _validate_manifest(root: Path, review: Path, manifest: dict) -> dict:
    if int(manifest.get("schemaVersion", 0)) < 6:
        raise GateError("package manifest schemaVersion must be >= 6")

    baseline = str(manifest.get("baselineSourceZipSha256", "")).strip().lower()
    if not SHA256_RE.fullmatch(baseline):
        raise GateError("package manifest baselineSourceZipSha256 missing/invalid")
    git_sha = str(manifest.get("gitExactSha", "")).strip()
    if git_sha != "UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT" and not SHA1_RE.fullmatch(git_sha.lower()):
        raise GateError("package manifest gitExactSha missing/invalid")

    source_identity = manifest.get("sourceIdentity")
    if not isinstance(source_identity, dict):
        raise GateError("package manifest sourceIdentity missing")
    source_sha1 = str(source_identity.get("sha1", "")).lower()
    source_sha256 = str(source_identity.get("sha256", "")).lower()
    if not SHA1_RE.fullmatch(source_sha1) or not SHA256_RE.fullmatch(source_sha256):
        raise GateError("package manifest sourceIdentity hash missing/invalid")
    source_exclusions = source_identity.get("excludedPaths")
    if not isinstance(source_exclusions, list) or not source_exclusions:
        raise GateError("package manifest sourceIdentity.excludedPaths missing")
    # The manifest documents the exclusion policy for human audit, but the digest itself must be
    # computed by the same canonical implementation used by the package builder.  Do not reinterpret
    # glob-like descriptions as exact path exclusions here, or the two validators will drift.
    computed_source = _canonical_source_snapshot(root, source_exclusions)
    if computed_source["contentSha1"] != source_sha1 or computed_source["contentSha256"] != source_sha256:
        raise GateError(
            "source identity mismatch "
            f"expected={source_sha256} actual={computed_source['contentSha256']}"
        )
    expected_source_count = source_identity.get("fileCount")
    if expected_source_count is not None and int(expected_source_count) != int(computed_source["fileCount"]):
        raise GateError("source identity fileCount mismatch")

    review_rel = review.relative_to(root).as_posix()
    canonical_metadata = {f"{review_rel}/{name}" for name in DEFAULT_METADATA_NAMES}
    metadata_exclusions = manifest.get("packageMetadataExcludedPaths", sorted(canonical_metadata))
    if not isinstance(metadata_exclusions, list):
        raise GateError("packageMetadataExcludedPaths must be a list")
    metadata_excluded = {str(value).replace("\\", "/") for value in metadata_exclusions}
    if metadata_excluded != canonical_metadata:
        raise GateError("package metadata exclusions must be the three review-directory self-referential files")

    files = manifest.get("files")
    if not isinstance(files, list) or not files:
        raise GateError("package manifest files must be a non-empty list")
    seen: set[str] = set()
    entries: list[tuple[str, str]] = []
    payload_bytes = 0
    for item in files:
        if not isinstance(item, dict):
            raise GateError("package manifest file item must be object")
        rel = str(item.get("path", "")).replace("\\", "/")
        if not rel or rel in seen:
            raise GateError(f"missing/duplicate manifest path: {rel!r}")
        if rel in metadata_excluded:
            raise GateError(f"self-referential metadata must not be package payload: {rel}")
        seen.add(rel)
        target = _safe(root, rel)
        if not target.is_file():
            raise GateError(f"manifest file missing: {rel}")
        try:
            size = int(item.get("sizeBytes", item.get("size_bytes")))
        except Exception as exc:
            raise GateError(f"manifest size invalid: {rel}") from exc
        digest = str(item.get("sha256", "")).lower()
        if size != target.stat().st_size:
            raise GateError(f"manifest size mismatch: {rel}")
        if not SHA256_RE.fullmatch(digest) or digest != _sha256(target):
            raise GateError(f"manifest hash mismatch: {rel}")
        entries.append((rel, digest))
        payload_bytes += size

    actual_payload = set(_all_file_paths(root)) - metadata_excluded
    if seen != actual_payload:
        missing = sorted(actual_payload - seen)[:5]
        extra = sorted(seen - actual_payload)[:5]
        raise GateError(f"package payload inventory mismatch missing={missing} extra={extra}")

    payload_sha1, payload_sha256 = _identity_from_entries(entries)
    payload = manifest.get("packagePayloadIdentity")
    if not isinstance(payload, dict):
        raise GateError("packagePayloadIdentity missing")
    if str(payload.get("sha1", "")).lower() != payload_sha1:
        raise GateError("package payload SHA-1 mismatch")
    if str(payload.get("sha256", "")).lower() != payload_sha256:
        raise GateError("package payload SHA-256 mismatch")
    if int(payload.get("fileCount", -1)) != len(seen):
        raise GateError("package payload fileCount mismatch")
    if int(payload.get("totalBytes", -1)) != payload_bytes:
        raise GateError("package payload totalBytes mismatch")

    desired = manifest.get("desiredState")
    if not isinstance(desired, dict):
        raise GateError("desiredState missing")
    if int(desired.get("fullFileCount", -1)) != len(_all_file_paths(root)):
        raise GateError("desiredState fullFileCount mismatch")

    return {
        "sourceSha1": source_sha1,
        "sourceSha256": source_sha256,
        "payloadSha1": payload_sha1,
        "payloadSha256": payload_sha256,
        "payloadPaths": seen,
        "manifestFiles": len(seen),
    }


def verify(root: Path, review_dir: Path, expected_sha: str | None, source_head: str | None,
           expected_requirements: int, expected_findings: int):
    root = root.resolve()
    review = (root / review_dir).resolve() if not review_dir.is_absolute() else review_dir.resolve()
    if review != root and root not in review.parents:
        raise GateError("review directory escapes root")

    required = [
        "PACKAGE_MANIFEST.json",
        "SHA256SUMS.txt",
        "TEST_AND_EVIDENCE.md",
        "CHANGE_MANIFEST.csv",
    ]
    for name in required:
        if not (review / name).is_file():
            raise GateError(f"missing review artifact: {name}")

    canonical_finding = root / "cpf-docs/governance/development-harness/current/CURRENT_DEVELOPMENT_STATUS.csv"
    finding_path = canonical_finding if canonical_finding.is_file() else review / "QA_FINDING_REVALIDATION.csv"
    canonical_requirement = root / "cpf-docs/governance/development-harness/current/CANONICAL_PRODUCT_REQUIREMENTS.csv"
    requirement_path = canonical_requirement if canonical_requirement.is_file() else review / "REQUIREMENT_STATUS.csv"
    if not finding_path.is_file():
        raise GateError(f"missing developer finding ledger: {finding_path}")
    if not requirement_path.is_file():
        raise GateError(f"missing requirement projection: {requirement_path}")

    manifest = json.loads((review / "PACKAGE_MANIFEST.json").read_text(encoding="utf-8"))
    package = _validate_manifest(root, review, manifest)

    # Optional CLI identity checks remain available for direct invocations, but do not substitute
    # a whole-tree runtime hash for the manifest's explicitly scoped source identity.
    if expected_sha:
        expected_sha = expected_sha.lower()
        if not (SHA1_RE.fullmatch(expected_sha) or SHA256_RE.fullmatch(expected_sha)):
            raise GateError("expected SHA format invalid")
        if not source_head:
            raise GateError("--expected-sha requires --source-head")
    if source_head:
        source_head = source_head.lower()
        if not (SHA1_RE.fullmatch(source_head) or SHA256_RE.fullmatch(source_head)):
            raise GateError("runtime source identity format invalid")
        if expected_sha and source_head != expected_sha:
            raise GateError(f"runtime source identity mismatch expected={expected_sha} actual={source_head}")

    sha_entries = _sha_list(review / "SHA256SUMS.txt", root)
    package_rel = (review / "PACKAGE_MANIFEST.json").relative_to(root).as_posix()
    change_rel = (review / "CHANGE_MANIFEST.csv").relative_to(root).as_posix()
    sha_rel = (review / "SHA256SUMS.txt").relative_to(root).as_posix()
    expected_sha_paths = set(package["payloadPaths"]) | {package_rel, change_rel}
    if sha_rel in sha_entries:
        raise GateError("SHA256SUMS.txt must not hash itself")
    if set(sha_entries) != expected_sha_paths:
        missing = sorted(expected_sha_paths - set(sha_entries))[:5]
        extra = sorted(set(sha_entries) - expected_sha_paths)[:5]
        raise GateError(f"SHA256SUMS inventory mismatch missing={missing} extra={extra}")

    _verify_change_manifest(review / "CHANGE_MANIFEST.csv", root, package["payloadPaths"], package_rel)

    fields, findings = _rows(finding_path)
    ids: set[str] = set()
    complete = incomplete = evidence_refs = 0

    if "work_item_id" in fields and "overall_status" in fields:
        mandatory = {
            "work_item_id", "development_status", "verification_status", "runtime_status",
            "overall_status", "source_identity", "verification_incomplete_reason", "current_action",
        }
        missing = mandatory - set(fields)
        if missing:
            raise GateError(f"Harness Current status missing columns: {sorted(missing)}")
        if expected_findings is not None and len(findings) != expected_findings:
            raise GateError(f"work item count mismatch expected={expected_findings} actual={len(findings)}")
        # Current Harness owns role/test evidence; status alone can never prove completion.
        role_path = root / "cpf-docs/governance/development-harness/current/ROLE_EXECUTION_LEDGER.csv"
        test_path = root / "cpf-docs/governance/development-harness/current/TEST_EXECUTION_LEDGER.csv"
        role_rows = _rows(role_path)[1] if role_path.is_file() else []
        test_rows = _rows(test_path)[1] if test_path.is_file() else []
        role_by: dict[str, list[dict[str,str]]] = {}
        test_by: dict[str, list[dict[str,str]]] = {}
        for rr in role_rows: role_by.setdefault(rr.get("work_item_id", ""), []).append(rr)
        for tr in test_rows: test_by.setdefault(tr.get("work_item_id", ""), []).append(tr)
        for row in findings:
            wid = row["work_item_id"]
            if not wid or wid in ids:
                raise GateError(f"missing/duplicate work item id: {wid}")
            ids.add(wid)
            if row["source_identity"].lower() != package["sourceSha256"]:
                raise GateError(f"{wid}: stale source identity")
            is_complete = row["overall_status"] == "완료"
            if is_complete:
                if row["development_status"] != "완료" or row["verification_status"] != "완료" or row["runtime_status"] not in {"PASS", "NOT_APPLICABLE"}:
                    raise GateError(f"{wid}: overall complete without development/verification/runtime completion")
                wr = role_by.get(wid, [])
                if {r.get("role") for r in wr} != {"DEVGPT", "INDEPENDENT_REVIEWER", "QA"}:
                    raise GateError(f"{wid}: role ledger coverage incomplete")
                for rr in wr:
                    if rr.get("execution_status") != "PASS":
                        raise GateError(f"{wid}: completed without {rr.get('role')} PASS")
                    ev = (rr.get("evidence") or "").strip()
                    if not ev or not _safe(root, ev).is_file():
                        raise GateError(f"{wid}: completed role evidence missing {rr.get('role')}")
                    evidence_refs += 1
                mandatory_tests = [x for x in test_by.get(wid, []) if x.get("mandatory") == "true"]
                if not mandatory_tests:
                    raise GateError(f"{wid}: no mandatory tests")
                for tr in mandatory_tests:
                    if tr.get("status") != "PASS":
                        raise GateError(f"{wid}: completed without mandatory test PASS {tr.get('test_execution_id')}")
                    ev = (tr.get("evidence") or "").strip()
                    if not ev or not _safe(root, ev).is_file():
                        raise GateError(f"{wid}: mandatory test evidence missing {tr.get('test_execution_id')}")
                    if tr.get("evidence_sha256") and _sha256(_safe(root, ev)) != tr["evidence_sha256"].lower():
                        raise GateError(f"{wid}: mandatory test evidence SHA mismatch {tr.get('test_execution_id')}")
                    evidence_refs += 1
                complete += 1
            else:
                # Incomplete is valid state but never promoted. It must explain why/what next.
                if not ((row.get("verification_incomplete_reason") or "").strip() or (row.get("current_action") or "").strip()):
                    raise GateError(f"{wid}: incomplete work lacks reason/action")
                incomplete += 1
    elif "closure_state" in fields:
        mandatory = {
            "finding_key", "qa_source", "finding_id", "development_status",
            "verification_status", "runtime_status", "overall_status",
            "closure_state", "evidence_paths", "external_blocker",
            "reexecution_command", "source_identity_sha256",
        }
        missing = mandatory - set(fields)
        if missing:
            raise GateError(f"finding closure ledger missing columns: {sorted(missing)}")
        if len(findings) != expected_findings:
            raise GateError(f"finding count mismatch expected={expected_findings} actual={len(findings)}")
        for row in findings:
            key = row["finding_key"]
            fid = row["finding_id"]
            if not key or key in ids:
                raise GateError(f"missing/duplicate finding key: {key}")
            ids.add(key)
            if row["source_identity_sha256"].lower() != package["sourceSha256"]:
                raise GateError(f"{key}: stale source identity")
            state = row["closure_state"]
            if state not in {"CLOSED", "BLOCKED_EXTERNAL"}:
                raise GateError(f"{key}: invalid closure_state={state!r}")
            refs = [value.strip() for value in re.split(r"[;\n]", row["evidence_paths"]) if value.strip()]
            if not refs:
                raise GateError(f"{key}: evidence missing")
            for rel in refs:
                if not _safe(root, rel).is_file():
                    raise GateError(f"{key}: referenced evidence missing: {rel}")
                evidence_refs += 1
            if not any(fid.lower() in rel.lower() for rel in refs):
                raise GateError(f"{key}: dedicated finding evidence path missing")
            if row["development_status"] != "완료":
                raise GateError(f"{key}: source development is not complete")
            if state == "CLOSED":
                if row["verification_status"] != "완료" or row["overall_status"] != "완료":
                    raise GateError(f"{key}: CLOSED without completed verification/overall status")
                if row["external_blocker"].strip():
                    raise GateError(f"{key}: CLOSED row must not retain external blocker")
                complete += 1
            else:
                if not row["external_blocker"].strip():
                    raise GateError(f"{key}: BLOCKED_EXTERNAL reason missing")
                command = row["reexecution_command"].strip()
                if not command or any(token.lower() in command.lower() for token in PLACEHOLDERS):
                    raise GateError(f"{key}: BLOCKED_EXTERNAL reexecution command missing/non-reproducible")
                incomplete += 1
    else:
        mandatory = {
            "finding_id", "개발GPT_상태", "source_head", "result_identity",
            "positive_exit_code", "negative_exit_code", "regression_exit_code",
            "evidence_paths", "execution_command",
        }
        missing = mandatory - set(fields)
        if missing:
            raise GateError(f"finding ledger missing columns: {sorted(missing)}")
        if len(findings) != expected_findings:
            raise GateError(f"finding count mismatch expected={expected_findings} actual={len(findings)}")
        completed_commands: dict[str, str] = {}
        expected_result_identity = (
            f"CONTENT_SHA1_{package['sourceSha1']};CONTENT_SHA256_{package['sourceSha256']}"
        )
        for row in findings:
            fid = row["finding_id"]
            if not fid or fid in ids:
                raise GateError(f"missing/duplicate finding id: {fid}")
            ids.add(fid)
            state = row["개발GPT_상태"]
            if state not in {"완료", "미완료"}:
                raise GateError(f"{fid}: invalid developer state {state!r}")
            if row["source_head"].lower() != package["sourceSha1"]:
                raise GateError(f"{fid}: stale source identity")
            if row["result_identity"] != expected_result_identity:
                raise GateError(f"{fid}: stale result identity")
            command = row["execution_command"]
            if not command or any(token.lower() in command.lower() for token in PLACEHOLDERS):
                raise GateError(f"{fid}: non-reproducible command")
            refs = [value.strip() for value in re.split(r"[;\n]", row["evidence_paths"]) if value.strip()]
            if not refs:
                raise GateError(f"{fid}: evidence missing")
            for rel in refs:
                if not _safe(root, rel).is_file():
                    raise GateError(f"{fid}: referenced evidence missing: {rel}")
                evidence_refs += 1
            if state == "완료":
                normalized_command = " ".join(command.split())
                previous = completed_commands.get(normalized_command)
                if previous:
                    raise GateError(f"{fid}: execution command duplicates completed finding {previous}")
                completed_commands[normalized_command] = fid
                if not any(fid.lower() in rel.lower() for rel in refs):
                    raise GateError(f"{fid}: completed finding lacks dedicated evidence path containing finding ID")
                for code_key in ("positive_exit_code", "negative_exit_code", "regression_exit_code"):
                    if row[code_key] != "0":
                        raise GateError(f"{fid}: completed finding lacks successful {code_key}")
                complete += 1
            else:
                if not row.get("미완료사유", "").strip():
                    raise GateError(f"{fid}: incomplete reason missing")
                incomplete += 1

    req_fields, reqs = _rows(requirement_path)
    if len(reqs) != expected_requirements:
        raise GateError(f"requirement count mismatch expected={expected_requirements} actual={len(reqs)}")
    req_id_field = "requirement_id" if "requirement_id" in req_fields else ("exact_id" if "exact_id" in req_fields else None)
    if not req_id_field:
        raise GateError("requirement projection ID column missing: expected requirement_id or exact_id")
    req_ids = [row[req_id_field] for row in reqs]
    if any(not value for value in req_ids) or len(set(req_ids)) != len(reqs):
        raise GateError("requirement projection IDs missing/duplicate")

    for path in review.glob("*"):
        if path.is_file() and path.suffix.lower() in {".md", ".csv", ".json", ".txt"}:
            text = path.read_text(encoding="utf-8-sig", errors="replace")
            lower = text.lower()
            for token in PLACEHOLDERS:
                value = token.lower()
                if value == "todo":
                    # Evidence may legitimately report a zero count such as ``TODO `0``` or
                    # ``TODO=0``.  Only unresolved TODO markers are placeholders.
                    if re.search(r"\btodo\b(?!\s*(?:[:=]?\s*)?(?:`|\*\*)?0(?:`|\*\*)?)", lower):
                        raise GateError(f"placeholder token {token!r} in {path.name}")
                elif value in lower:
                    raise GateError(f"placeholder token {token!r} in {path.name}")

    return {
        "status": "PASS",
        "sourceIdentitySha1": package["sourceSha1"],
        "sourceIdentitySha256": package["sourceSha256"],
        "packagePayloadSha1": package["payloadSha1"],
        "packagePayloadSha256": package["payloadSha256"],
        "runtimeSourceSha": source_head or "NOT_SUPPLIED",
        "manifestFiles": package["manifestFiles"],
        "sha256Entries": len(sha_entries),
        "findings": {
            "total": len(findings),
            "complete": complete,
            "incomplete": incomplete,
            "evidenceReferences": evidence_refs,
        },
        "requirements": len(reqs),
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--review-dir", required=True)
    parser.add_argument("--expected-sha")
    parser.add_argument("--source-head")
    parser.add_argument("--expected-requirements", type=int)
    parser.add_argument("--expected-findings", type=int)
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    expected_requirements=args.expected_requirements
    if expected_requirements is None:
        canonical=root/"cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md"
        catalog_ids=[m.group(1) for line in canonical.read_text(encoding="utf-8-sig").splitlines() if (m:=re.match(r'^\| `([A-Z0-9-]+)` \|',line))]
        if not catalog_ids or len(catalog_ids)!=len(set(catalog_ids)):
            print(json.dumps({"status":"FAIL","message":f"canonical catalog invalid: {len(catalog_ids)}/{len(set(catalog_ids))}"},ensure_ascii=False)); return 1
        expected_requirements=len(catalog_ids)
    try:
        result = verify(
            root,
            Path(args.review_dir),
            args.expected_sha,
            args.source_head,
            expected_requirements,
            args.expected_findings,
        )
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    if args.json_output:
        output = Path(args.json_output)
        if not output.is_absolute():
            output = root / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())

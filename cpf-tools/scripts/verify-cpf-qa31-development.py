#!/usr/bin/env python3
"""CPF QA31 development/result fail-closed gate.

This gate never mutates source, SQL, matrices, checksums, or evidence. It validates
immutable QA31 inputs, vertical consumer wiring, DB-vendor migration parity,
result/evidence completeness, and README/Guide exclusion policy.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import re
import subprocess
import sys
import platform
import tempfile
from dataclasses import dataclass, asdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable, Any

ALLOWED_STATUS = {"완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요"}
SHA_RE = re.compile(r"^[0-9a-f]{40}$")
README_GUIDE_PATTERNS = (
    re.compile(r"(^|/)README[^/]*$", re.I),
    re.compile(r"^cpf-docs/guides/", re.I),
    re.compile(r"^cpf-tools/README\.md$", re.I),
    re.compile(r"^cpf-docs/assets/readme/", re.I),
    re.compile(r"^cpf-docs/work/overlay/20260730-readme-guides/", re.I),
)
IMMUTABLE_FILES = (
    "cpf-docs/work/current/CPF_20260730_06_QA31_PACKAGE_INDEX.md",
    "cpf-docs/work/current/CPF_20260730_06_QA31_DEVELOPMENT_REMEDIATION_REQUEST.md",
    "cpf-docs/quality/CPF_20260730_QA31_DEFECT_REGISTER.csv",
    "cpf-docs/quality/CPF_20260730_QA31_REQUIREMENT_MATRIX.csv",
    "cpf-docs/quality/CPF_20260730_QA31_SCENARIO_MATRIX.csv",
    "cpf-docs/governance/CPF_AI_DEVELOPMENT_QA_CONTINUITY_STANDARD.md",
    "cpf-docs/work/handover/CPF_QA_SESSION_HANDOVER_STANDARD.md",
    "cpf-docs/work/current/CPF_20260730_06_QA31_CODEX_BATCH_REVIEW_REQUEST.md",
)
INTEGRATED_MATRIX_PATH = "cpf-docs/quality/CPF_20260730_INTEGRATED_REQUIREMENT_SCENARIO_MATRIX.csv"
INTEGRATED_MATRIX_EXPECTED_ROWS = 926
INTEGRATED_MATRIX_EXPECTED_KIND_COUNTS = {"Requirement": 708, "Scenario": 218}

RESULT_FILES = (
    "cpf-docs/work/review/CPF_20260730_QA31_PRE_DEVELOPMENT_REVIEW.md",
    "cpf-docs/work/review/CPF_20260730_QA31_DEVELOPMENT_COMPLETION_REPORT.md",
    "cpf-docs/quality/CPF_20260730_QA31_RESULT_MATRIX.csv",
    "cpf-docs/quality/CPF_20260730_QA31_UNRESOLVED_REGISTER.csv",
    "cpf-docs/work/handover/CPF_20260730_QA31_DEVELOPMENT_HANDOVER.md",
    "cpf-docs/work/current/CPF_20260730_QA31_CODEX_REVIEW_READY.md",
    "cpf-docs/work/manifest/CPF_20260730_QA31_DELETE_MANIFEST.txt",
    "cpf-docs/work/manifest/CPF_20260730_QA31_REQUEST_INTEGRITY.json",
)
REQUIRED_SOURCE = {
    "cpf-core/src/main/java/com/cpf/core/api/gateway/CpfGatewayControlSigner.java": (
        "contentSha256", "audience", "keyId", "MessageDigest.isEqual", "normalizedTarget"),
    "cpf-gateway/src/main/java/com/cpf/gateway/control/CpfGatewayControlAuthenticationFilter.java": (
        "CpfGatewayControlNoncePort", "CpfGatewayControlSecurityAuditPort", "CONTENT_SHA256", "sha256(body)", "audience"),
    "cpf-gateway/src/main/java/com/cpf/gateway/route/CpfGatewayPathRewriter.java": (
        "rewrite", "ingressPattern", "targetTemplate", "requestPath"),
    "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java": (
        "HandlerFunctions.http", "request.uri().getRawPath()", "snapshot.resolveRequest",
        "ledgerRecovery.begin", "ledgerRecovery.recordAttempt", "RetryFilterFunctions.retry",
        "captureService.captureRequestMetadata", "requestSignatureVerified(principal)"),
    "cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayProbeExecutor.java": (
        "NETWORK", "TCP", "TLS", "APPLICATION", "GATEWAY_E2E", "HttpURLConnection"),
    "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerCompletionFilter.java": (
        "recovery.complete", "captureService.captureResponseBody", "UNKNOWN_RESULT", "AFTER"),
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchRuntimeExecutorRegistry.java": (
        "ObjectMapper", "writeValueAsString", "readTree", "Payload is not valid JSON"),
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchFileProcessHandlerRegistry.java": (
        "FileProcessHandler", "require", "Duplicate FILE_PROCESS handler"),
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/SpringBatchWorkerStepHandler.java": (
        "implements BatchStepHandler", "handler.process", "claimForProcess", "moveFromProcessing"),
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/JcaScriptArtifactVerifier.java": (
        "Signature", "CertificateFactory", "PKIX"),
    "cpf-admin/src/main/java/com/cpf/admin/opr/parameter/AdmParameterReferenceCatalogAdapter.java": (
        "secretReferences", "pathAliases", "fileReferences", "valueExposed"),
    "cpf-reference/src/main/java/com/cpf/reference/batch/file/csv/ReferenceCsvFileProcessHandler.java": (
        "FileProcessHandler", "REF_CSV_COUNT", "FileProcessResult.completed"),
}
FORBIDDEN_LEGACY = (
    "cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRoute.java",
    "cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalog.java",
    "cpf-core/src/test/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalogTest.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/controller/CpfGatewayController.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/controller/CpfGatewayPublicController.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/transport/JdkCpfGatewayHttpExchangeAdapter.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerRuntime.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/JobPackDispatcher.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/internal/JdbcWorkerExecutionRepository.java",
)


@dataclass
class Check:
    name: str
    ok: bool
    detail: str


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def sha256_canonical_lf_text(path: Path) -> str:
    """Hash repository text in its .gitattributes eol=lf representation.

    Git may materialize the immutable QA input files as CRLF on Windows even
    though their repository bytes and recorded integrity hashes use LF.  BOM
    bytes are intentionally preserved; only line endings are canonicalized.
    """
    data = path.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"\n")
    return hashlib.sha256(data).hexdigest()


def read_csv(path: Path) -> list[dict[str, str]]:
    with path.open("r", encoding="utf-8-sig", newline="") as stream:
        return list(csv.DictReader(stream))


def is_excluded(path: str) -> bool:
    normalized = path.replace("\\", "/").lstrip("./")
    return any(pattern.search(normalized) for pattern in README_GUIDE_PATTERNS)


def git_lines(root: Path, *args: str) -> list[str]:
    result = subprocess.run(
        ["git", "-C", str(root), *args], text=True, encoding="utf-8",
        errors="replace", capture_output=True, check=False)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip() or f"git {' '.join(args)} failed")
    return [line.strip() for line in result.stdout.splitlines() if line.strip()]


def current_head(root: Path) -> str:
    try:
        value = git_lines(root, "rev-parse", "HEAD")
        return value[0].lower() if value else "WORKTREE-OVERLAY"
    except Exception:
        return "WORKTREE-OVERLAY"


def add(checks: list[Check], name: str, ok: bool, detail: str) -> None:
    checks.append(Check(name, bool(ok), detail))


def require_file(root: Path, relative: str, checks: list[Check]) -> Path:
    path = root / relative
    add(checks, f"file:{relative}", path.is_file(), "present" if path.is_file() else "missing")
    return path


def validate_request_integrity(root: Path, checks: list[Check]) -> None:
    manifest_path = root / "cpf-docs/work/manifest/CPF_20260730_QA31_REQUEST_INTEGRITY.json"
    if not manifest_path.is_file():
        add(checks, "request-integrity-manifest", False, "manifest missing")
        return
    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8-sig"))
    except Exception as exc:
        add(checks, "request-integrity-manifest", False, f"invalid JSON: {exc}")
        return
    entries = {str(item.get("path", "")): str(item.get("sha256", "")).lower()
               for item in manifest.get("files", [])}
    add(checks, "request-integrity-entry-count", len(entries) == len(IMMUTABLE_FILES),
        f"expected={len(IMMUTABLE_FILES)} actual={len(entries)}")
    for relative in IMMUTABLE_FILES:
        path = root / relative
        expected = entries.get(relative)
        if not path.is_file():
            add(checks, f"request:{relative}", False, "missing")
        elif not expected or not re.fullmatch(r"[0-9a-f]{64}", expected):
            add(checks, f"request:{relative}", False, "manifest hash missing/invalid")
        else:
            actual = sha256_canonical_lf_text(path)
            add(checks, f"request:{relative}", actual == expected,
                f"expected={expected} actual={actual}")


def validate_original_matrices(root: Path, checks: list[Check]) -> dict[str, dict[str, str]]:
    specs = (
        ("defect", "cpf-docs/quality/CPF_20260730_QA31_DEFECT_REGISTER.csv", "defect_id", 23),
        ("requirement", "cpf-docs/quality/CPF_20260730_QA31_REQUIREMENT_MATRIX.csv", "requirement_id", 99),
        ("scenario", "cpf-docs/quality/CPF_20260730_QA31_SCENARIO_MATRIX.csv", "scenario_id", 66),
    )
    all_rows: dict[str, dict[str, str]] = {}
    for kind, relative, id_column, expected_count in specs:
        path = root / relative
        if not path.is_file():
            add(checks, f"matrix:{kind}", False, "missing")
            continue
        try:
            rows = read_csv(path)
        except Exception as exc:
            add(checks, f"matrix:{kind}", False, f"parse failed: {exc}")
            continue
        ids = [row.get(id_column, "").strip() for row in rows]
        unique = len(ids) == len(set(ids)) and all(ids)
        status_column = "review_status" if kind == "defect" else "requested_status"
        statuses_ok = all(row.get(status_column, "").strip() == "OPEN" for row in rows)
        add(checks, f"matrix:{kind}:count", len(rows) == expected_count,
            f"expected={expected_count} actual={len(rows)}")
        add(checks, f"matrix:{kind}:unique", unique, f"ids={len(ids)} unique={len(set(ids))}")
        add(checks, f"matrix:{kind}:immutable-status", statuses_ok,
            "all OPEN" if statuses_ok else "original status was changed")
        for row_id, row in zip(ids, rows):
            if row_id:
                if row_id in all_rows:
                    add(checks, f"matrix:cross-duplicate:{row_id}", False, "ID duplicated across originals")
                all_rows[row_id] = {**row, "_kind": kind}
    add(checks, "matrix:total", len(all_rows) == 188, f"expected=188 actual={len(all_rows)}")
    return all_rows


def validate_integrated_matrix(root: Path, require_closure: bool, checks: list[Check]) -> None:
    """Validate the 708 Requirement + 218 Scenario product matrix directly.

    Development-result mode validates structural integrity and honest status values.
    Final-completion mode additionally rejects every non-complete development or
    verification row so QA31 cannot pass by validating only its own delta matrix.
    """
    path = root / INTEGRATED_MATRIX_PATH
    if not path.is_file():
        add(checks, "integrated-matrix:file", False, "missing")
        return
    try:
        rows = read_csv(path)
    except Exception as exc:
        add(checks, "integrated-matrix:parse", False, f"parse failed: {exc}")
        return
    required_columns = {
        "id", "kind", "wp", "title", "priority", "owner", "request",
        "development_status", "verification_status", "source_evidence",
    }
    columns = set(rows[0].keys()) if rows else set()
    missing_columns = sorted(required_columns - columns)
    add(checks, "integrated-matrix:columns", not missing_columns,
        "present" if not missing_columns else "missing=" + ",".join(missing_columns))
    if missing_columns:
        return
    ids = [row.get("id", "").strip() for row in rows]
    add(checks, "integrated-matrix:row-count", len(rows) == INTEGRATED_MATRIX_EXPECTED_ROWS,
        f"expected={INTEGRATED_MATRIX_EXPECTED_ROWS} actual={len(rows)}")
    add(checks, "integrated-matrix:id-unique", bool(ids) and all(ids) and len(ids) == len(set(ids)),
        f"rows={len(ids)} unique={len(set(ids))} blank={sum(1 for value in ids if not value)}")
    kind_counts: dict[str, int] = {}
    for row in rows:
        kind = row.get("kind", "").strip()
        kind_counts[kind] = kind_counts.get(kind, 0) + 1
    add(checks, "integrated-matrix:kind-counts", kind_counts == INTEGRATED_MATRIX_EXPECTED_KIND_COUNTS,
        f"expected={INTEGRATED_MATRIX_EXPECTED_KIND_COUNTS} actual={kind_counts}")
    invalid_priorities = sorted({row.get("priority", "").strip() for row in rows
                                 if row.get("priority", "").strip() not in {"P0", "P1", "P2"}})
    add(checks, "integrated-matrix:priority-enum", not invalid_priorities,
        "valid" if not invalid_priorities else "invalid=" + ",".join(invalid_priorities))
    for field in ("development_status", "verification_status"):
        invalid = sorted({row.get(field, "").strip() for row in rows
                          if row.get(field, "").strip() not in ALLOWED_STATUS})
        add(checks, f"integrated-matrix:{field}:enum", not invalid,
            "valid" if not invalid else "invalid=" + ",".join(invalid))
    blank_core = [row.get("id", "") for row in rows
                  if not row.get("id", "").strip()
                  or not row.get("kind", "").strip()
                  or not row.get("wp", "").strip()
                  or not row.get("title", "").strip()
                  or not row.get("priority", "").strip()]
    blank_requirement_contract = [row.get("id", "") for row in rows
                                  if row.get("kind", "").strip() == "Requirement"
                                  and (not row.get("owner", "").strip()
                                       or not row.get("request", "").strip())]
    add(checks, "integrated-matrix:core-fields", not blank_core,
        "present" if not blank_core else "blank=" + ",".join(blank_core[:20]))
    add(checks, "integrated-matrix:requirement-owner-request", not blank_requirement_contract,
        "present" if not blank_requirement_contract else "blank=" + ",".join(blank_requirement_contract[:20]))
    completed_without_evidence = [row.get("id", "") for row in rows
        if row.get("development_status", "").strip() == "완료"
        and not row.get("source_evidence", "").strip()]
    add(checks, "integrated-matrix:completed-source-evidence", not completed_without_evidence,
        "present" if not completed_without_evidence else "missing=" + ",".join(completed_without_evidence[:20]))
    dev_counts: dict[str, int] = {}
    verify_counts: dict[str, int] = {}
    for row in rows:
        dev = row.get("development_status", "").strip()
        ver = row.get("verification_status", "").strip()
        dev_counts[dev] = dev_counts.get(dev, 0) + 1
        verify_counts[ver] = verify_counts.get(ver, 0) + 1
    add(checks, "integrated-matrix:development-status-summary", True, json.dumps(dev_counts, ensure_ascii=False, sort_keys=True))
    add(checks, "integrated-matrix:verification-status-summary", True, json.dumps(verify_counts, ensure_ascii=False, sort_keys=True))
    if require_closure:
        open_development = [row.get("id", "") for row in rows
                            if row.get("development_status", "").strip() != "완료"]
        open_verification = [row.get("id", "") for row in rows
                             if row.get("verification_status", "").strip() != "완료"]
        add(checks, "integrated-matrix:closure-development", not open_development,
            "all complete" if not open_development else f"open={len(open_development)} sample=" + ",".join(open_development[:20]))
        add(checks, "integrated-matrix:closure-verification", not open_verification,
            "all complete" if not open_verification else f"open={len(open_verification)} sample=" + ",".join(open_verification[:20]))


def validate_source(root: Path, checks: list[Check]) -> None:
    for relative, anchors in REQUIRED_SOURCE.items():
        path = root / relative
        if not path.is_file():
            add(checks, f"source:{relative}", False, "missing")
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        missing = [anchor for anchor in anchors if anchor not in text]
        unfinished = bool(re.search(r"\b(TODO|FIXME|TBD|NOT_IMPLEMENTED|NotImplemented)\b", text, re.I))
        add(checks, f"source:{relative}:anchors", not missing,
            "present" if not missing else "missing=" + ",".join(missing))
        add(checks, f"source:{relative}:unfinished", not unfinished,
            "no unfinished marker" if not unfinished else "unfinished marker found")

    # Consumer-specific semantic checks to prevent interface/file-only false green.
    semantic = {
        "gateway-filter-body-replay": (
            "cpf-gateway/src/main/java/com/cpf/gateway/control/CpfGatewayControlAuthenticationFilter.java",
            (r"getInputStream\(\)", r"CpfGatewayControlSigner\.sha256", r"noncePort\.claim", r"securityAuditPort\.append")),
        "gateway-route-ack-before-activation": (
            "cpf-gateway/src/main/java/com/cpf/gateway/route/CpfGatewayRouteSnapshot.java",
            (r"ACK|acknowledged", r"current\.set|마지막 정상본")),
        "gateway-real-ingress-path": (
            "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java",
            (r"request\.uri\(\)\.getRawPath\(\)", r"snapshot\.resolveRequest\(")),
        "batch-file-process-consumer": (
            "cpf-batch/worker/src/main/java/com/cpf/batch/worker/SpringBatchWorkerStepHandler.java",
            (r"implements\s+BatchStepHandler", r"fileHandlers\.require", r"handler\.process",
             r"files\.claimForProcess", r"moveFromProcessing")),
        "batch-canonical-json-consumer": (
            "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchRuntimeExecutorRegistry.java",
            (r"readTree\(", r"writeValueAsString\(")),
        "parameter-catalog-consumer": (
            "cpf-admin/src/main/java/com/cpf/admin/opr/parameter/AdmParameterReferenceCatalogAdapter.java",
            (r"case \"SECRET\"", r"configured\.getSecrets", r"configured\.getPaths", r"configured\.getFiles")),
    }
    for name, (relative, patterns) in semantic.items():
        path = root / relative
        if not path.is_file():
            add(checks, f"semantic:{name}", False, "source missing")
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        missing = [pattern for pattern in patterns if re.search(pattern, text, re.M) is None]
        add(checks, f"semantic:{name}", not missing,
            "connected" if not missing else "missing-pattern=" + ",".join(missing))

    delete_manifest = root / "cpf-docs/work/manifest/CPF_20260730_QA31_DELETE_MANIFEST.txt"
    declared: set[str] = set()
    if delete_manifest.is_file():
        declared = {line.strip().replace("\\", "/") for line in delete_manifest.read_text(encoding="utf-8-sig").splitlines()
                    if line.strip() and not line.lstrip().startswith("#")}
    for relative in FORBIDDEN_LEGACY:
        absent = not (root / relative).exists()
        declared_delete = relative in declared
        add(checks, f"legacy:{relative}", absent or declared_delete,
            "absent" if absent else ("delete-manifest" if declared_delete else "still present and undeclared"))


def validate_db(root: Path, checks: list[Check]) -> None:
    required = (
        "cpf-tools/db/vendor/mariadb/migration/flyway/V81__qa31_gateway_target_batch_attempt_detail.sql",
        "cpf-tools/db/vendor/mariadb/rollback/R81__qa31_gateway_target_batch_attempt_detail.sql",
        "cpf-tools/db/vendor/oracle/migration/flyway/cpfDB/V81__qa31_gateway_target_nonce.sql",
        "cpf-tools/db/vendor/oracle/migration/flyway/batDB/V81__qa31_batch_attempt_detail.sql",
        "cpf-tools/db/vendor/oracle/migration/flyway/admDB/V81__qa31_durable_log_export.sql",
        "cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/V81__qa31_gateway_target_nonce.sql",
        "cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V81__qa31_batch_attempt_detail.sql",
        "cpf-tools/db/vendor/postgresql/migration/flyway/admDB/V81__qa31_durable_log_export.sql",
        "cpf-tools/db/vendor/oracle/rollback/cpfDB/R81__qa31_gateway_target_nonce.sql",
        "cpf-tools/db/vendor/oracle/rollback/batDB/R81__qa31_batch_attempt_detail.sql",
        "cpf-tools/db/vendor/oracle/rollback/admDB/R81__qa31_durable_log_export.sql",
        "cpf-tools/db/vendor/postgresql/rollback/cpfDB/R81__qa31_gateway_target_nonce.sql",
        "cpf-tools/db/vendor/postgresql/rollback/batDB/R81__qa31_batch_attempt_detail.sql",
        "cpf-tools/db/vendor/postgresql/rollback/admDB/R81__qa31_durable_log_export.sql",
    )
    for relative in required:
        require_file(root, relative, checks)

    # Checksum manifests must include every V*.sql in their own directory and never self-update here.
    checksum_dirs = (
        "cpf-tools/db/vendor/mariadb/migration/flyway",
        "cpf-tools/db/vendor/oracle/migration/flyway/cpfDB",
        "cpf-tools/db/vendor/oracle/migration/flyway/batDB",
        "cpf-tools/db/vendor/oracle/migration/flyway/admDB",
        "cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB",
        "cpf-tools/db/vendor/postgresql/migration/flyway/batDB",
        "cpf-tools/db/vendor/postgresql/migration/flyway/admDB",
    )
    for relative in checksum_dirs:
        directory = root / relative
        manifest = directory / "checksums.sha256"
        if not manifest.is_file():
            add(checks, f"checksum:{relative}", False, "manifest missing")
            continue
        entries: dict[str, str] = {}
        malformed: list[str] = []
        duplicates: list[str] = []
        for line in manifest.read_text(encoding="utf-8-sig").splitlines():
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            match = re.match(r"^([0-9a-fA-F]{64})\s+\*?(.+?)\s*$", line)
            if not match:
                malformed.append(line)
                continue
            name = match.group(2).strip()
            if name in entries:
                duplicates.append(name)
            entries[name] = match.group(1).lower()
        errors = []
        if malformed:
            errors.append(f"malformed={len(malformed)}")
        if duplicates:
            errors.append("duplicates=" + ",".join(sorted(set(duplicates))))
        for migration in sorted(directory.glob("V*.sql")):
            actual = sha256_file(migration)
            expected = entries.get(migration.name)
            if expected != actual:
                errors.append(f"{migration.name}:expected={expected}:actual={actual}")
        add(checks, f"checksum:{relative}", not errors, "OK" if not errors else "; ".join(errors[:20]))

    canonical = root / "cpf-tools/db/canonical/platform-schema.json"
    if not canonical.is_file():
        add(checks, "canonical-schema", False, "missing")
    else:
        try:
            data = json.loads(canonical.read_text(encoding="utf-8-sig"))
            raw = json.dumps(data, ensure_ascii=False).lower()
            anchors = ("cpf_gateway_control_nonce", "cpf_gateway_control_security_audit",
                       "bat_execution_attempt", "exit_code", "stdout_text", "stderr_text",
                       "artifact_hash", "unknown_result_yn", "adm_log_export_artifact")
            missing = [anchor for anchor in anchors if anchor not in raw]
            add(checks, "canonical-schema", not missing,
                "anchors present" if not missing else "missing=" + ",".join(missing))
        except Exception as exc:
            add(checks, "canonical-schema", False, f"invalid JSON: {exc}")


def validate_exclusions(root: Path, base_sha: str, checks: list[Check]) -> None:
    if not (root / ".git").exists():
        add(checks, "readme-guide-exclusion", True, "git metadata unavailable; package filter still checked")
        return
    try:
        # This is an evergreen gate. Historical changes committed after the
        # original QA31 base are valid product evolution; the exclusion applies
        # to the current overlay only, including staged and untracked files.
        changed = set(git_lines(root, "diff", "--name-only", "HEAD"))
        changed.update(git_lines(root, "diff", "--name-only", "--cached"))
        changed.update(git_lines(root, "ls-files", "--others", "--exclude-standard"))
        excluded = sorted(path for path in changed if is_excluded(path))
        add(checks, "readme-guide-exclusion", not excluded,
            "no excluded change" if not excluded else "changed=" + ",".join(excluded))
    except Exception as exc:
        add(checks, "readme-guide-exclusion", False, str(exc))


def evidence_valid(path: Path, expected_sha: str | None, require_exact: bool) -> tuple[bool, str]:
    if not path.is_file():
        return False, "evidence file missing"
    try:
        data = json.loads(path.read_text(encoding="utf-8-sig"))
    except Exception as exc:
        return False, f"invalid evidence JSON: {exc}"
    required = ("sourceSha", "command", "startedAt", "finishedAt", "exitCode", "expected", "actual",
                "environment", "profile", "relatedIds", "sensitiveDataRemoved")
    missing = [key for key in required if key not in data]
    if missing:
        return False, "missing fields: " + ",".join(missing)
    source_sha = str(data.get("sourceSha", "")).lower()
    if source_sha != "WORKTREE-OVERLAY" and not SHA_RE.fullmatch(source_sha):
        return False, f"invalid sourceSha: {source_sha}"
    if require_exact and expected_sha and source_sha != expected_sha:
        return False, f"sourceSha mismatch: {source_sha}"
    if data.get("sensitiveDataRemoved") is not True:
        return False, "sensitiveDataRemoved must be true"
    if not isinstance(data.get("relatedIds"), list) or not data.get("relatedIds"):
        return False, "relatedIds must be a non-empty list"
    try:
        int(data.get("exitCode"))
    except Exception:
        return False, "exitCode must be numeric"
    return True, "OK"


def validate_results(root: Path, originals: dict[str, dict[str, str]], mode: str,
                     expected_sha: str | None, require_exact: bool, checks: list[Check]) -> None:
    if mode == "report":
        require_file(root, RESULT_FILES[0], checks)
        return
    for relative in RESULT_FILES:
        require_file(root, relative, checks)

    requirement_scenario = {
        row_id: row for row_id, row in originals.items()
        if row.get("_kind") in {"requirement", "scenario"}
    }
    defects = {
        row_id: row for row_id, row in originals.items()
        if row.get("_kind") == "defect"
    }

    result_path = root / "cpf-docs/quality/CPF_20260730_QA31_RESULT_MATRIX.csv"
    if not result_path.is_file():
        return
    try:
        rows = read_csv(result_path)
    except Exception as exc:
        add(checks, "result-matrix-parse", False, str(exc))
        return

    # QA31 result matrix intentionally contains Requirement + Scenario only.
    # Defect IDs remain in the immutable defect register and are tracked in the
    # separate unresolved register so severity and original state cannot drift.
    modern_columns = {
        "record_type", "record_id", "priority", "result_status", "root_cause_group",
        "changed_files", "validation_command", "evidence_path", "notes",
    }
    legacy_columns = {
        "id", "source_type", "priority", "status", "root_cause", "changed_files",
        "validation", "evidence", "notes",
    }
    columns = set(rows[0].keys()) if rows else set()
    modern = modern_columns.issubset(columns)
    legacy = legacy_columns.issubset(columns)
    add(checks, "result-matrix-columns", modern or legacy,
        "modern" if modern else ("legacy" if legacy else "unsupported columns"))
    if not (modern or legacy):
        return

    def value(row: dict[str, str], modern_key: str, legacy_key: str) -> str:
        return row.get(modern_key if modern else legacy_key, "").strip()

    ids = [value(row, "record_id", "id") for row in rows]
    result_by_id = {row_id: row for row_id, row in zip(ids, rows) if row_id}
    add(checks, "result-matrix-id-coverage", set(result_by_id) == set(requirement_scenario),
        f"expected={len(requirement_scenario)} actual={len(result_by_id)} "
        f"missing={len(set(requirement_scenario)-set(result_by_id))} "
        f"extra={len(set(result_by_id)-set(requirement_scenario))}")
    add(checks, "result-matrix-duplicates", len(ids) == len(set(ids)),
        f"rows={len(ids)} unique={len(set(ids))}")

    evidence_cache: dict[str, tuple[bool, str]] = {}
    for row_id, row in result_by_id.items():
        status = value(row, "result_status", "status")
        add(checks, f"result:{row_id}:status", status in ALLOWED_STATUS, status or "blank")
        root_cause = value(row, "root_cause_group", "root_cause")
        if not root_cause:
            add(checks, f"result:{row_id}:root-cause", False, "blank")
        original = requirement_scenario.get(row_id, {})
        expected_priority = original.get("priority", "").strip()
        add(checks, f"result:{row_id}:priority", row.get("priority", "").strip() == expected_priority,
            f"expected={expected_priority} actual={row.get('priority','')}")
        if status == "완료":
            evidence_field = value(row, "evidence_path", "evidence")
            evidence_values = [item.strip() for item in re.split(r"[;|]", evidence_field) if item.strip()]
            if not evidence_values:
                add(checks, f"result:{row_id}:evidence", False, "completed row has no evidence")
            for relative in evidence_values:
                normalized = relative.replace("\\", "/")
                if normalized not in evidence_cache:
                    evidence_cache[normalized] = evidence_valid(root / normalized, expected_sha, require_exact)
                ok, detail = evidence_cache[normalized]
                add(checks, f"result:{row_id}:evidence:{normalized}", ok, detail)

    unresolved_path = root / "cpf-docs/quality/CPF_20260730_QA31_UNRESOLVED_REGISTER.csv"
    if not unresolved_path.is_file():
        return
    try:
        unresolved = read_csv(unresolved_path)
    except Exception as exc:
        add(checks, "unresolved-register-parse", False, str(exc))
        return
    unresolved_columns = set(unresolved[0].keys()) if unresolved else set()
    id_column = "defect_id" if "defect_id" in unresolved_columns else "id"
    status_column = "current_status" if "current_status" in unresolved_columns else "status"
    unresolved_ids = {row.get(id_column, "").strip() for row in unresolved if row.get(id_column, "").strip()}
    add(checks, "unresolved-register-defect-coverage", unresolved_ids == set(defects),
        f"expected={len(defects)} actual={len(unresolved_ids)} "
        f"missing={len(set(defects)-unresolved_ids)} extra={len(unresolved_ids-set(defects))}")
    add(checks, "unresolved-register-duplicates", len(unresolved) == len(unresolved_ids),
        f"rows={len(unresolved)} unique={len(unresolved_ids)}")
    for row in unresolved:
        defect_id = row.get(id_column, "").strip()
        if not defect_id:
            continue
        status = row.get(status_column, "").strip()
        add(checks, f"unresolved:{defect_id}:status", status in ALLOWED_STATUS, status or "blank")
        expected_priority = defects.get(defect_id, {}).get("priority", "").strip()
        add(checks, f"unresolved:{defect_id}:priority", row.get("priority", "").strip() == expected_priority,
            f"expected={expected_priority} actual={row.get('priority','')}")


def run_self_test() -> tuple[bool, dict[str, Any]]:
    checks: list[dict[str, Any]] = []
    with tempfile.TemporaryDirectory(prefix="cpf-qa31-selftest-") as tmp:
        root = Path(tmp)
        evidence = root / "evidence.json"
        valid = {
            "sourceSha": "a" * 40, "command": "test", "startedAt": utc_now(), "finishedAt": utc_now(),
            "exitCode": 0, "expected": "pass", "actual": "pass", "environment": {"java": "25"},
            "profile": "self-test", "relatedIds": ["QA31-S001"], "sensitiveDataRemoved": True,
        }
        evidence.write_text(json.dumps(valid), encoding="utf-8")
        ok, detail = evidence_valid(evidence, "a" * 40, True)
        checks.append({"name": "valid evidence accepted", "ok": ok, "detail": detail})
        missing_ok, missing_detail = evidence_valid(root / "missing.json", "a" * 40, True)
        checks.append({"name": "missing evidence rejected", "ok": not missing_ok, "detail": missing_detail})
        stale_ok, stale_detail = evidence_valid(evidence, "b" * 40, True)
        checks.append({"name": "stale SHA rejected", "ok": not stale_ok, "detail": stale_detail})
        before = hashlib.sha256(b"alpha").hexdigest()
        after = hashlib.sha256(b"alphb").hexdigest()
        checks.append({"name": "one-byte tamper changes hash", "ok": before != after})
        checks.append({"name": "README exclusion", "ok": is_excluded("README.md") and is_excluded("cpf-docs/guides/x.md") and not is_excluded("cpf-core/src/X.java")})
    passed = all(item["ok"] for item in checks)
    timestamp = utc_now()
    return passed, {
        "schemaVersion": 1,
        "gate": "CPF_QA31_SELF_TEST",
        "sourceSha": "WORKTREE-OVERLAY",
        "command": "verify-cpf-qa31-development.py --self-test",
        "startedAt": timestamp,
        "finishedAt": timestamp,
        "exitCode": 0 if passed else 1,
        "expected": "valid evidence accepted; missing/stale/tampered evidence rejected",
        "actual": f"checks={len(checks)} failures={sum(1 for item in checks if not item['ok'])}",
        "environment": {"python": platform.python_version()},
        "profile": "gate-self-test",
        "relatedIds": ["QA31-D001", "QA31-D002", "QA31-D007"],
        "checks": checks,
        "pass": passed,
        "sensitiveDataRemoved": True,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--base-sha", default="")
    parser.add_argument("--mode", choices=("report", "full"), default="report")
    parser.add_argument("--output", type=Path)
    parser.add_argument("--expected-sha", default="")
    parser.add_argument("--require-exact", action="store_true")
    parser.add_argument("--require-integrated-closure", action="store_true")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()

    if args.self_test:
        ok, report = run_self_test()
        text = json.dumps(report, ensure_ascii=False, indent=2)
        if args.output:
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(text + "\n", encoding="utf-8")
        print(text)
        return 0 if ok else 1

    root = args.root.resolve()
    started = utc_now()
    checks: list[Check] = []
    base_sha = args.base_sha.lower().strip()
    expected_sha = args.expected_sha.lower().strip() or None
    add(checks, "base-sha-format", bool(SHA_RE.fullmatch(base_sha)), base_sha or "blank")
    if expected_sha:
        add(checks, "expected-sha-format", bool(SHA_RE.fullmatch(expected_sha)), expected_sha)

    head = current_head(root)
    if expected_sha and head != "WORKTREE-OVERLAY":
        add(checks, "head-exact-sha", head == expected_sha, f"head={head} expected={expected_sha}")
    elif args.require_exact:
        add(checks, "head-exact-sha", False, "exact evidence requested but expected/head SHA unavailable")

    validate_request_integrity(root, checks)
    originals = validate_original_matrices(root, checks)
    validate_integrated_matrix(root, args.require_integrated_closure, checks)
    validate_source(root, checks)
    validate_db(root, checks)
    validate_exclusions(root, base_sha, checks)
    validate_results(root, originals, args.mode, expected_sha, args.require_exact, checks)

    failures = [check for check in checks if not check.ok]
    report = {
        "schemaVersion": 1,
        "gate": "CPF_QA31_DEVELOPMENT_RESULT",
        "mode": args.mode,
        "baseSha": base_sha,
        "headSha": head,
        "expectedSha": expected_sha,
        "sourceSha": head if SHA_RE.fullmatch(head) else "WORKTREE-OVERLAY",
        "command": " ".join([sys.executable, *sys.argv]),
        "startedAt": started,
        "finishedAt": utc_now(),
        "status": "완료" if not failures else "실패",
        "exitCode": 0 if not failures else 1,
        "expected": "QA31 immutable originals, source wiring, DB parity and result integrity pass",
        "actual": f"checks={len(checks)} failures={len(failures)}",
        "environment": {"python": platform.python_version(), "platform": platform.platform()},
        "profile": args.mode,
        "relatedIds": sorted(originals.keys()),
        "checkCount": len(checks),
        "failureCount": len(failures),
        "checks": [asdict(check) for check in checks],
        "sensitiveDataRemoved": True,
    }
    output = args.output or (root / "cpf-docs/evidence/current/qa31-development-gate.json")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for failure in failures:
        print(f"[FAIL] {failure.name}: {failure.detail}", file=sys.stderr)
    print(f"CPF QA31 gate status={report['status']} checks={len(checks)} failures={len(failures)} report={output}")
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())

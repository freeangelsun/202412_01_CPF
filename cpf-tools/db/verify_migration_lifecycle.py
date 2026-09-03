#!/usr/bin/env python3
"""Fail-closed verifier for CPF official database lifecycle packs.

This verifier is database-independent and is intended to run before the
Oracle, PostgreSQL, and MariaDB runtime matrix.  It validates the repository
contract that must be true before destructive or stateful DB execution starts:

* only the three official vendors are admitted;
* provision/install/seed/verify/runtime/template consumers actually exist;
* every discovered migration pack has an exact checksum manifest;
* Flyway versions are unique inside each logical database pack;
* every migration has either a rollback artifact or an explicit forward
  recovery policy;
* UNKNOWN, partial-failure, reapply, backup, and PITR contracts fail closed;
* vendor pack declarations and special reference packs point to real files;
* the declared root runtime executor and all static prerequisite gates exist.

Exit codes:
  0: verification passed
  2: repository contract violation
  3: invalid invocation or unexpected environment failure
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
import dataclasses
import datetime as dt
import fnmatch
import hashlib
import json
import re
import subprocess
import sys
from collections import defaultdict
from pathlib import Path
from typing import Any, Iterable, Mapping

OFFICIAL_VENDORS = ("mariadb", "postgresql", "oracle")
EXPECTED_RUNTIME_OWNERS = {
    "cpf": "cpf-core",
    "cmn": "cpf-common",
    "adm": "cpf-admin",
    "backoffice": "cpf-backoffice",
    "bat": "cpf-batch",
    "gwy": "cpf-gateway",
    "ref": "cpf-education",
}
REQUIRED_LIFECYCLE_KEYS = (
    "provision",
    "emptyInstall",
    "productSeed",
    "optionalSampleSeed",
    "testSeed",
    "verify",
    "migration",
    "rollback",
)
REQUIRED_PACK_LIFECYCLE_STATUS = (
    "provision",
    "install",
    "seed",
    "migration",
    "verify",
    "rollback",
)
REQUIRED_STAGES = {
    "baseline-install",
    "sequential-upgrade",
    "runtime-query",
    "schema-drift",
    "reverse-rollback",
    "forward-reapply",
    "backup-restore",
    "point-in-time-recovery",
}
REQUIRED_EVIDENCE = {
    "sourceSha",
    "vendor",
    "databaseVersion",
    "command",
    "startedAt",
    "endedAt",
    "exitCode",
    "stageResults",
    "schemaHashBefore",
    "schemaHashAfter",
    "sanitized",
}
MIGRATION_RE = re.compile(r"^V(?P<version>\d+)__(?P<description>.+)\.sql$")
ROLLBACK_PATTERNS = (
    re.compile(r"^[RU](?P<version>\d+)__(?P<description>.+)\.sql$"),
    re.compile(r"^V(?P<version>\d+)__(?P<description>.+)_rollback\.sql$"),
)
CHECKSUM_RE = re.compile(r"^(?P<hash>[0-9a-fA-F]{64})\s+\*?(?P<name>V\d+__.+\.sql)$")
SHA_RE = re.compile(r"[0-9a-f]{40}")
SHA256_RE = re.compile(r"[0-9a-f]{64}")


class ContractError(RuntimeError):
    """Raised for a fail-closed repository contract violation."""


@dataclasses.dataclass(frozen=True)
class Migration:
    vendor: str
    pack: str
    version: int
    description: str
    path: Path
    checksum: str

    @property
    def key(self) -> str:
        return f"{self.vendor}/{self.pack}/{self.path.name}"


@dataclasses.dataclass(frozen=True)
class Classification:
    strategy: str
    reason: str
    recovery_plan: tuple[str, ...]
    rollback_paths: tuple[str, ...] = ()
    rule_id: str = ""


@dataclasses.dataclass(frozen=True)
class VendorContext:
    vendor: str
    entry: Mapping[str, Any]
    pack: Mapping[str, Any]
    vendor_root: Path
    migration_root: Path
    rollback_root: Path
    lifecycle_files: Mapping[str, Path]
    runtime_root: Path
    template_root: Path


def utc_now() -> str:
    return dt.datetime.now(dt.timezone.utc).isoformat().replace("+00:00", "Z")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def ensure_within_root(root: Path, candidate: Path, label: str) -> Path:
    root_resolved = root.resolve()
    candidate_resolved = candidate.resolve()
    try:
        candidate_resolved.relative_to(root_resolved)
    except ValueError as exc:
        raise ContractError(f"{label} escapes repository root: {candidate}") from exc
    return candidate_resolved


def load_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8-sig"))
    except (OSError, UnicodeError, json.JSONDecodeError) as exc:
        raise ContractError(f"cannot read JSON {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise ContractError(f"JSON root must be an object: {path}")
    return value


def require_file(root: Path, relative: str, label: str, *, nonempty: bool = True) -> Path:
    if not relative or not isinstance(relative, str):
        raise ContractError(f"{label} path is missing")
    candidate = ensure_within_root(root, root / relative, label)
    if not candidate.is_file():
        raise ContractError(f"{label} missing: {relative}")
    if nonempty and candidate.stat().st_size == 0:
        raise ContractError(f"{label} is empty: {relative}")
    return candidate


def require_directory(root: Path, relative: str, label: str, *, nonempty: bool = True) -> Path:
    if not relative or not isinstance(relative, str):
        raise ContractError(f"{label} path is missing")
    candidate = ensure_within_root(root, root / relative, label)
    if not candidate.is_dir():
        raise ContractError(f"{label} missing: {relative}")
    if nonempty and not any(candidate.iterdir()):
        raise ContractError(f"{label} is empty: {relative}")
    return candidate


def require_policy_file(root: Path, path: Path) -> Path:
    candidate = path if path.is_absolute() else root / path
    candidate = ensure_within_root(root, candidate, "migration lifecycle policy")
    if not candidate.is_file():
        raise ContractError(f"migration lifecycle policy missing: {candidate}")
    return candidate


def resolve_source_provenance(root: Path, supplied_sha: str, supplied_identity: str) -> tuple[str, str, str]:
    """Resolve evidence provenance without making Git the Source authority.

    Current Working Tree SHA-256 is authoritative when explicitly supplied. A local Git
    SHA is retained only as optional read-only provenance/legacy compatibility.
    """
    identity=(supplied_identity or "").strip().lower()
    sha=(supplied_sha or "").strip().lower()
    if identity and not SHA256_RE.fullmatch(identity):
        raise ContractError("--source-identity-sha256 must be an exact 64-character lowercase SHA-256")
    if sha and not SHA_RE.fullmatch(sha):
        raise ContractError("--source-sha must be an exact 40-character lowercase SHA")
    if identity:
        return (sha or "UNAVAILABLE", identity, "CANONICAL_WORKING_TREE_SHA256")
    if sha:
        return (sha, "", "EXPLICIT_LOCAL_GIT_SHA_COMPATIBILITY")
    try:
        result=subprocess.run(["git","-C",str(root),"rev-parse","HEAD"],check=True,text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        value=result.stdout.strip().lower()
        if SHA_RE.fullmatch(value):
            return (value,"","LOCAL_GIT_READ_ONLY_PROVENANCE")
    except (OSError,subprocess.CalledProcessError):
        pass
    raise ContractError("canonical Working Tree source identity unavailable; pass --source-identity-sha256 (Git recovery is not attempted)")


def parse_checksum_manifest(pack_dir: Path) -> dict[str, str]:
    manifest = pack_dir / "checksums.sha256"
    if not manifest.is_file():
        raise ContractError(f"checksum manifest missing: {manifest}")
    entries: dict[str, str] = {}
    versions: dict[int, str] = {}
    for line_number, raw in enumerate(manifest.read_text(encoding="utf-8-sig").splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        match = CHECKSUM_RE.fullmatch(line)
        if not match:
            raise ContractError(f"malformed checksum line {manifest}:{line_number}: {raw!r}")
        name = match.group("name")
        migration_match = MIGRATION_RE.fullmatch(name)
        if migration_match is None:
            raise ContractError(f"invalid migration filename in checksum manifest: {manifest}/{name}")
        version = int(migration_match.group("version"))
        if name in entries:
            raise ContractError(f"duplicate checksum filename: {manifest}/{name}")
        if version in versions:
            raise ContractError(
                f"duplicate Flyway version V{version} in {manifest}: {versions[version]} and {name}"
            )
        entries[name] = match.group("hash").lower()
        versions[version] = name
    if not entries:
        raise ContractError(f"checksum manifest contains no migrations: {manifest}")
    return entries


def discover_migrations(vendor: str, migration_root: Path) -> list[Migration]:
    if not migration_root.is_dir():
        raise ContractError(f"migration root missing for {vendor}: {migration_root}")
    pack_dirs = sorted(
        {path.parent for path in migration_root.rglob("V*.sql") if MIGRATION_RE.fullmatch(path.name)},
        key=lambda item: item.as_posix(),
    )
    if not pack_dirs:
        raise ContractError(f"no migration packs found for {vendor}: {migration_root}")
    migrations: list[Migration] = []
    for pack_dir in pack_dirs:
        entries = parse_checksum_manifest(pack_dir)
        files = {
            path.name: path
            for path in pack_dir.glob("V*.sql")
            if path.is_file() and MIGRATION_RE.fullmatch(path.name)
        }
        missing_from_manifest = sorted(set(files) - set(entries))
        missing_from_disk = sorted(set(entries) - set(files))
        if missing_from_manifest:
            raise ContractError(
                f"migrations missing from checksum manifest {pack_dir}: {missing_from_manifest}"
            )
        if missing_from_disk:
            raise ContractError(
                f"checksum manifest points to missing migrations {pack_dir}: {missing_from_disk}"
            )
        pack = pack_dir.relative_to(migration_root).as_posix()
        if pack == ".":
            pack = "root"
        for name, path in sorted(files.items()):
            match = MIGRATION_RE.fullmatch(name)
            assert match is not None
            actual = sha256(path)
            expected = entries[name]
            if actual != expected:
                raise ContractError(
                    f"migration checksum mismatch: {path} expected={expected} actual={actual}"
                )
            migrations.append(
                Migration(
                    vendor=vendor,
                    pack=pack,
                    version=int(match.group("version")),
                    description=match.group("description"),
                    path=path,
                    checksum=actual,
                )
            )
    return migrations


def sql_has_executable_content(path: Path) -> bool:
    text = path.read_text(encoding="utf-8-sig", errors="strict")
    without_block = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    lines = [
        line.strip()
        for line in without_block.splitlines()
        if line.strip() and not line.lstrip().startswith("--")
    ]
    return bool(lines)


def discover_rollbacks(rollback_root: Path) -> dict[tuple[str, int], list[Path]]:
    by_version: dict[tuple[str, int], list[Path]] = defaultdict(list)
    if not rollback_root.is_dir():
        return by_version
    for path in rollback_root.rglob("*.sql"):
        if not path.is_file():
            continue
        for pattern in ROLLBACK_PATTERNS:
            match = pattern.fullmatch(path.name)
            if match:
                if not sql_has_executable_content(path):
                    marker = "CPF_FORWARD_RECOVERY_ONLY"
                    text = path.read_text(encoding="utf-8-sig", errors="strict")
                    if marker in text:
                        break
                    raise ContractError(f"rollback SQL has no executable content: {path}")
                pack = path.parent.relative_to(rollback_root).as_posix()
                if pack == ".":
                    pack = "root"
                by_version[(pack, int(match.group("version")))].append(path)
                break
    return by_version


def validate_state_contract(policy: Mapping[str, Any]) -> None:
    required_states = {
        "NOT_STARTED",
        "RUNNING",
        "APPLIED",
        "ROLLED_BACK",
        "FORWARD_RECOVERED",
        "FAILED",
        "UNKNOWN",
    }
    states = set(policy.get("stateModel", {}).get("states", []))
    missing = sorted(required_states - states)
    if missing:
        raise ContractError(f"migration lifecycle state model missing: {missing}")
    unknown = policy.get("unknownResultPolicy")
    if not isinstance(unknown, dict):
        raise ContractError("unknownResultPolicy must be an object")
    for field in ("failClosed", "reconcileRequired", "automaticRetryAllowed", "reconcileIdentity"):
        if field not in unknown:
            raise ContractError(f"unknownResultPolicy missing field: {field}")
    if unknown.get("failClosed") is not True or unknown.get("reconcileRequired") is not True:
        raise ContractError("UNKNOWN must fail closed and require reconciliation")
    if unknown.get("automaticRetryAllowed") is not False:
        raise ContractError("UNKNOWN must not permit automatic retry before reconciliation")
    if unknown.get("reconcileIdentity") != "vendor|logicalDatabase|version|checksumSha256":
        raise ContractError("UNKNOWN reconciliation identity is not stable")
    partial = policy.get("partialFailurePolicy")
    if not isinstance(partial, dict) or partial.get("checkpointRequired") is not True:
        raise ContractError("partialFailurePolicy.checkpointRequired must be true")
    if partial.get("stopAtFirstFailedStatement") is not True:
        raise ContractError("partialFailurePolicy.stopAtFirstFailedStatement must be true")
    if partial.get("subsequentMigrationBlocked") is not True:
        raise ContractError("partialFailurePolicy.subsequentMigrationBlocked must be true")
    if not partial.get("requiredEvidence"):
        raise ContractError("partialFailurePolicy.requiredEvidence must not be empty")
    reapply = policy.get("reapplyPolicy")
    if not isinstance(reapply, dict):
        raise ContractError("reapplyPolicy must be an object")
    if reapply.get("checksumMustMatch") is not True or reapply.get("duplicateApplyMustFailClosed") is not True:
        raise ContractError("reapplyPolicy must require matching checksum and fail closed on duplicate apply")
    if reapply.get("driftResult") != "FAILED_RECONCILIATION_REQUIRED":
        raise ContractError("reapplyPolicy drift must require reconciliation")


def match_override(migration: Migration, overrides: Iterable[dict[str, Any]]) -> dict[str, Any] | None:
    matches: list[dict[str, Any]] = []
    for rule in overrides:
        if not isinstance(rule, dict):
            raise ContractError("migration policy override must be an object")
        vendors = rule.get("vendors", OFFICIAL_VENDORS)
        if not isinstance(vendors, list):
            raise ContractError(f"invalid vendors selector in policy rule {rule.get('id')}")
        if migration.vendor not in vendors:
            continue
        if not fnmatch.fnmatchcase(migration.pack, str(rule.get("packPattern", "*"))):
            continue
        if not fnmatch.fnmatchcase(migration.path.name, str(rule.get("migrationPattern", "V*.sql"))):
            continue
        versions = rule.get("versions")
        if versions is not None:
            allowed: set[int] = set()
            if isinstance(versions, list):
                try:
                    allowed = {int(item) for item in versions}
                except (TypeError, ValueError) as exc:
                    raise ContractError(
                        f"invalid versions selector in policy rule {rule.get('id')}"
                    ) from exc
            elif isinstance(versions, str) and re.fullmatch(r"\d+-\d+", versions):
                start, end = (int(item) for item in versions.split("-", 1))
                if start > end:
                    raise ContractError(f"reversed versions selector in policy rule {rule.get('id')}")
                allowed = set(range(start, end + 1))
            else:
                raise ContractError(f"invalid versions selector in policy rule {rule.get('id')}")
            if migration.version not in allowed:
                continue
        matches.append(rule)
    if len(matches) > 1:
        raise ContractError(
            f"ambiguous recovery policy for {migration.key}: {[rule.get('id') for rule in matches]}"
        )
    return matches[0] if matches else None


def classify_migration(
    root: Path,
    migration: Migration,
    rollback_paths: list[Path],
    policy: Mapping[str, Any],
) -> Classification:
    override = match_override(migration, policy.get("overrides", []))
    if override is not None:
        strategy = str(override.get("strategy", "")).upper()
        reason = str(override.get("reason", "")).strip()
        plans = tuple(str(item) for item in override.get("recoveryPlan", []))
        rule_id = str(override.get("id", ""))
    elif rollback_paths:
        strategy = "ROLLBACK"
        reason = "matching rollback artifact discovered in the same logical database pack"
        plans = ()
        rule_id = "IMPLICIT-ROLLBACK-ARTIFACT"
    else:
        defaults = policy.get("unpairedMigrationDefault", {})
        strategy = str(defaults.get("strategy", "")).upper()
        reason = str(defaults.get("reason", "")).strip()
        plans = tuple(str(item) for item in defaults.get("recoveryPlan", []))
        rule_id = str(defaults.get("id", "UNPAIRED-DEFAULT"))
    if strategy not in {"ROLLBACK", "FORWARD_RECOVERY"}:
        raise ContractError(f"migration lacks rollback/forward-recovery classification: {migration.key}")
    if not reason:
        raise ContractError(f"migration classification lacks reason: {migration.key}")
    if strategy == "ROLLBACK":
        if not rollback_paths:
            raise ContractError(f"ROLLBACK classification lacks rollback SQL: {migration.key}")
    else:
        if not plans:
            raise ContractError(f"FORWARD_RECOVERY classification lacks recoveryPlan: {migration.key}")
        for relative in plans:
            require_file(root, relative, f"forward recovery plan for {migration.key}")
    return Classification(
        strategy=strategy,
        reason=reason,
        recovery_plan=plans,
        rollback_paths=tuple(path.relative_to(root).as_posix() for path in sorted(rollback_paths)),
        rule_id=rule_id,
    )


def validate_manifest_identity(manifest: Mapping[str, Any], contract: Mapping[str, Any], policy: Mapping[str, Any]) -> None:
    supported = tuple(manifest.get("supportedVendors", []))
    official = tuple(manifest.get("officialVendors", []))
    if supported != OFFICIAL_VENDORS:
        raise ContractError(f"supportedVendors must be exactly {OFFICIAL_VENDORS}, actual={supported}")
    if official != OFFICIAL_VENDORS:
        raise ContractError(f"officialVendors must be exactly {OFFICIAL_VENDORS}, actual={official}")
    if tuple(contract.get("officialVendors", [])) != OFFICIAL_VENDORS:
        raise ContractError("DB lifecycle contract officialVendors mismatch")
    if tuple(policy.get("officialVendors", [])) != OFFICIAL_VENDORS:
        raise ContractError("migration policy officialVendors mismatch")
    candidates = manifest.get("candidateVendors", [])
    if candidates not in ([], None):
        raise ContractError(f"candidateVendors must be empty for the official release gate: {candidates}")


def validate_contract_files(root: Path, contract: Mapping[str, Any]) -> dict[str, Path]:
    runtime_executor = require_file(root, str(contract.get("runtimeExecutor", "")), "runtime executor")
    backup = require_file(root, str(contract.get("backupContract", "")), "backupContract")
    pitr = require_file(root, str(contract.get("pitrContract", "")), "pitrContract")
    actual_stages = set(contract.get("orderedStages", []))
    missing_stages = sorted(REQUIRED_STAGES - actual_stages)
    if missing_stages:
        raise ContractError(f"DB lifecycle orderedStages missing: {missing_stages}")
    evidence = set(contract.get("evidenceRequired", []))
    missing_evidence = sorted(REQUIRED_EVIDENCE - evidence)
    if missing_evidence:
        raise ContractError(f"DB lifecycle evidenceRequired missing: {missing_evidence}")
    static_gates = contract.get("requiredStaticGates", [])
    if not isinstance(static_gates, list) or not static_gates:
        raise ContractError("requiredStaticGates must be a non-empty list")
    verifier_rel = "cpf-tools/db/verify_migration_lifecycle.py"
    if verifier_rel not in static_gates:
        raise ContractError("requiredStaticGates does not consume verify_migration_lifecycle.py")
    for relative in static_gates:
        require_file(root, str(relative), f"required static gate {relative}")
    discovery = contract.get("migrationDiscoveryPolicy", {})
    expected_discovery = {
        "mode": "ALL_CHECKSUM_MANIFEST_MIGRATIONS",
        "hardCodedVersionAllowlistForbidden": True,
        "recursiveLogicalDatabasePacks": True,
        "rollbackOrForwardRecoveryRequired": True,
        "unknownResultFailClosed": True,
        "reapplyChecksumLocked": True,
    }
    for key, expected in expected_discovery.items():
        if discovery.get(key) != expected:
            raise ContractError(f"migrationDiscoveryPolicy.{key} must be {expected!r}")
    runtime_requirements = contract.get("runtimeExecutorRequirements", {})
    expected_runtime = {
        "migrationSelection": "ALL_DISCOVERED_FROM_CHECKSUM_MANIFEST",
        "partialFailureCheckpoint": True,
        "unknownResultReconcileBeforeRetry": True,
        "reverseRollbackThenForwardReapply": True,
        "backupManifestRequiredForDestructiveTransition": True,
    }
    for key, expected in expected_runtime.items():
        if runtime_requirements.get(key) != expected:
            raise ContractError(f"runtimeExecutorRequirements.{key} must be {expected!r}")
    return {"runtimeExecutor": runtime_executor, "backupContract": backup, "pitrContract": pitr}


def lifecycle_root_from_template(
    root: Path, template: str, label: str, *, nonempty: bool = True
) -> Path:
    if "{logicalDatabase}" in template:
        prefix = template.split("/{logicalDatabase}", 1)[0]
    else:
        prefix = template
    return require_directory(root, prefix, label, nonempty=nonempty)


def validate_special_pack(root: Path, vendor_root: Path, name: str, declaration: Mapping[str, Any]) -> None:
    required = ("canonicalSource", "freshInstall", "migration", "rollback", "runtimeQueries", "verify", "checksumManifest")
    for key in required:
        value = declaration.get(key)
        if not isinstance(value, str) or not value:
            raise ContractError(f"{name}.{key} is missing")
        path = ensure_within_root(root, vendor_root / value, f"{name}.{key}")
        if not path.is_file() or path.stat().st_size == 0:
            raise ContractError(f"{name}.{key} missing or empty: {path.relative_to(root)}")
    migration = ensure_within_root(root, vendor_root / str(declaration["migration"]), f"{name}.migration")
    manifest = ensure_within_root(root, vendor_root / str(declaration["checksumManifest"]), f"{name}.checksumManifest")
    entries = parse_checksum_manifest(manifest.parent)
    if migration.name not in entries:
        raise ContractError(f"{name} migration missing from checksum manifest: {migration.name}")
    if sha256(migration) != entries[migration.name]:
        raise ContractError(f"{name} migration checksum mismatch: {migration}")


def validate_vendor_context(root: Path, vendor: str, entry: Mapping[str, Any], contract: Mapping[str, Any]) -> VendorContext:
    if not isinstance(entry, dict):
        raise ContractError(f"vendor manifest entry missing: {vendor}")

    # v5 Canonical DB pack: current snapshot is rendered from canonical JSON, while
    # historical migrations remain immutable and are verified independently.
    if entry.get("generatedCurrent") or entry.get("historicalMigration"):
        vendor_root = require_directory(root, f"cpf-tools/db/vendor/{vendor}", f"{vendor} vendorRoot")
        pack_path = require_file(root, f"cpf-tools/db/vendor/{vendor}/pack.json", f"{vendor} pack")
        pack = load_json(pack_path)
        if pack.get("vendor") != vendor:
            raise ContractError(f"vendor pack identity mismatch: expected={vendor} actual={pack.get('vendor')}")
        if pack.get("runtimeVerification") not in {"미검증", "완료", "실패", "재확인 필요"}:
            raise ContractError(f"{vendor} runtimeVerification has invalid status")
        if pack.get("currentSnapshotAuthority") != "CANONICAL_JSON_GENERATED_VENDOR_PACK":
            raise ContractError(f"{vendor} currentSnapshotAuthority mismatch")
        canonical_schema = require_file(root, str(pack.get("canonicalSchema", "")), f"{vendor} canonicalSchema")
        generated_root = require_directory(root, str(pack.get("generatedCurrentRoot", "")), f"{vendor} generatedCurrentRoot")
        template_root = require_directory(root, str(pack.get("generatedDomainTemplateRoot", "")), f"{vendor} generatedDomainTemplateRoot")
        runtime_root = require_directory(root, str(pack.get("runtimeRoot", "")), f"{vendor} runtimeRoot")
        lifecycle_files: dict[str, Path] = {"canonicalSchema": canonical_schema}
        for group in ("freshInstall", "seed", "verify", "currentRollback"):
            values = pack.get(group, {})
            if not isinstance(values, dict) or not values:
                raise ContractError(f"{vendor} {group} current snapshot declaration missing")
            for role, rel in values.items():
                f = require_file(root, str(rel), f"{vendor} {group}.{role}")
                if f.suffix.lower() == ".sql" and not sql_has_executable_content(f):
                    raise ContractError(f"{vendor} {group}.{role} has no executable SQL: {f}")
                lifecycle_files[f"{group}.{role}"] = f
        generated = pack.get("generatedDomainContract", {})
        if generated.get("businessDatabaseRole") != "CUSTOMER_BUSINESS_DB":
            raise ContractError(f"{vendor} generated domain role must be CUSTOMER_BUSINESS_DB")
        if generated.get("generatedSourceVendorFork") is not False:
            raise ContractError(f"{vendor} generated source vendor fork must be false")
        contract_vendor = contract.get("vendorContracts", {}).get(vendor, {})
        migration_template = str(contract_vendor.get("migrationRoot") or pack.get("historicalMigrationRoot") or "")
        rollback_template = str(contract_vendor.get("rollbackRoot") or pack.get("historicalRollbackRoot") or "")
        migration_root = lifecycle_root_from_template(root, migration_template, f"{vendor} migration root")
        rollback_root = lifecycle_root_from_template(root, rollback_template, f"{vendor} rollback root", nonempty=False)
        expected_manifest_migration = str(entry.get("historicalMigration") or pack.get("historicalMigrationRoot") or "")
        if expected_manifest_migration and not (root/expected_manifest_migration).exists():
            raise ContractError(f"{vendor} historicalMigration missing: {expected_manifest_migration}")
        return VendorContext(
            vendor=vendor, entry=entry, pack=pack, vendor_root=vendor_root,
            migration_root=migration_root, rollback_root=rollback_root,
            lifecycle_files=lifecycle_files, runtime_root=runtime_root, template_root=template_root,
        )

    # Legacy fixture/pack compatibility used by mutation tests.
    lifecycle = entry.get("lifecycle")
    if not isinstance(lifecycle, dict):
        raise ContractError(f"vendor lifecycle entry missing: {vendor}")
    missing_keys = [key for key in REQUIRED_LIFECYCLE_KEYS if not lifecycle.get(key)]
    if missing_keys:
        raise ContractError(f"{vendor} lifecycle paths missing: {missing_keys}")
    vendor_root = require_directory(root, str(entry.get("vendorRoot", "")), f"{vendor} vendorRoot")
    pack_path = require_file(root, str(entry.get("pack", "")), f"{vendor} pack")
    pack = load_json(pack_path)
    if pack.get("vendor") != vendor:
        raise ContractError(f"vendor pack identity mismatch: expected={vendor} actual={pack.get('vendor')}")
    if pack.get("canonicalConsumerRoot") != entry.get("vendorRoot"):
        raise ContractError(f"{vendor} canonicalConsumerRoot does not match vendorRoot")
    if pack.get("runtimeVerification") not in {"미검증", "완료", "실패", "재확인 필요"}:
        raise ContractError(f"{vendor} runtimeVerification has invalid status")
    lifecycle_status = pack.get("lifecycleStatus", {})
    for key in REQUIRED_PACK_LIFECYCLE_STATUS:
        if lifecycle_status.get(key) != "완료":
            raise ContractError(f"{vendor} lifecycleStatus.{key} must be 완료")
    lifecycle_files: dict[str, Path] = {}
    for key in ("provision", "emptyInstall", "productSeed", "optionalSampleSeed", "testSeed", "verify"):
        lifecycle_files[key] = require_file(root, str(lifecycle[key]), f"{vendor} lifecycle {key}")
        if lifecycle_files[key].suffix.lower() == ".sql" and not sql_has_executable_content(lifecycle_files[key]):
            raise ContractError(f"{vendor} lifecycle {key} has no executable SQL: {lifecycle_files[key]}")
    migration_root = lifecycle_root_from_template(root, str(lifecycle["migration"]), f"{vendor} migration root")
    rollback_root = lifecycle_root_from_template(
        root, str(lifecycle["rollback"]), f"{vendor} rollback root", nonempty=False
    )
    runtime_root = require_directory(root, str(entry.get("runtimeRoot", "")), f"{vendor} runtimeRoot")
    template_root = require_directory(root, str(entry.get("domainTemplateRoot", "")), f"{vendor} domainTemplateRoot")
    generated = pack.get("generatedDomainContract", {})
    if generated.get("metadataDriven") is not True or generated.get("fixedDomainList") is not False:
        raise ContractError(f"{vendor} generated domain contract is not metadata-driven")
    if generated.get("templateRoot") != "domain-template":
        raise ContractError(f"{vendor} generated domain templateRoot mismatch")
    require_file(root, str(generated.get("databaseBootstrapScript", "")), f"{vendor} databaseBootstrapScript")
    expected_migration = str(lifecycle["migration"])
    expected_rollback = str(lifecycle["rollback"])
    if pack.get("migrationLocationPattern") != expected_migration:
        raise ContractError(f"{vendor} migrationLocationPattern mismatch")
    if pack.get("rollbackLocationPattern") != expected_rollback:
        raise ContractError(f"{vendor} rollbackLocationPattern mismatch")
    declared_modules = pack.get("runtimeModules", {})
    if not isinstance(declared_modules, dict) or not declared_modules:
        raise ContractError(f"{vendor} runtimeModules must not be empty")
    for module, module_contract in declared_modules.items():
        expected_owner = EXPECTED_RUNTIME_OWNERS.get(module)
        if expected_owner is None:
            raise ContractError(f"{vendor} declares unknown runtime module: {module}")
        if not isinstance(module_contract, dict) or module_contract.get("ownerArtifact") != expected_owner:
            raise ContractError(f"{vendor} runtime module owner mismatch: {module}")
        module_dir = runtime_root / module
        if not module_dir.is_dir() or not any(module_dir.iterdir()):
            raise ContractError(f"{vendor} runtime module consumer missing or empty: {module_dir.relative_to(root)}")
    contract_vendor = contract.get("vendorContracts", {}).get(vendor, {})
    if contract_vendor:
        if contract_vendor.get("migrationRoot") != expected_migration:
            raise ContractError(f"DB lifecycle vendorContracts.{vendor}.migrationRoot mismatch")
        if contract_vendor.get("rollbackRoot") != expected_rollback:
            raise ContractError(f"DB lifecycle vendorContracts.{vendor}.rollbackRoot mismatch")
    for special_name in ("operationLedger", "referenceBatchPack"):
        declaration = pack.get(special_name)
        if declaration is not None:
            if not isinstance(declaration, dict):
                raise ContractError(f"{vendor} {special_name} must be an object")
            validate_special_pack(root, vendor_root, f"{vendor}.{special_name}", declaration)
    return VendorContext(
        vendor=vendor,
        entry=entry,
        pack=pack,
        vendor_root=vendor_root,
        migration_root=migration_root,
        rollback_root=rollback_root,
        lifecycle_files=lifecycle_files,
        runtime_root=runtime_root,
        template_root=template_root,
    )


def verify(root: Path, policy_path: Path, source_sha: str, source_identity_sha256: str = "") -> dict[str, Any]:
    started_at = utc_now()
    root = root.resolve()
    source_sha, source_identity_sha256, source_authority = resolve_source_provenance(root, source_sha, source_identity_sha256)
    manifest = load_json(require_file(root, "cpf-tools/db/vendor-pack-manifest.json", "vendor pack manifest"))
    contract = load_json(require_file(root, "cpf-tools/db/cpf-db-lifecycle-contract.json", "DB lifecycle contract"))
    policy_path = require_policy_file(root, policy_path)
    policy = load_json(policy_path)
    validate_state_contract(policy)
    validate_manifest_identity(manifest, contract, policy)
    contract_files = validate_contract_files(root, contract)

    results: list[dict[str, Any]] = []
    vendor_counts: dict[str, int] = {}
    vendor_pack_counts: dict[str, int] = {}
    lifecycle_file_counts: dict[str, int] = {}
    for vendor in OFFICIAL_VENDORS:
        entry = manifest.get("vendors", {}).get(vendor)
        context = validate_vendor_context(root, vendor, entry, contract)
        migrations = discover_migrations(vendor, context.migration_root)
        rollbacks = discover_rollbacks(context.rollback_root)
        vendor_counts[vendor] = len(migrations)
        vendor_pack_counts[vendor] = len({migration.pack for migration in migrations})
        lifecycle_file_counts[vendor] = len(context.lifecycle_files)
        for migration in migrations:
            version_candidates = rollbacks.get((migration.pack, migration.version), [])
            exact_candidates = []
            for candidate in version_candidates:
                for pattern in ROLLBACK_PATTERNS:
                    match = pattern.fullmatch(candidate.name)
                    if match and match.group("description") == migration.description:
                        exact_candidates.append(candidate)
                        break
            candidates = exact_candidates
            if len(candidates) > 1:
                raise ContractError(
                    f"multiple exact rollback artifacts match {migration.key}: "
                    f"{[path.relative_to(root).as_posix() for path in candidates]}"
                )
            classification = classify_migration(root, migration, candidates, policy)
            results.append(
                {
                    "vendor": vendor,
                    "pack": migration.pack,
                    "version": migration.version,
                    "migration": migration.path.relative_to(root).as_posix(),
                    "checksumSha256": migration.checksum,
                    "strategy": classification.strategy,
                    "ruleId": classification.rule_id,
                    "reason": classification.reason,
                    "rollbackPaths": list(classification.rollback_paths),
                    "recoveryPlan": list(classification.recovery_plan),
                    "idempotencyKey": f"{vendor}|{migration.pack}|{migration.version}|{migration.checksum}",
                }
            )

    if any(count == 0 for count in vendor_counts.values()):
        raise ContractError(f"official vendor has zero migrations: {vendor_counts}")
    ended_at = utc_now()
    return {
        "schemaVersion": 2,
        "gate": "CPF-DB-MIGRATION-LIFECYCLE",
        "result": "PASS",
        "exitCode": 0,
        "sourceSha": source_sha,
        "sourceIdentitySha256": source_identity_sha256,
        "sourceAuthority": source_authority,
        "startedAt": started_at,
        "endedAt": ended_at,
        "officialVendors": list(OFFICIAL_VENDORS),
        "vendorMigrationCounts": vendor_counts,
        "vendorLogicalPackCounts": vendor_pack_counts,
        "vendorLifecycleFileCounts": lifecycle_file_counts,
        "migrationCount": len(results),
        "rollbackClassified": sum(item["strategy"] == "ROLLBACK" for item in results),
        "forwardRecoveryClassified": sum(item["strategy"] == "FORWARD_RECOVERY" for item in results),
        "runtimeExecutor": contract_files["runtimeExecutor"].relative_to(root).as_posix(),
        "backupContractSha256": sha256(contract_files["backupContract"]),
        "pitrContractSha256": sha256(contract_files["pitrContract"]),
        "policySha256": sha256(policy_path),
        "unknownResultPolicy": policy["unknownResultPolicy"],
        "partialFailurePolicy": policy["partialFailurePolicy"],
        "reapplyPolicy": policy["reapplyPolicy"],
        "migrations": sorted(results, key=lambda item: (item["vendor"], item["pack"], item["version"])),
        "sanitized": True,
    }


def write_report(path: Path | None, report: Mapping[str, Any]) -> None:
    text = json.dumps(report, ensure_ascii=False, indent=2, sort_keys=False) + "\n"
    if path is None:
        sys.stdout.write(text)
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path.cwd(), help="CPF repository root")
    parser.add_argument(
        "--policy", type=Path, default=Path("cpf-tools/db/migration-lifecycle-policy.json"),
        help="policy path, absolute or relative to repository root",
    )
    parser.add_argument("--report", type=Path, help="write sanitized JSON evidence")
    parser.add_argument("--source-sha", default="", help="optional exact 40-character local Git provenance SHA")
    parser.add_argument("--source-identity-sha256", default="", help="authoritative current Working Tree 64-character SHA-256")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    root = args.root.resolve()
    policy = args.policy if args.policy.is_absolute() else root / args.policy
    try:
        report = verify(root, policy, args.source_sha, args.source_identity_sha256)
    except ContractError as exc:
        failure = {
            "schemaVersion": 2,
            "gate": "CPF-DB-MIGRATION-LIFECYCLE",
            "result": "FAIL",
            "exitCode": 2,
            "error": str(exc),
            "sanitized": True,
        }
        write_report(args.report, failure)
        print(f"[CPF][DB][FAIL] {exc}", file=sys.stderr)
        return 2
    except Exception as exc:  # defensive environment boundary
        failure = {
            "schemaVersion": 2,
            "gate": "CPF-DB-MIGRATION-LIFECYCLE",
            "result": "ERROR",
            "exitCode": 3,
            "error": f"{type(exc).__name__}: {exc}",
            "sanitized": True,
        }
        write_report(args.report, failure)
        print(f"[CPF][DB][ERROR] {type(exc).__name__}: {exc}", file=sys.stderr)
        return 3
    write_report(args.report, report)
    print(
        "[CPF][DB][PASS] "
        f"migrations={report['migrationCount']} "
        f"rollback={report['rollbackClassified']} "
        f"forwardRecovery={report['forwardRecoveryClassified']}",
        file=sys.stderr,
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

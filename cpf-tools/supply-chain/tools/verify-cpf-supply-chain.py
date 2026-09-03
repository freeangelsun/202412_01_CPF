#!/usr/bin/env python3
"""Fail-closed CPF release artifact, environment, license and supply-chain verifier.

Static mode validates repository policy/catalog coverage. Release mode additionally
requires exact-SHA sanitized environment and generated supply-chain evidence.
The verifier is read-only and writes no files inside the repository.
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
import json
import re
import subprocess
import sys
from pathlib import Path, PurePosixPath
from typing import Any

SHA256_RE = re.compile(r"^[0-9a-f]{64}$")
SHA_RE = re.compile(r"^[0-9a-f]{40}$")
QUOTED = re.compile(r"['\"]([^'\"]+)['\"]")
INCLUDE_LINE = re.compile(r"^\s*include\s+(.+?)\s*$", re.MULTILINE)
PROJECT_DIR = re.compile(r"project\(['\"]:(?P<name>[^'\"]+)['\"]\)\.projectDir\s*=\s*file\(['\"](?P<path>[^'\"]+)['\"]\)")
REQUIRED_TOOL_NAMES = {"cyclonedx-gradle", "ort", "syft", "grype", "cpf-release-signer"}
OFFICIAL_DBS = {"oracle", "postgresql", "mariadb"}
SUPPORTED_CATALOG_SCHEMAS = {"1", "1.0", "1.0.0", "2.0.0", "2.1.0"}
NON_RELEASE_PROJECT_PREFIXES = ("cpf-tools/verification/", "cpf-tools/testing/cpf-testkit")


def load_json(path: Path, failures: list[str]) -> dict[str, Any]:
    if not path.is_file():
        failures.append(f"missing file: {path}")
        return {}
    try:
        value = json.loads(path.read_text(encoding="utf-8-sig"))
    except Exception as exc:
        failures.append(f"invalid JSON {path}: {exc}")
        return {}
    if not isinstance(value, dict):
        failures.append(f"JSON root must be object: {path}")
        return {}
    return value


def git_head(root: Path) -> str | None:
    try:
        cp = subprocess.run(["git", "-C", str(root), "rev-parse", "HEAD"], check=True, capture_output=True, text=True)
        value = cp.stdout.strip()
        return value if SHA_RE.fullmatch(value) else None
    except (OSError, subprocess.CalledProcessError):
        return None


def project_paths(settings: str) -> set[str]:
    aliases = {m.group("name"): m.group("path").replace("\\", "/").strip("/") for m in PROJECT_DIR.finditer(settings)}
    names: set[str] = set()
    for match in INCLUDE_LINE.finditer(settings):
        for value in QUOTED.findall(match.group(1)):
            name = value.lstrip(":")
            if name:
                names.add(name)
    paths = set()
    for name in names:
        paths.add(aliases.get(name, name.replace(":", "/")))
    return paths


def relative_safe(value: str) -> bool:
    if not value or "\\" in value:
        return False
    p = PurePosixPath(value)
    return not p.is_absolute() and ".." not in p.parts


def validate_environment(data: dict[str, Any], expected_sha: str | None, release: bool, failures: list[str]) -> None:
    if data.get("schemaVersion") != 1:
        failures.append("environment manifest schemaVersion must be 1")
    if data.get("sanitized") is not True:
        failures.append("environment manifest must declare sanitized=true")
    source_sha = data.get("sourceSha")
    if not isinstance(source_sha, str) or not SHA_RE.fullmatch(source_sha):
        failures.append("environment manifest sourceSha must be a 40-char lowercase SHA")
    elif expected_sha and source_sha != expected_sha:
        failures.append(f"environment manifest SHA mismatch expected={expected_sha} actual={source_sha}")
    tools = data.get("tools")
    if not isinstance(tools, list):
        failures.append("environment manifest tools must be an array")
    else:
        names = {str(x.get("name", "")) for x in tools if isinstance(x, dict)}
        for required in {"java", "gradle-wrapper", "node", "npm", "python", "powershell"} - names:
            failures.append(f"environment manifest tool missing: {required}")
        if release:
            versions = {str(x.get("name")): str(x.get("version", "")) for x in tools if isinstance(x, dict)}
            if not versions.get("java", "").startswith("25"):
                failures.append("release environment requires Java 25")
            if not versions.get("node", "").startswith("22"):
                failures.append("release environment requires Node 22")
    databases = data.get("databases")
    if not isinstance(databases, list):
        failures.append("environment manifest databases must be an array")
    else:
        vendors = {str(x.get("vendor", "")).lower() for x in databases if isinstance(x, dict)}
        if vendors != OFFICIAL_DBS:
            failures.append(f"environment database vendors must be exactly {sorted(OFFICIAL_DBS)}: {sorted(vendors)}")
        if release:
            unavailable = [str(x.get("vendor")) for x in databases if isinstance(x, dict) and x.get("available") is not True]
            if unavailable:
                failures.append(f"release database environments unavailable: {','.join(unavailable)}")
    browsers = data.get("browsers")
    if not isinstance(browsers, list):
        failures.append("environment manifest browsers must be an array")
    else:
        names = {str(x.get("name", "")).lower() for x in browsers if isinstance(x, dict)}
        if names != {"chromium", "firefox", "webkit"}:
            failures.append(f"environment browsers must be chromium/firefox/webkit: {sorted(names)}")
        if release and any(x.get("available") is not True for x in browsers if isinstance(x, dict)):
            failures.append("release browser environment is incomplete")


def sbom_licenses(sbom: dict[str, Any], denied: set[str], failures: list[str]) -> None:
    components = sbom.get("components", [])
    if not isinstance(components, list):
        failures.append("SBOM components must be an array")
        return
    for component in components:
        if not isinstance(component, dict):
            failures.append("SBOM component must be an object")
            continue
        licenses: list[str] = []
        for item in component.get("licenses", []) or []:
            if not isinstance(item, dict):
                continue
            lic = item.get("license", {})
            if isinstance(lic, dict):
                licenses.append(str(lic.get("id") or lic.get("name") or "UNKNOWN"))
        if not licenses:
            licenses = ["UNKNOWN"]
        bad = [lic for lic in licenses if lic in denied]
        if bad:
            failures.append(f"denied/unknown license {component.get('name', '<unnamed>')}:{bad}")


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", type=Path, default=Path.cwd())
    ap.add_argument("--evidence-dir", type=Path)
    ap.add_argument("--sbom", type=Path)
    ap.add_argument("--environment-manifest", type=Path)
    ap.add_argument("--release", action="store_true")
    args = ap.parse_args()
    root = args.root.resolve()
    failures: list[str] = []
    warnings: list[str] = []

    settings_path = root / "settings.gradle"
    catalog_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    policy_path = root / "cpf-tools/release/supply-chain/cpf-supply-chain-policy.json"
    approved_path = root / "cpf-tools/release/supply-chain/approved-primary-oss.csv"
    notice_path = root / "cpf-docs/legal/THIRD_PARTY_NOTICES.md"
    env_template_path = root / "cpf-tools/governance/cpf-runtime-environment-manifest.template.json"
    env_schema_path = root / "cpf-tools/governance/cpf-runtime-environment-manifest.schema.json"

    for required in (settings_path, approved_path, notice_path, env_template_path, env_schema_path):
        if not required.is_file():
            failures.append(f"missing file: {required}")

    catalog = load_json(catalog_path, failures)
    policy = load_json(policy_path, failures)
    env_template = load_json(env_template_path, failures)
    _ = load_json(env_schema_path, failures)

    if policy:
        allowed = set(map(str, policy.get("allowedLicenses", [])))
        conditional = set(map(str, policy.get("conditionalLicenses", [])))
        denied = set(map(str, policy.get("deniedLicenses", [])))
        if not allowed or not denied or policy.get("failClosed") is not True:
            failures.append("supply-chain policy must define allowed/denied licenses and failClosed=true")
        if (allowed & denied) or (allowed & conditional) or (conditional & denied):
            failures.append("supply-chain license sets must be disjoint")
        tools = {str(x.get("name")) for x in policy.get("requiredTools", []) if isinstance(x, dict)}
        missing_tools = REQUIRED_TOOL_NAMES - tools
        if missing_tools:
            failures.append(f"supply-chain required tools missing: {sorted(missing_tools)}")
    else:
        allowed, conditional, denied = set(), set(), {"UNKNOWN", "NOASSERTION"}

    if approved_path.is_file():
        with approved_path.open(encoding="utf-8-sig", newline="") as handle:
            rows = list(csv.DictReader(handle))
        if not rows:
            failures.append("approved primary OSS catalog is empty")
        for row in rows:
            component = row.get("component") or row.get("name") or "<unknown>"
            version = row.get("version", "")
            source_url = row.get("source_url", "")
            license_id = row.get("license", "UNKNOWN")
            if not version or not source_url.startswith("https://"):
                failures.append(f"incomplete approved component {component}")
            if license_id in denied or license_id not in allowed | conditional:
                failures.append(f"unapproved license in approved component {component}:{license_id}")

    settings_text = settings_path.read_text(encoding="utf-8-sig") if settings_path.is_file() else ""
    included_projects = project_paths(settings_text) if settings_text else set()
    project_alias_by_path = {
        m.group("path").replace("\\", "/").strip("/"): m.group("name")
        for m in PROJECT_DIR.finditer(settings_text)
    }
    expected_projects = {
        path for path in included_projects
        if not any(path.startswith(prefix) for prefix in NON_RELEASE_PROJECT_PREFIXES)
    }
    artifacts = catalog.get("artifacts", []) if catalog else []
    if str(catalog.get("schemaVersion")) not in SUPPORTED_CATALOG_SCHEMAS or not isinstance(artifacts, list) or not artifacts:
        failures.append(
            "final artifact catalog must use a supported schemaVersion "
            f"{sorted(SUPPORTED_CATALOG_SCHEMAS)} with non-empty artifacts"
        )
        artifacts = []
    if set(map(str.lower, catalog.get("officialDatabaseVendors", []))) != OFFICIAL_DBS:
        failures.append("artifact catalog official DB vendors must be Oracle/PostgreSQL/MariaDB only")
    artifact_ids: set[str] = set()
    owner_paths: set[str] = set()
    for artifact in artifacts:
        if not isinstance(artifact, dict):
            failures.append("artifact catalog row must be an object")
            continue
        artifact_id = str(artifact.get("artifactId", ""))
        owner_path = str(artifact.get("ownerPath", "")).strip("/")
        output_pattern = str(artifact.get("outputPattern", ""))
        if not artifact_id or artifact_id in artifact_ids:
            failures.append(f"duplicate/blank artifactId: {artifact_id}")
        artifact_ids.add(artifact_id)
        if not relative_safe(owner_path) or not relative_safe(output_pattern):
            failures.append(f"unsafe artifact path/pattern: {artifact_id}")
        elif not (root / owner_path).exists():
            failures.append(f"artifact ownerPath missing from repository: {artifact_id}:{owner_path}")
        if not artifact.get("producer") or not artifact.get("consumer"):
            failures.append(f"artifact producer/consumer missing: {artifact_id}")
        attestations = artifact.get("requiredAttestations")
        if not isinstance(attestations, list) or "sha256" not in attestations:
            failures.append(f"artifact sha256 attestation missing: {artifact_id}")
        owner_paths.add(owner_path)
    missing_projects = sorted(
        path for path in expected_projects
        if path not in owner_paths and project_alias_by_path.get(path) not in owner_paths
    )
    if missing_projects:
        failures.append(f"included Gradle projects missing from artifact catalog: {missing_projects}")
    database_artifacts = {a.get("ownerPath", "").split("/")[-1] for a in artifacts if isinstance(a, dict) and a.get("kind") == "database-pack"}
    if database_artifacts != OFFICIAL_DBS:
        failures.append(f"database artifact packs must be exactly {sorted(OFFICIAL_DBS)}: {sorted(database_artifacts)}")

    validate_environment(env_template, None, False, failures)

    # 명시적으로 전달된 SBOM은 release 모드 여부와 무관하게 라이선스 정책을 검증한다.
    if args.sbom is not None:
        sbom_licenses(load_json(args.sbom.resolve(), failures), denied, failures)

    head = git_head(root)
    evidence_dir = args.evidence_dir.resolve() if args.evidence_dir else None
    if args.release:
        if evidence_dir is None or not evidence_dir.is_dir():
            failures.append("release evidence directory missing")
        else:
            required_evidence = list(map(str, policy.get("releaseRequiredEvidence", [])))
            for name in required_evidence:
                if not (evidence_dir / name).is_file():
                    failures.append(f"release evidence missing: {name}")
        env_path = args.environment_manifest or (evidence_dir / "environment-manifest.json" if evidence_dir else None)
        if env_path is None:
            failures.append("release environment manifest missing")
            env_data = {}
        else:
            env_data = load_json(env_path, failures)
        expected_sha = head or (env_data.get("sourceSha") if isinstance(env_data, dict) else None)
        validate_environment(env_data, expected_sha, True, failures)

        # 명시 SBOM은 위에서 이미 검증했으므로 release evidence의 기본 SBOM만 추가 검증한다.
        if args.sbom is None:
            sbom_path = evidence_dir / "cyclonedx-bom.json" if evidence_dir else None
            if sbom_path is not None:
                sbom_licenses(load_json(sbom_path, failures), denied, failures)

        if evidence_dir:
            artifact_manifest = load_json(evidence_dir / "artifact-manifest.json", failures)
            manifest_sha = artifact_manifest.get("sourceSha")
            if expected_sha and manifest_sha != expected_sha:
                failures.append(f"artifact manifest SHA mismatch expected={expected_sha} actual={manifest_sha}")
            manifest_rows = artifact_manifest.get("artifacts", [])
            manifest_ids = set()
            if not isinstance(manifest_rows, list):
                failures.append("artifact manifest artifacts must be an array")
            else:
                for row in manifest_rows:
                    if not isinstance(row, dict):
                        failures.append("artifact manifest row must be object")
                        continue
                    manifest_ids.add(str(row.get("artifactId", "")))
                    if not SHA256_RE.fullmatch(str(row.get("sha256", ""))):
                        failures.append(f"artifact manifest invalid sha256: {row.get('artifactId')}")
                missing = artifact_ids - manifest_ids
                if missing:
                    failures.append(f"release artifact manifest coverage missing: {sorted(missing)}")
            sha_lines = (evidence_dir / "artifact-sha256.txt").read_text(encoding="ascii").splitlines() if (evidence_dir / "artifact-sha256.txt").is_file() else []
            sha_rows = {}
            for line in sha_lines:
                parts = line.split(None, 1)
                if len(parts) != 2 or not SHA256_RE.fullmatch(parts[0]):
                    failures.append(f"invalid artifact-sha256 row: {line}")
                    continue
                sha_rows[parts[1].strip()] = parts[0]
            for row in manifest_rows if isinstance(manifest_rows, list) else []:
                artifact_id = str(row.get("artifactId", ""))
                if sha_rows.get(artifact_id) != str(row.get("sha256", "")):
                    failures.append(f"artifact-sha256 mismatch: {artifact_id}")
            license_report = load_json(evidence_dir / "license-report.json", failures)
            if license_report:
                if license_report.get("sourceSha") != expected_sha or license_report.get("status") != "PASS":
                    failures.append("release license report SHA/status mismatch")
                if int(license_report.get("deniedCount", 0)) > 0 or int(license_report.get("unknownCount", 0)) > 0:
                    failures.append("release license report contains denied/unknown findings")
            vulnerability = load_json(evidence_dir / "vulnerability-report.json", failures)
            if vulnerability:
                if vulnerability.get("sourceSha") != expected_sha:
                    failures.append("release vulnerability report SHA mismatch")
                if int(vulnerability.get("critical", 0)) > 0 or int(vulnerability.get("high", 0)) > 0:
                    failures.append("release vulnerability report contains critical/high findings")
            signatures = load_json(evidence_dir / "signature-verification.json", failures)
            sig_rows = signatures.get("artifacts", []) if signatures else []
            if signatures and signatures.get("sourceSha") != expected_sha:
                failures.append("signature verification SHA mismatch")
            if signatures and signatures.get("manifestVerified") is not True:
                failures.append("artifact manifest signature is not verified")
            if not isinstance(sig_rows, list) or not sig_rows:
                failures.append("signature verification artifact list missing")
            else:
                sig_ids = {str(row.get("artifactId", "")) for row in sig_rows if isinstance(row, dict) and row.get("verified") is True}
                if sig_ids != artifact_ids:
                    failures.append(f"signature verification coverage mismatch missing={sorted(artifact_ids-sig_ids)} extra={sorted(sig_ids-artifact_ids)}")
                for row in sig_rows:
                    if not isinstance(row, dict) or row.get("verified") is not True or not SHA256_RE.fullmatch(str(row.get("sha256", ""))):
                        failures.append("one or more release artifact signatures are invalid")
    elif evidence_dir is not None:
        warnings.append("evidence-dir is ignored for release completeness unless --release is specified")

    result = {
        "status": "PASS" if not failures else "FAIL",
        "release": args.release,
        "headSha": head,
        "includedProjectCount": len(included_projects),
        "releaseProjectCount": len(expected_projects),
        "excludedVerificationProjectCount": len(included_projects - expected_projects),
        "artifactCount": len(artifacts),
        "approvedOssCount": len(rows) if approved_path.is_file() else 0,
        "warnings": warnings,
        "failures": failures,
    }
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())

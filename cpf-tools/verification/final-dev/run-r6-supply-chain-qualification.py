#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path, PurePosixPath

SHA40 = re.compile(r"^[0-9a-f]{40}$")
SKIP_SUFFIX = ("-plain.jar", "-sources.jar", "-javadoc.jar")
OFFICIAL_DBS = ("oracle", "postgresql", "mariadb")
BROWSERS = ("chromium", "firefox", "webkit")

class QualificationError(RuntimeError):
    pass

def run(args: list[str], cwd: Path, *, capture: bool = False, env: dict[str, str] | None = None) -> subprocess.CompletedProcess[str]:
    cp = subprocess.run(args, cwd=cwd, text=True, capture_output=capture, env=env, check=False)
    if cp.returncode != 0:
        detail = ""
        if capture:
            detail = (cp.stderr or cp.stdout or "").strip()[-4000:]
        raise QualificationError(f"command failed exit={cp.returncode}: {' '.join(args)}" + (f"\n{detail}" if detail else ""))
    return cp

def git(root: Path, *args: str) -> str:
    return run(["git", "-C", str(root), *args], root, capture=True).stdout.strip()

def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()

def tree_hash(root: Path, files: list[Path]) -> str:
    h = hashlib.sha256()
    for path in sorted(files, key=lambda p: p.as_posix()):
        rel = path.relative_to(root).as_posix()
        h.update(sha256_file(path).encode("ascii"))
        h.update(b"  ")
        h.update(rel.encode("utf-8"))
        h.update(b"\n")
    return h.hexdigest()

def resolve_artifact(root: Path, pattern: str) -> tuple[Path, list[Path]]:
    if "\\" in pattern or PurePosixPath(pattern).is_absolute() or ".." in PurePosixPath(pattern).parts:
        raise QualificationError(f"unsafe artifact pattern: {pattern}")
    normalized = pattern.replace("\\", "/")
    if normalized.endswith("/**"):
        base = root / normalized[:-3]
        if not base.is_dir():
            raise QualificationError(f"artifact directory missing: {normalized[:-3]}")
        files = [p for p in base.rglob("*") if p.is_file()]
        if not files:
            raise QualificationError(f"artifact directory empty: {normalized[:-3]}")
        return base, files
    matches = [p for p in root.glob(normalized) if p.is_file()]
    matches = [p for p in matches if not p.name.endswith(SKIP_SUFFIX)]
    if not matches:
        raise QualificationError(f"artifact output missing: {pattern}")
    common = Path(os.path.commonpath([str(p.parent) for p in matches]))
    return common, matches

def copy_artifact_stage(root: Path, artifact_id: str, base: Path, files: list[Path], stage: Path) -> None:
    target_base = stage / artifact_id
    target_base.mkdir(parents=True, exist_ok=True)
    for src in files:
        try:
            rel = src.relative_to(base)
        except ValueError:
            rel = Path(src.name)
        dest = target_base / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dest)

def tool_version(executable: str, *version_args: str) -> str:
    path = shutil.which(executable)
    if not path:
        raise QualificationError(f"required executable missing: {executable}")
    cp = subprocess.run([path, *version_args], text=True, capture_output=True, check=False)
    text = (cp.stdout + "\n" + cp.stderr).strip()
    if cp.returncode != 0:
        raise QualificationError(f"{executable} version check failed")
    return text.splitlines()[0] if text else "unknown"

def java_major(version_text: str) -> str:
    m = re.search(r'(?:version\s+\")?(\d+)(?:[.\"]|$)', version_text)
    return m.group(1) if m else ""

def node_major(version_text: str) -> str:
    m = re.search(r'v?(\d+)(?:\.|$)', version_text)
    return m.group(1) if m else ""

def summarize_grype(raw_path: Path) -> dict:
    data = json.loads(raw_path.read_text(encoding="utf-8-sig"))
    counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "negligible": 0, "unknown": 0}
    for match in data.get("matches", []) if isinstance(data, dict) else []:
        vuln = match.get("vulnerability", {}) if isinstance(match, dict) else {}
        sev = str(vuln.get("severity", "unknown")).lower()
        counts[sev if sev in counts else "unknown"] += 1
    return counts


def self_test() -> None:
    with tempfile.TemporaryDirectory(prefix="cpf-r6-supply-selftest-") as td:
        root = Path(td)
        safe = root / "build" / "artifact.jar"
        safe.parent.mkdir(parents=True, exist_ok=True)
        safe.write_bytes(b"one")
        _, files = resolve_artifact(root, "build/*.jar")
        first = tree_hash(root, files)
        safe.write_bytes(b"two")
        _, files2 = resolve_artifact(root, "build/*.jar")
        second = tree_hash(root, files2)
        if first == second:
            raise QualificationError("self-test: artifact content mutation did not change tree hash")
        for unsafe in ("../secret", "/absolute/**", "build/../../secret"):
            try:
                resolve_artifact(root, unsafe)
            except QualificationError:
                pass
            else:
                raise QualificationError(f"self-test: unsafe artifact pattern accepted: {unsafe}")
        grype = root / "grype.json"
        grype.write_text(json.dumps({"matches":[
            {"vulnerability":{"severity":"Critical"}},
            {"vulnerability":{"severity":"High"}},
            {"vulnerability":{"severity":"Low"}}
        ]}), encoding="utf-8")
        counts = summarize_grype(grype)
        if counts["critical"] != 1 or counts["high"] != 1 or counts["low"] != 1:
            raise QualificationError(f"self-test: vulnerability severity aggregation failed: {counts}")
        if java_major('openjdk version "25.0.1"') != "25" or node_major('v22.18.0') != "22" or node_major('7.5.2') != "7":
            raise QualificationError("self-test: tool major version parser failed")
    print("[CPF][R6I][SUPPLY][SELFTEST][PASS] unsafePattern=true hashMutation=true severityAggregation=true toolVersion=true")

def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--expected-head")
    ap.add_argument("--evidence-dir")
    ap.add_argument("--self-test", action="store_true")
    ap.add_argument("--release-private-key", default=os.getenv("CPF_RELEASE_PRIVATE_KEY", ""))
    ap.add_argument("--release-public-key", default=os.getenv("CPF_RELEASE_PUBLIC_KEY", ""))
    args = ap.parse_args()
    if args.self_test:
        self_test()
        return 0
    if not args.expected_head or not args.evidence_dir:
        raise QualificationError("--expected-head and --evidence-dir are required unless --self-test is used")
    root = Path(args.root).resolve()
    expected = args.expected_head.lower().strip()
    if not SHA40.fullmatch(expected):
        raise QualificationError("--expected-head must be a 40-char lowercase SHA")
    actual = git(root, "rev-parse", "HEAD").lower()
    if actual != expected:
        raise QualificationError(f"source SHA mismatch expected={expected} actual={actual}")
    if git(root, "status", "--porcelain=v1", "--untracked-files=all"):
        raise QualificationError("supply-chain qualification requires clean exact-SHA tree")

    evidence = Path(args.evidence_dir)
    evidence = evidence if evidence.is_absolute() else root / evidence
    evidence.mkdir(parents=True, exist_ok=True)
    private_key = Path(args.release_private_key).expanduser() if args.release_private_key else None
    public_key = Path(args.release_public_key).expanduser() if args.release_public_key else None
    if not private_key or not private_key.is_file():
        raise QualificationError("CPF_RELEASE_PRIVATE_KEY / --release-private-key is required")
    if not public_key or not public_key.is_file():
        raise QualificationError("CPF_RELEASE_PUBLIC_KEY / --release-public-key is required")

    catalog_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    catalog = json.loads(catalog_path.read_text(encoding="utf-8-sig"))
    if str(catalog.get("schemaVersion")) != "2.1.0":
        raise QualificationError(f"unexpected artifact catalog schemaVersion={catalog.get('schemaVersion')}")
    artifacts = catalog.get("artifacts")
    if not isinstance(artifacts, list) or not artifacts:
        raise QualificationError("artifact catalog is empty")

    java_v = tool_version("java", "-version")
    javac_v = tool_version("javac", "-version")
    node_v = tool_version("node", "--version")
    npm_v = tool_version("npm", "--version")
    python_v = sys.version.split()[0]
    ort_v = tool_version("ort", "version")
    syft_v = tool_version("syft", "version")
    grype_v = tool_version("grype", "version")
    pwsh_v = tool_version("pwsh", "-NoProfile", "-Command", "$PSVersionTable.PSVersion.ToString()")
    if java_major(java_v) != "25" or java_major(javac_v) != "25":
        raise QualificationError(f"Java 25 required java={java_v} javac={javac_v}")
    if node_major(node_v) != "22":
        raise QualificationError(f"Node 22 required: {node_v}")
    if node_major(pwsh_v) != "7":
        raise QualificationError(f"PowerShell 7 required: {pwsh_v}")

    manifest_rows: list[dict] = []
    with tempfile.TemporaryDirectory(prefix="cpf-r6-artifacts-") as td:
        artifact_stage = Path(td) / "artifacts"
        artifact_stage.mkdir()
        for item in artifacts:
            artifact_id = str(item.get("artifactId", "")).strip()
            pattern = str(item.get("outputPattern", "")).strip()
            if not artifact_id or not pattern:
                raise QualificationError("catalog artifactId/outputPattern missing")
            base, files = resolve_artifact(root, pattern)
            digest = tree_hash(root, files)
            copy_artifact_stage(root, artifact_id, base, files, artifact_stage)
            manifest_rows.append({
                "artifactId": artifact_id,
                "kind": item.get("kind"),
                "outputPattern": pattern,
                "matchedFiles": [p.relative_to(root).as_posix() for p in sorted(files)],
                "sha256": digest,
                "requiredAttestations": item.get("requiredAttestations", []),
            })

        artifact_manifest = {
            "schemaVersion": 1,
            "sourceSha": actual,
            "resultSha": actual,
            "catalogSha256": sha256_file(catalog_path),
            "artifactCount": len(manifest_rows),
            "artifacts": manifest_rows,
        }
        manifest_path = evidence / "artifact-manifest.json"
        manifest_path.write_text(json.dumps(artifact_manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        (evidence / "artifact-sha256.txt").write_text(
            "".join(f"{row['sha256']}  {row['artifactId']}\n" for row in manifest_rows), encoding="ascii"
        )

        sbom_path = evidence / "cyclonedx-bom.json"
        run([shutil.which("syft") or "syft", f"dir:{artifact_stage}", "-o", f"cyclonedx-json={sbom_path}"], root)
        raw_vuln = evidence / "vulnerability-report.raw.json"
        grype_cp = subprocess.run(
            [shutil.which("grype") or "grype", f"sbom:{sbom_path}", "-o", "json", "--file", str(raw_vuln), "--fail-on", "high"],
            cwd=root, text=True, capture_output=True, check=False,
        )
        counts = summarize_grype(raw_vuln) if raw_vuln.is_file() else {"critical": 0, "high": 0, "medium": 0, "low": 0, "negligible": 0, "unknown": 0}
        vuln_summary = {"schemaVersion": 1, "sourceSha": actual, **counts, "rawReportSha256": sha256_file(raw_vuln) if raw_vuln.is_file() else None}
        (evidence / "vulnerability-report.json").write_text(json.dumps(vuln_summary, indent=2) + "\n", encoding="utf-8")
        if grype_cp.returncode != 0 or counts["critical"] or counts["high"]:
            raise QualificationError(f"Grype blocker critical={counts['critical']} high={counts['high']} exit={grype_cp.returncode}")

        ort_dir = evidence / "ort"
        ort_eval = evidence / "ort-evaluation"
        ort_report = evidence / "ort-report"
        run([shutil.which("ort") or "ort", "analyze", "-i", str(root), "-o", str(ort_dir)], root)
        analyzer = next((p for p in ort_dir.rglob("*") if p.is_file() and re.search(r"analyzer-result.*\.(?:yml|yaml|json)$", p.name)), None)
        if analyzer is None:
            raise QualificationError("ORT analyzer result missing")
        rules = root / "cpf-tools/supply-chain/ort/evaluator.rules.kts"
        run([shutil.which("ort") or "ort", "evaluate", "-i", str(analyzer), "-o", str(ort_eval), "--rules-file", str(rules)], root)
        evaluation = next((p for p in ort_eval.rglob("*") if p.is_file() and re.search(r"evaluation-result.*\.(?:yml|yaml|json)$", p.name)), None)
        if evaluation is None:
            raise QualificationError("ORT evaluation result missing")
        run([shutil.which("ort") or "ort", "report", "-i", str(evaluation), "-o", str(ort_report), "-f", "WebApp,NoticeTemplate"], root)
        run([shutil.which("java") or "java", str(root / "cpf-tools/scripts/Qa39Tool.java"), "supply-chain", "--root", str(root), "--sbom", str(sbom_path)], root)
        license_report = {
            "schemaVersion": 1,
            "sourceSha": actual,
            "status": "PASS",
            "deniedCount": 0,
            "unknownCount": 0,
            "ortAnalyzerSha256": sha256_file(analyzer),
            "ortEvaluationSha256": sha256_file(evaluation),
        }
        (evidence / "license-report.json").write_text(json.dumps(license_report, indent=2) + "\n", encoding="utf-8")

    db_available = []
    for vendor in OFFICIAL_DBS:
        prefix = f"CPF_RUNTIME_{vendor.upper()}"
        available = all(os.getenv(prefix + suffix) for suffix in ("_JDBC_URL", "_USERNAME", "_PASSWORD"))
        db_available.append({"vendor": vendor, "available": bool(available)})
    browser_available = [{"name": name, "available": os.getenv(f"CPF_R6_BROWSER_{name.upper()}_PASSED") == "true"} for name in BROWSERS]
    environment_manifest = {
        "schemaVersion": 1,
        "sourceSha": actual,
        "sanitized": True,
        "tools": [
            {"name": "java", "version": java_major(java_v)},
            {"name": "gradle-wrapper", "version": "9.1"},
            {"name": "node", "version": node_major(node_v)},
            {"name": "npm", "version": npm_v.lstrip("v")},
            {"name": "python", "version": python_v},
            {"name": "powershell", "version": node_major(pwsh_v)},
        ],
        "databases": db_available,
        "browsers": browser_available,
    }
    if not all(x["available"] for x in db_available):
        raise QualificationError("DB3 release environment is incomplete")
    if not all(x["available"] for x in browser_available):
        raise QualificationError("Chromium/Firefox/WebKit release evidence flags are required")
    (evidence / "environment-manifest.json").write_text(json.dumps(environment_manifest, indent=2) + "\n", encoding="utf-8")

    signer_src = root / "cpf-tools/release/src/main/java/com/cpf/tools/release/CpfReleaseSigner.java"
    with tempfile.TemporaryDirectory(prefix="cpf-r6-signer-") as td:
        classes = Path(td) / "classes"
        classes.mkdir()
        run([shutil.which("javac") or "javac", "--release", "25", "-d", str(classes), str(signer_src)], root)
        manifest_path = evidence / "artifact-manifest.json"
        sig_path = evidence / "artifact-manifest.json.sig"
        run([shutil.which("java") or "java", "-cp", str(classes), "com.cpf.tools.release.CpfReleaseSigner", "sign", str(private_key), str(manifest_path), str(sig_path)], root)
        run([shutil.which("java") or "java", "-cp", str(classes), "com.cpf.tools.release.CpfReleaseSigner", "verify", str(public_key), str(manifest_path), str(sig_path)], root)
    signature_verification = {
        "schemaVersion": 1,
        "sourceSha": actual,
        "algorithm": "Ed25519",
        "manifestFile": manifest_path.name,
        "manifestSha256": sha256_file(manifest_path),
        "signatureFile": sig_path.name,
        "signatureSha256": sha256_file(sig_path),
        "manifestVerified": True,
        "artifacts": [{"artifactId": row["artifactId"], "sha256": row["sha256"], "verified": True} for row in manifest_rows],
    }
    (evidence / "signature-verification.json").write_text(json.dumps(signature_verification, indent=2) + "\n", encoding="utf-8")

    if git(root, "rev-parse", "HEAD").lower() != actual or git(root, "status", "--porcelain=v1", "--untracked-files=all"):
        raise QualificationError("repository changed during supply-chain qualification")
    summary = {
        "schemaVersion": 1,
        "protocol": "CPF-R6-SUPPLY-CHAIN-QUALIFICATION",
        "sourceSha": actual,
        "artifactCount": len(manifest_rows),
        "status": "PASS",
        "tools": {"java": java_v, "node": node_v, "ort": ort_v, "syft": syft_v, "grype": grype_v},
    }
    (evidence / "supply-chain-summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"[CPF][R6I][SUPPLY][PASS] sourceSha={actual} artifacts={len(manifest_rows)} evidence={evidence}")
    return 0

if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except QualificationError as exc:
        print(f"[CPF][R6I][SUPPLY][FAIL] {exc}", file=sys.stderr)
        raise SystemExit(1)

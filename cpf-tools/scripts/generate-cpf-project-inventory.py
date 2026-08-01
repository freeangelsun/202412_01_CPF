#!/usr/bin/env python3
"""Generate a deterministic, read-only CPF product surface inventory.

The output directory MUST be outside the repository for final validation. The
script never edits source, matrices, generated clients, or evidence.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import re
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

TEXT_EXTENSIONS = {
    ".java", ".kt", ".groovy", ".gradle", ".xml", ".properties", ".yml", ".yaml",
    ".json", ".csv", ".md", ".ps1", ".py", ".sh", ".sql", ".vue", ".ts", ".tsx",
    ".js", ".mjs", ".css", ".scss", ".html", ".toml",
}
SKIP_PARTS = {".git", ".gradle", "build", "node_modules", "dist", "coverage", "test-results", "playwright-report", "__pycache__"}
JAVA_PACKAGE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)
JAVA_PUBLIC_TYPE = re.compile(r"\bpublic\s+(?:sealed\s+|non-sealed\s+|abstract\s+|final\s+)?(?:class|interface|record|enum|@interface)\s+(\w+)")
IMPORT = re.compile(r"\bimport\s+(?:static\s+)?([\w.]+)")
REQUEST_MAPPING = re.compile(r"@(RequestMapping|GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)\s*(?:\((.*?)\))?", re.DOTALL)
OPERATION_ID = re.compile(r"operationId\s*=\s*\"([^\"]+)\"")
PROPERTY_KEY = re.compile(r"^\s*([A-Za-z0-9_.-]+)\s*[:=]", re.MULTILINE)
ENV_TOKEN = re.compile(r"\$\{([A-Za-z0-9_.-]+)(?::([^}]*))?}")

@dataclass(frozen=True)
class FileEntry:
    path: str
    owner: str
    kind: str
    size: int
    sha256: str


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser()
    p.add_argument("--root", type=Path, default=Path.cwd())
    p.add_argument("--policy", type=Path, default=Path("cpf-tools/governance/cpf-product-surface-policy.json"))
    p.add_argument("--result-matrix", type=Path, default=Path("cpf-docs/quality/CPF_20260801_INTEGRATED_RESULT_MATRIX.csv"))
    p.add_argument("--output-dir", type=Path, required=True)
    return p.parse_args()


def relative_path(root: Path, path: Path) -> str:
    return path.resolve().relative_to(root.resolve()).as_posix()


def tracked_files(root: Path) -> list[Path]:
    try:
        cp = subprocess.run(["git", "-C", str(root), "ls-files", "-z"], check=True, capture_output=True)
        names = [x.decode("utf-8") for x in cp.stdout.split(b"\0") if x]
        return [root / n for n in names if (root / n).is_file()]
    except (subprocess.CalledProcessError, FileNotFoundError, UnicodeDecodeError):
        return sorted(
            p for p in root.rglob("*") if p.is_file() and not any(part in SKIP_PARTS for part in p.relative_to(root).parts)
        )


def owner_for(path: str, policy: dict) -> str:
    if path in policy.get("rootFiles", []):
        return "repository-root"
    matches = [(x["prefix"], x["owner"]) for x in policy["moduleOwners"] if path.startswith(x["prefix"])]
    return max(matches, key=lambda x: len(x[0]))[1] if matches else "UNOWNED"


def file_kind(path: str) -> str:
    low = path.lower()
    suffix = Path(low).suffix
    if "/src/test/" in low or "/e2e/" in low or low.endswith("test.java") or low.endswith(".test.ts"):
        return "test"
    if suffix == ".sql":
        return "database"
    if "/frontend/" in low or suffix in {".vue", ".tsx"}:
        return "frontend"
    if suffix in {".properties", ".yml", ".yaml", ".toml", ".env"} or low.endswith("config.json"):
        return "configuration"
    if suffix in {".java", ".kt", ".groovy"}:
        return "source"
    if suffix in {".gradle"} or Path(low).name in {"build.gradle", "settings.gradle", "gradlew", "gradlew.bat"}:
        return "build"
    if suffix in {".ps1", ".py", ".sh", ".mjs"}:
        return "script"
    if suffix in {".md", ".csv"} or low.startswith("cpf-docs/"):
        return "documentation"
    return "resource"


def read_text(path: Path) -> str:
    if path.suffix.lower() not in TEXT_EXTENSIONS and path.name not in {"gradlew", "gradlew.bat"}:
        return ""
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        try:
            return path.read_text(encoding="utf-8-sig")
        except UnicodeDecodeError:
            return ""


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def write_csv(path: Path, columns: list[str], rows: Iterable[dict]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=columns, extrasaction="ignore", lineterminator="\n")
        w.writeheader()
        w.writerows(rows)


def split_paths(value: str) -> list[str]:
    return [x.strip().replace("\\", "/") for x in (value or "").split(";") if x.strip()]


def main() -> int:
    args = parse_args()
    root = args.root.resolve()
    output = args.output_dir.resolve()
    if output == root or root in output.parents:
        raise SystemExit("output-dir must be outside repository to preserve a read-only working tree")
    policy_path = args.policy if args.policy.is_absolute() else root / args.policy
    matrix_path = args.result_matrix if args.result_matrix.is_absolute() else root / args.result_matrix
    policy = json.loads(policy_path.read_text(encoding="utf-8"))
    files = tracked_files(root)
    entries: list[FileEntry] = []
    public_contracts: list[dict] = []
    configs: list[dict] = []
    db_rows: list[dict] = []
    frontend_rows: list[dict] = []
    imports_by_file: dict[str, set[str]] = {}

    for p in files:
        rel = relative_path(root, p)
        owner = owner_for(rel, policy)
        kind = file_kind(rel)
        text = read_text(p)
        entries.append(FileEntry(rel, owner, kind, p.stat().st_size, sha256(p)))
        if p.suffix.lower() == ".java" and text:
            package = (JAVA_PACKAGE.search(text).group(1) if JAVA_PACKAGE.search(text) else "")
            imports_by_file[rel] = set(IMPORT.findall(text))
            public_types = JAVA_PUBLIC_TYPE.findall(text)
            is_controller = any(a in text for a in policy["controllerAnnotations"])
            if public_types or is_controller or any(m in f".{package}." for m in policy["publicPackageMarkers"]):
                mappings = []
                for match in REQUEST_MAPPING.finditer(text):
                    mappings.append(f"{match.group(1)}:{' '.join((match.group(2) or '').split())[:300]}")
                public_contracts.append({
                    "path": rel, "owner": owner, "package": package,
                    "symbols": ";".join(public_types), "contract_type": "controller" if is_controller else "java-public",
                    "operation_ids": ";".join(OPERATION_ID.findall(text)), "http_mappings": ";".join(mappings),
                    "direct_internal_imports": ";".join(sorted(x for x in imports_by_file[rel] if ".internal." in x)),
                })
        if kind == "configuration" and text:
            keys = sorted(set(PROPERTY_KEY.findall(text)))
            envs = sorted({m.group(1) for m in ENV_TOKEN.finditer(text)})
            configs.append({"path": rel, "owner": owner, "property_keys": ";".join(keys), "environment_tokens": ";".join(envs), "secure_default_review": "REQUIRED" if envs or any(x in text.lower() for x in ["password", "secret", "token", "credential"]) else "N/A"})
        if kind == "database":
            parts = rel.lower().split("/")
            vendor = next((v for v in policy["officialDatabaseVendors"] if v in parts), "canonical-or-unspecified")
            lifecycle = next((x for x in ["install", "migration", "upgrade", "rollback", "seed", "runtime", "verify", "backup", "purge"] if x in parts), "other")
            db_rows.append({"path": rel, "owner": owner, "vendor": vendor, "lifecycle": lifecycle, "sha256": entries[-1].sha256})
        if kind == "frontend":
            privileged_calls = sorted(set(re.findall(r"[\"'](/(?:adm/api|api/bza)/[^\"']+)[\"']", text)))
            generated_import = "generated" in text and "import" in text
            route_markers = sorted(set(re.findall(r"data-cpf-page=[\"']([^\"']+)", text)))
            frontend_rows.append({"path": rel, "owner": owner, "privileged_calls": ";".join(privileged_calls), "generated_client_import": str(generated_import).lower(), "page_markers": ";".join(route_markers), "sha256": entries[-1].sha256})

    trace_rows: list[dict] = []
    known_paths = {e.path for e in entries}
    if matrix_path.is_file():
        with matrix_path.open(encoding="utf-8-sig", newline="") as f:
            for row in csv.DictReader(f):
                for role, column in [("source", "source_paths"), ("consumer", "consumer_paths"), ("test", "test_paths"), ("evidence", "evidence_paths")]:
                    for path in split_paths(row.get(column, "")):
                        trace_rows.append({
                            "requirement_id": row.get("requirement_id", ""), "role": role, "path": path,
                            "path_exists": str(path in known_paths or (root / path).is_file()).lower(),
                            "development_status": row.get("development_status", ""),
                            "verification_status": row.get("verification_status", ""),
                        })

    output.mkdir(parents=True, exist_ok=True)
    write_csv(output / "cpf-module-file-inventory.csv", ["path", "owner", "kind", "size", "sha256"], [e.__dict__ for e in entries])
    write_csv(output / "cpf-public-contract-inventory.csv", ["path", "owner", "package", "symbols", "contract_type", "operation_ids", "http_mappings", "direct_internal_imports"], public_contracts)
    write_csv(output / "cpf-configuration-inventory.csv", ["path", "owner", "property_keys", "environment_tokens", "secure_default_review"], configs)
    write_csv(output / "cpf-database-inventory.csv", ["path", "owner", "vendor", "lifecycle", "sha256"], db_rows)
    write_csv(output / "cpf-frontend-inventory.csv", ["path", "owner", "privileged_calls", "generated_client_import", "page_markers", "sha256"], frontend_rows)
    write_csv(output / "cpf-requirement-reverse-trace.csv", ["requirement_id", "role", "path", "path_exists", "development_status", "verification_status"], trace_rows)
    summary = {
        "schemaVersion": 1,
        "root": str(root),
        "fileCount": len(entries),
        "unownedFileCount": sum(e.owner == "UNOWNED" for e in entries),
        "publicContractCount": len(public_contracts),
        "configurationFileCount": len(configs),
        "databaseFileCount": len(db_rows),
        "frontendFileCount": len(frontend_rows),
        "reverseTraceRows": len(trace_rows),
        "missingTracePathCount": sum(x["path_exists"] != "true" for x in trace_rows),
    }
    (output / "cpf-project-inventory-summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False))
    return 0

if __name__ == "__main__":
    raise SystemExit(main())

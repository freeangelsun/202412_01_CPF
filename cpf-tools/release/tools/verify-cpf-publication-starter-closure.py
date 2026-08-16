#!/usr/bin/env python3
"""Fail-closed final-artifact/canonical-starter/publication-consumer closure gate."""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter
from pathlib import Path, PurePosixPath

STARTER_KINDS = {"starter-base", "starter-common", "starter-profile", "starter-provider", "internal-starter"}
PUBLIC_ARTIFACT_RE = re.compile(r"^cpf-starter(?:-[A-Za-z0-9._-]+)?$")
INTERNAL_ARTIFACT_RE = re.compile(r"^cpf-[A-Za-z0-9._-]+$")


class GateError(RuntimeError):
    pass


def load(path: Path) -> dict:
    if not path.is_file():
        raise GateError(f"required JSON missing: {path}")
    try:
        value = json.loads(path.read_text(encoding="utf-8-sig"))
    except Exception as exc:
        raise GateError(f"invalid JSON {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise GateError(f"JSON root must be object: {path}")
    return value


def normalized_owner(value: object) -> str:
    if not isinstance(value, str) or not value.strip():
        raise GateError("Starter ownerPath missing")
    raw = value.strip().replace("\\", "/")
    path = PurePosixPath(raw)
    if path.is_absolute() or ".." in path.parts or path.as_posix() != raw:
        raise GateError(f"Starter ownerPath invalid: {raw}")
    if len(path.parts) < 2 or path.parts[0] != "cpf-starters":
        raise GateError(f"Starter ownerPath must be grouped under cpf-starters: {raw}")
    return raw


def duplicates(values: list[str]) -> list[str]:
    return sorted(value for value, count in Counter(values).items() if count > 1)


def verify(root: Path, require_physical: bool = False) -> dict:
    final_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    final = load(final_path)
    if final.get("sourceShaPolicy") != "runtime-exact-sha-evidence":
        raise GateError("final artifact catalog sourceShaPolicy must require runtime exact-SHA evidence")
    if final.get("baselinePolicy") != "GIT_HEAD_RUNTIME" or final.get("baselineSha") != "RUNTIME_GIT_HEAD":
        raise GateError("final artifact catalog must use runtime Git HEAD baseline policy")
    canonical_relative = final.get("canonicalStarterCatalog")
    if not isinstance(canonical_relative, str) or not canonical_relative.strip():
        raise GateError("final artifact catalog canonicalStarterCatalog missing")
    canonical = load(root / canonical_relative)

    modules = canonical.get("modules")
    artifacts = final.get("artifacts")
    if not isinstance(modules, list) or not modules:
        raise GateError("canonical Starter modules empty")
    if not isinstance(artifacts, list) or not artifacts:
        raise GateError("final artifact catalog artifacts empty")

    canonical_rows: dict[str, str] = {}
    canonical_public: set[str] = set()
    for row in modules:
        if not isinstance(row, dict):
            raise GateError("canonical Starter module row invalid")
        if row.get("publicationRequired") is not True:
            continue
        kind = row.get("kind")
        if kind not in STARTER_KINDS:
            raise GateError(f"publicationRequired Starter kind invalid: {kind}")
        artifact = row.get("artifactId")
        visibility = row.get("visibility")
        artifact_re = PUBLIC_ARTIFACT_RE if visibility == "public" else INTERNAL_ARTIFACT_RE
        if not isinstance(artifact, str) or not artifact_re.fullmatch(artifact):
            raise GateError(f"canonical Starter artifactId invalid: {artifact}")
        owner = normalized_owner(row.get("ownerPath"))
        if artifact in canonical_rows:
            raise GateError(f"duplicate canonical Starter artifactId: {artifact}")
        canonical_rows[artifact] = owner
        if visibility == "public":
            canonical_public.add(artifact)

    final_rows: dict[str, str] = {}
    starter_artifacts = [row for row in artifacts if isinstance(row, dict) and row.get("kind") in STARTER_KINDS]
    legacy_rows = [row for row in artifacts if isinstance(row, dict) and row.get("kind") == "starter"]
    if legacy_rows:
        raise GateError("legacy final artifact kind=starter is forbidden")
    for row in starter_artifacts:
        artifact = row.get("artifactId")
        artifact_re = PUBLIC_ARTIFACT_RE if artifact in canonical_public else INTERNAL_ARTIFACT_RE
        if not isinstance(artifact, str) or not artifact_re.fullmatch(artifact):
            raise GateError(f"final Starter artifactId invalid: {artifact}")
        owner = normalized_owner(row.get("ownerPath"))
        if artifact in final_rows:
            raise GateError(f"duplicate final Starter artifactId: {artifact}")
        final_rows[artifact] = owner

    if final_rows != canonical_rows:
        missing = sorted(set(canonical_rows) - set(final_rows))
        extra = sorted(set(final_rows) - set(canonical_rows))
        owner_drift = sorted(
            artifact for artifact in set(final_rows).intersection(canonical_rows)
            if final_rows[artifact] != canonical_rows[artifact]
        )
        raise GateError(
            f"final/canonical Starter closure mismatch missing={missing} extra={extra} ownerDrift={owner_drift}"
        )

    removed = final.get("removedArtifactIds") or []
    overlap = sorted(set(final_rows).intersection(str(value) for value in removed))
    if overlap:
        raise GateError(f"active Starter also removed: {overlap}")

    publication_script = root / "cpf-tools/verification/tools/verify-local-artifact-propagation.ps1"
    if not publication_script.is_file():
        raise GateError("publication verifier script missing")
    material = publication_script.read_text(encoding="utf-8-sig")
    starter_kinds_match = re.search(r"\$starterKinds\s*=\s*@\(([^)]*)\)", material, re.MULTILINE)
    if starter_kinds_match is None:
        raise GateError("publication verifier Starter kind declaration missing")
    declared_starter_kinds = set(re.findall(r"['\"]([^'\"]+)['\"]", starter_kinds_match.group(1)))
    missing_starter_kinds = sorted(STARTER_KINDS - declared_starter_kinds)
    if missing_starter_kinds:
        raise GateError(f"publication verifier Starter kinds incomplete: {missing_starter_kinds}")

    required_tokens = [
        "$artifactCatalog.canonicalStarterCatalog",
        "$canonicalByArtifact",
        "Get-ChildItem -LiteralPath (Join-Path $Root 'cpf-starters') -Recurse -File",
        "Starter catalog/physical project mismatch",
        "$batchArtifactRows = @($artifactCatalog.artifacts",
        "artifact='cpf-internal-platform-bom'",
        "$publicStarterArtifacts",
        "$internalStarterArtifacts",
        "Internal Starter leaked into public BOM",
        "CPF internal BOM must import the exact public BOM once",
    ]
    missing_tokens = [token for token in required_tokens if token not in material]
    if missing_tokens:
        raise GateError(f"publication verifier missing grouped Starter tokens: {missing_tokens}")
    forbidden_tokens = [
        "Where-Object { [string]$_.kind -eq 'starter' }",
        "^cpf-starters/([^/]+)$",
        '"cpf-starter-$($Matches[1])"',
        "artifact='cpf-center-cut-runner'",
        "cpf-center-cut-runner",
        ") + @($starterCatalogRows | ForEach-Object { [string]$_.artifactId }",
    ]
    present_forbidden = [token for token in forbidden_tokens if token in material]
    if present_forbidden:
        raise GateError(f"publication verifier retains flat/legacy Starter assumptions: {present_forbidden}")

    physical_checked = False
    if require_physical:
        physical_checked = True
        missing_paths: list[str] = []
        for owner in canonical_rows.values():
            directory = root / owner
            if not directory.is_dir() or not (
                (directory / "build.gradle").is_file() or (directory / "build.gradle.kts").is_file()
            ):
                missing_paths.append(owner)
        if missing_paths:
            raise GateError(f"physical Starter Gradle modules missing: {missing_paths}")
        actual = sorted(
            path.parent.relative_to(root).as_posix()
            for path in (root / "cpf-starters").rglob("build.gradle*")
            if path.is_file()
        )
        all_canonical_owners = sorted(normalized_owner(row.get("ownerPath")) for row in modules if isinstance(row, dict))
        if actual != all_canonical_owners:
            raise GateError(
                f"physical Starter closure mismatch expected={all_canonical_owners} actual={actual}"
            )

    return {
        "status": "PASS",
        "starterCount": len(canonical_rows),
        "publicProfiles": sum(1 for row in starter_artifacts if row.get("kind") == "starter-profile"),
        "publicStarters": sum(1 for row in starter_artifacts if row.get("artifactId") in canonical_public),
        "internalStarters": sum(1 for row in starter_artifacts if row.get("kind") == "internal-starter"),
        "physicalChecked": physical_checked,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--require-physical", action="store_true")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        result = verify(Path(args.root).resolve(), args.require_physical)
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    if args.json_output:
        output = Path(args.json_output)
        if not output.is_absolute():
            output = Path(args.root).resolve() / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())

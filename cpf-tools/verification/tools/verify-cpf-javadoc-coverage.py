#!/usr/bin/env python3
"""Verify Javadoc coverage for every public top-level type in published Java artifacts.

The canonical publication boundary is cpf-tools/release/cpf-final-artifact-catalog.json.
This verifier intentionally does not depend on Git changed-files, so Local Working Tree ZIP
validation and Fresh Replay use the same contract.
"""
from __future__ import annotations
import argparse
import json
import re
import sys
from pathlib import Path

TYPE = re.compile(r"(?m)^public\s+(?:final\s+|abstract\s+|sealed\s+|non-sealed\s+)?(?:class|interface|record|enum|@interface)\s+([A-Za-z_$][\w$]*)")


def has_javadoc(text: str, start: int) -> bool:
    """Return true when the declaration is preceded by a Javadoc block.

    Java annotations are declaration modifiers and may span multiple lines (for example
    ``@EnableConfigurationProperties({ ... })``).  The previous line-based implementation
    stopped at a continuation line such as ``})`` and produced a false negative even though
    a valid class Javadoc was immediately before the annotation block.
    """
    prefix = text[:start]
    end = prefix.rfind("*/")
    if end < 0:
        return False
    begin = prefix.rfind("/**", 0, end + 2)
    if begin < 0:
        return False
    between = prefix[end + 2 :].strip()
    if not between:
        return True
    # Only annotations may occur between a type Javadoc and the type declaration.  Treat
    # an entire multi-line annotation block as one modifier instead of inspecting each line.
    return between.startswith("@") and ";" not in between


def publication_policy(root: Path) -> tuple[dict, set[str], tuple[re.Pattern[str], ...]]:
    policy_path = root / "cpf-tools/release/public/cpf-public-java-publication-policy.json"
    if not policy_path.is_file():
        raise ValueError(f"canonical Public Java publication policy missing: {policy_path.relative_to(root)}")
    policy = json.loads(policy_path.read_text(encoding="utf-8-sig"))
    source_policy = policy.get("sourceArtifactPolicy") or {}
    excluded = {str(value).lower() for value in source_policy.get("excludedPathSegments") or []}
    forbidden = tuple(re.compile(str(value), re.I) for value in source_policy.get("forbiddenContentPatterns") or [])
    if not excluded or not forbidden or set(source_policy.get("projectionTargets") or []) != {"sourcesJar", "javadoc"}:
        raise ValueError("CPF Public Java source projection policy is incomplete")
    return policy, excluded, forbidden


def publication_roots(root: Path, policy: dict) -> list[tuple[str, Path]]:
    catalog_path = root / str(policy.get("artifactCatalog") or "cpf-tools/release/cpf-final-artifact-catalog.json")
    if not catalog_path.is_file():
        raise ValueError(f"canonical artifact catalog missing: {catalog_path.relative_to(root)}")
    catalog = json.loads(catalog_path.read_text(encoding="utf-8"))
    compile_class = str(policy.get("compileTimePublicationClass") or "PUBLIC_COMPILE_TIME_JAVA")
    rows: list[tuple[str, Path]] = []
    for artifact in catalog.get("artifacts", []):
        if artifact.get("publicationClass") != compile_class:
            continue
        owner = artifact.get("ownerPath")
        artifact_id = artifact.get("artifactId")
        if not owner or not artifact_id:
            raise ValueError(f"published artifact is missing ownerPath/artifactId: {artifact}")
        path = root / owner
        if not path.is_dir():
            raise ValueError(f"published artifact owner missing: {artifact_id}:{owner}")
        rows.append((artifact_id, path))
    if not rows:
        raise ValueError("canonical artifact catalog contains no public compile-time Java artifacts")
    return rows


def verify(root: Path, manifest: Path | None = None) -> tuple[int, int]:
    """Verify canonical published Java types; optional manifest is a fixture-only compatibility input."""
    errors: list[str] = []
    seen: set[Path] = set()
    public_types = 0
    policy, excluded_segments, forbidden_patterns = publication_policy(root)
    if manifest is not None:
        paths=[]
        for raw in manifest.read_text(encoding="utf-8").splitlines():
            rel=raw.strip().replace("\\","/")
            if rel: paths.append(("fixture", root / rel))
        artifacts=[("fixture", root)]
        iterable=paths
    else:
        artifacts = publication_roots(root, policy)
        iterable=[]
        for artifact_id, owner in artifacts:
            iterable.extend((artifact_id, path) for path in sorted(owner.rglob("*.java")))
    for artifact_id, path in iterable:
        if path in seen:
            continue
        seen.add(path)
        if not path.is_file():
            raise ValueError(f"public Java source missing: {path.relative_to(root).as_posix()}")
        rel = path.relative_to(root).as_posix()
        if manifest is None and ("/src/main/java/" not in rel or "/src/test/" in rel):
            continue
        text = path.read_text(encoding="utf-8", errors="ignore")
        source_parts = {part.lower() for part in path.parts}
        if manifest is None and (excluded_segments.intersection(source_parts) or
                                 any(pattern.search(text) for pattern in forbidden_patterns)):
            continue
        match = TYPE.search(text)
        if not match:
            continue
        public_types += 1
        if not has_javadoc(text, match.start()):
            errors.append(f"public type Javadoc missing: {artifact_id}:{rel}:{match.group(1)}")
    if errors:
        raise ValueError("\n".join(errors))
    return len(artifacts), public_types


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    # Kept only so old orchestrators fail closed with the same CLI rather than argparse-crashing.
    parser.add_argument("--manifest", help="Deprecated: ignored; canonical publication catalog is always verified")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        artifacts, types = verify(root)
    except (ValueError, json.JSONDecodeError) as exc:
        print(f"[FAIL] CPF Javadoc coverage\n{exc}", file=sys.stderr)
        return 1
    print(f"[PASS] CPF Javadoc coverage publishedArtifacts={artifacts} publicTopLevelTypes={types} missing=0")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

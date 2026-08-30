#!/usr/bin/env python3
"""Fail closed when the classified CPF Public Java surface lacks usable type documentation.

JDK Javadoc/doclint validates syntax and references during Gradle publication.  This
static gate complements it by requiring every externally visible top-level type on
the classified compile-time surface to explain its CPF-specific contract.  Internal
packages are deliberately outside the public documentation surface.
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path


class GateError(RuntimeError):
    pass


TYPE_RE = re.compile(
    r"(?m)^(?P<indent>[ \t]*)public\s+(?:final\s+|sealed\s+|non-sealed\s+|abstract\s+)?"
    r"(?:class|interface|record|enum|@interface)\s+(?P<name>[A-Za-z_$][A-Za-z0-9_$]*)\b"
)
GENERIC_ONLY_RE = re.compile(r"^(?:CPF\s+)?(?:public\s+)?(?:api|class|interface|record|enum)\.?$", re.I)


def _policy(root: Path) -> tuple[dict, set[str], tuple[re.Pattern[str], ...]]:
    policy = json.loads((root / "cpf-tools/release/public/cpf-public-java-publication-policy.json").read_text(encoding="utf-8-sig"))
    source_policy = policy.get("sourceArtifactPolicy") or {}
    excluded = {str(value).lower() for value in source_policy.get("excludedPathSegments") or []}
    forbidden = tuple(re.compile(str(value), re.I) for value in source_policy.get("forbiddenContentPatterns") or [])
    if not excluded or not forbidden or set(source_policy.get("projectionTargets") or []) != {"sourcesJar", "javadoc"}:
        raise GateError("CPF Public Java source projection policy is incomplete")
    return policy, excluded, forbidden


def _owners(root: Path, policy: dict) -> list[tuple[str, Path]]:
    catalog = json.loads((root / str(policy.get("artifactCatalog") or "cpf-tools/release/cpf-final-artifact-catalog.json")).read_text(encoding="utf-8-sig"))
    compile_class = str(policy.get("compileTimePublicationClass") or "PUBLIC_COMPILE_TIME_JAVA")
    rows: list[tuple[str, Path]] = []
    for row in catalog.get("artifacts", []):
        if str(row.get("publicationClass") or "") != compile_class:
            continue
        owner = str(row.get("ownerPath") or "").strip()
        if not owner:
            raise GateError(f"public Java owner path missing artifact={row.get('artifactId')}")
        if row.get("publishSources") is not True or row.get("publishJavadoc") is not True:
            raise GateError(f"public Java documentation flags invalid artifact={row.get('artifactId')}")
        rows.append((str(row["artifactId"]), root / owner))
    if not rows:
        raise GateError("canonical artifact catalog has no public Java documentation owners")
    return rows

def _doc_for(text: str, declaration_start: int) -> str | None:
    prefix = text[:declaration_start]
    end = prefix.rfind("*/")
    if end < 0:
        return None
    begin = prefix.rfind("/**", 0, end + 2)
    if begin < 0:
        return None
    between = prefix[end + 2 :].strip()
    # Multi-line Spring/Java annotations are declaration modifiers.  They may sit between
    # the type Javadoc and declaration, but executable statements/declarations may not.
    if between and (not between.startswith("@") or ";" in between):
        return None
    return prefix[begin + 3 : end]


def _clean_doc(body: str) -> str:
    lines = []
    for raw in body.splitlines():
        value = raw.strip().lstrip("*").strip()
        if value.startswith("@"):
            continue
        if value:
            lines.append(value)
    return " ".join(lines)


def verify(root: Path) -> dict:
    policy, excluded_segments, forbidden_patterns = _policy(root)
    findings: list[str] = []
    artifact_count = 0
    public_type_count = 0
    documented_count = 0
    for artifact, owner in _owners(root, policy):
        artifact_count += 1
        source_root = owner / "src/main/java"
        if not source_root.is_dir():
            # Profile Starters may intentionally contain dependency metadata only.
            continue
        for source in sorted(source_root.rglob("*.java")):
            rel = source.relative_to(source_root).as_posix()
            text = source.read_text(encoding="utf-8", errors="replace")
            if excluded_segments.intersection(part.lower() for part in Path(rel).parts):
                continue
            if any(pattern.search(text) for pattern in forbidden_patterns):
                continue
            for match in TYPE_RE.finditer(text):
                public_type_count += 1
                body = _doc_for(text, match.start())
                if body is None:
                    findings.append(f"{artifact}:{rel}:{match.group('name')} missing public type Javadoc")
                    continue
                cleaned = _clean_doc(body)
                if len(cleaned) < 10 or GENERIC_ONLY_RE.fullmatch(cleaned):
                    findings.append(f"{artifact}:{rel}:{match.group('name')} Javadoc is too weak")
                    continue
                # Public docs must describe the CPF contract without leaking private implementation types.
                if re.search(r"com\.cpf(?:\.[A-Za-z0-9_]+)*\.internal(?:\.|\b)", body, re.I):
                    findings.append(f"{artifact}:{rel}:{match.group('name')} Javadoc exposes internal CPF type")
                    continue
                documented_count += 1
    if findings:
        raise GateError("\n".join(findings))
    return {
        "status": "PASS",
        "artifactCount": artifact_count,
        "publicTypeCount": public_type_count,
        "documentedPublicTypeCount": documented_count,
        "findings": 0,
    }


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    args = ap.parse_args()
    try:
        result = verify(Path(args.root).resolve())
        code = 0
    except Exception as exc:
        result = {"status": "FAIL", "message": str(exc)}
        code = 1
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())

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
DOC_RE = re.compile(r"/\*\*(?P<body>.*?)\*/\s*(?:@[\w.]+(?:\([^\n]*\))?\s*)*$", re.S)
GENERIC_ONLY_RE = re.compile(r"^(?:CPF\s+)?(?:public\s+)?(?:api|class|interface|record|enum)\.?$", re.I)


def _owners(root: Path) -> list[tuple[str, Path]]:
    policy = json.loads((root / "cpf-tools/release/public/cpf-public-java-publication-policy.json").read_text(encoding="utf-8-sig"))
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
    match = DOC_RE.search(prefix)
    return match.group("body") if match else None


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
    findings: list[str] = []
    artifact_count = 0
    public_type_count = 0
    documented_count = 0
    for artifact, owner in _owners(root):
        artifact_count += 1
        source_root = owner / "src/main/java"
        if not source_root.is_dir():
            # Profile Starters may intentionally contain dependency metadata only.
            continue
        for source in sorted(source_root.rglob("*.java")):
            rel = source.relative_to(source_root).as_posix()
            if "/internal/" in f"/{rel}":
                continue
            text = source.read_text(encoding="utf-8", errors="replace")
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

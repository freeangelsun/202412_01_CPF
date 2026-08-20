#!/usr/bin/env python3
"""CPF Spring/Java source hygiene gate.

The gate intentionally targets warnings that point to ambiguous Spring ownership/wiring,
not cosmetic formatting.  It prevents two recurring repository regressions:

* redundant ``@Autowired`` on the only constructor of a class;
* ``WebMvcConfigurer`` implementations whose configuration role is implicit.

Optional/method/field injection is not rejected here because those forms can carry
required/optional semantics that must be reviewed separately.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
FAIL: list[str] = []

SKIP_PARTS = {"build", ".gradle", "node_modules", "__pycache__", ".git"}


def product_java_files() -> list[Path]:
    files: list[Path] = []
    for path in ROOT.rglob("*.java"):
        rel = path.relative_to(ROOT)
        if any(part in SKIP_PARTS for part in rel.parts):
            continue
        # Test fixtures intentionally exercise non-production wiring patterns.
        if "src" in rel.parts and "test" in rel.parts:
            continue
        files.append(path)
    return files


def top_level_class_name(text: str) -> str | None:
    match = re.search(r"(?m)^\s*(?:public\s+)?(?:abstract\s+|final\s+|sealed\s+|non-sealed\s+)?class\s+(\w+)", text)
    return match.group(1) if match else None


def constructor_spans(text: str, class_name: str) -> list[tuple[int, int]]:
    pattern = re.compile(
        rf"(?m)^\s*(?:public|protected|private)?\s*{re.escape(class_name)}\s*\(",
    )
    return [(m.start(), m.end()) for m in pattern.finditer(text)]


def redundant_autowired(text: str, class_name: str) -> bool:
    constructors = constructor_spans(text, class_name)
    if len(constructors) != 1:
        return False
    constructor_start = constructors[0][0]
    prefix = text[:constructor_start]
    # Only plain @Autowired immediately attached to the constructor is redundant.
    # @Autowired(required=false) is a different semantic contract and is not touched.
    return re.search(r"@Autowired\s*(?:\r?\n\s*)$", prefix) is not None


def configurer_has_explicit_configuration_role(text: str) -> bool:
    if not re.search(r"\bimplements\s+WebMvcConfigurer\b", text):
        return True
    class_match = re.search(r"(?m)^\s*(?:public\s+)?(?:final\s+)?class\s+\w+\s+implements\s+WebMvcConfigurer\b", text)
    if not class_match:
        return True
    prefix = text[max(0, class_match.start() - 1200):class_match.start()]
    return bool(re.search(r"@(Configuration|AutoConfiguration)\b", prefix))


for path in product_java_files():
    text = path.read_text(encoding="utf-8-sig", errors="ignore")
    rel = path.relative_to(ROOT).as_posix()
    class_name = top_level_class_name(text)
    if class_name and redundant_autowired(text, class_name):
        FAIL.append(f"redundant single-constructor @Autowired: {rel}")
    if not configurer_has_explicit_configuration_role(text):
        FAIL.append(f"WebMvcConfigurer missing explicit @Configuration/@AutoConfiguration: {rel}")

if FAIL:
    print("CPF_SPRING_JAVA_HYGIENE=FAIL")
    for finding in FAIL:
        print(f"- {finding}")
    raise SystemExit(1)

print("CPF_SPRING_JAVA_HYGIENE=PASS")
print(f"scannedMainJava={len(product_java_files())}")
raise SystemExit(0)

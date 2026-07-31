#!/usr/bin/env python3
from pathlib import Path
import csv
import re
import sys

root = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path.cwd()
required = [
    root / "cpf-docs/work/review/CPF_SELF_DEVELOPMENT_SOURCE_REVIEW.md",
    root / "cpf-docs/work/current/CPF_SELF_DEVELOPMENT_REQUIREMENTS.md",
    root / "cpf-docs/quality/CPF_SELF_DEVELOPMENT_REQUIREMENT_MATRIX.csv",
    root / "cpf-docs/work/current/CPF_CURRENT_SELF_DEVELOPMENT_REQUEST.md",
    root / "cpf-docs/work/current/CPF_SELF_DEVELOPMENT_EXECUTION_PROMPT.md",
    root / "cpf-docs/work/current/CPF_CODEX_SELF_DEVELOPMENT_VERIFICATION_REQUEST.md",
]
missing = [str(path.relative_to(root)) for path in required if not path.is_file()]
if missing:
    raise SystemExit("Missing self-development files: " + ", ".join(missing))

matrix = required[2]
with matrix.open(encoding="utf-8-sig", newline="") as handle:
    rows = list(csv.DictReader(handle))

if len(rows) != 30:
    raise SystemExit(f"Requirement count mismatch: {len(rows)}")

ids = [row["requirement_id"] for row in rows]
expected = [f"CPF-SELF-DEV-{index:03d}" for index in range(1, 31)]
if ids != expected:
    raise SystemExit("Requirement ID sequence mismatch")

if any(row.get("source_type") != "SELF" for row in rows):
    raise SystemExit("Non-SELF source_type detected")

for path in required:
    text = path.read_text(encoding="utf-8-sig")
    if re.search(r"\bQA\d+\b|QA\d+-|_QA\d+_", text):
        raise SystemExit(f"External verification round identifier detected: {path.relative_to(root)}")

print("[CPF][SELF][PASS] requirements=30 namespace=CPF-SELF-DEV external-round-identifiers=0")

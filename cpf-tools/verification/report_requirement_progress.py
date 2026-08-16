#!/usr/bin/env python3
"""CPF current requirement ledger progress calculator.

The ledger is the only denominator. No session estimate or stale percentage is accepted.
"""
from __future__ import annotations
import argparse, csv, json
from collections import Counter
from pathlib import Path

STAGES = ("analysis", "source_consumer", "test_integration", "gate_evidence")
ALLOWED = {"0", "1"}

def load(path: Path):
    with path.open(encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))
    if not rows:
        raise SystemExit("REQUIREMENT_PROGRESS_FAIL: empty ledger")
    missing = [c for c in ("requirement_id", "category", *STAGES) if c not in rows[0]]
    if missing:
        raise SystemExit("REQUIREMENT_PROGRESS_FAIL: missing columns=" + ",".join(missing))
    seen = set()
    for i, row in enumerate(rows, 2):
        rid = (row.get("requirement_id") or "").strip()
        if not rid or rid in seen:
            raise SystemExit(f"REQUIREMENT_PROGRESS_FAIL: duplicate/blank requirement_id line={i} id={rid!r}")
        seen.add(rid)
        for stage in STAGES:
            value = (row.get(stage) or "").strip()
            if value not in ALLOWED:
                raise SystemExit(f"REQUIREMENT_PROGRESS_FAIL: {rid} {stage}={value!r}; expected 0|1")
    return rows

def stats(rows):
    total = len(rows) * len(STAGES)
    closed = sum(int(r[s]) for r in rows for s in STAGES)
    return {"rows": len(rows), "closed": closed, "total": total,
            "percent": round(closed * 100.0 / total, 2) if total else 0.0}

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--ledger", required=True, type=Path)
    p.add_argument("--json-out", type=Path)
    p.add_argument("--markdown-out", type=Path)
    p.add_argument("--expected-canonical", type=int, default=63)
    args = p.parse_args()
    rows = load(args.ledger)
    categories = Counter(r["category"] for r in rows)
    canonical = [r for r in rows if r["category"] == "CANONICAL"]
    if len(canonical) != args.expected_canonical:
        raise SystemExit(f"REQUIREMENT_PROGRESS_FAIL: canonical rows={len(canonical)} expected={args.expected_canonical}")
    result = {"canonical": stats(canonical), "all": stats(rows), "categories": {}}
    for category in sorted(categories):
        result["categories"][category] = stats([r for r in rows if r["category"] == category])
    if args.json_out:
        args.json_out.parent.mkdir(parents=True, exist_ok=True)
        args.json_out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if args.markdown_out:
        args.markdown_out.parent.mkdir(parents=True, exist_ok=True)
        lines = ["# CPF Requirement Progress", "",
                 f"- Canonical: **{result['canonical']['percent']:.2f}%** ({result['canonical']['closed']} / {result['canonical']['total']})",
                 f"- Tracked total: **{result['all']['percent']:.2f}%** ({result['all']['closed']} / {result['all']['total']})", ""]
        for category, value in result["categories"].items():
            lines.append(f"- {category}: **{value['percent']:.2f}%** ({value['closed']} / {value['total']})")
        args.markdown_out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"REQUIREMENT_PROGRESS_PASS canonical={result['canonical']['closed']}/{result['canonical']['total']}={result['canonical']['percent']:.2f}% all={result['all']['closed']}/{result['all']['total']}={result['all']['percent']:.2f}%")

if __name__ == "__main__":
    main()

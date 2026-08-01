#!/usr/bin/env python3
from __future__ import annotations
import argparse,csv,json,sys
from pathlib import Path

SOURCES=(
 ("FINAL_LEDGER","cpf-docs/quality/qa-20260729/CPF_FINAL_QA_MASTER_LEDGER_20260729.csv","id"),
 ("ENTERPRISE_INVENTORY","cpf-docs/quality/qa-20260729/CPF_ENTERPRISE_REQA_INVENTORY_20260729_05.csv","id"),
 ("ENTERPRISE_SCENARIO","cpf-docs/quality/qa-20260729/CPF_ENTERPRISE_REQA_SCENARIOS_20260729_05.csv","id"),
 ("QA33_REQUIREMENT","cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv","requirement_id"),
 ("QA33_SCENARIO","cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv","scenario_id"),
)

def rows(path:Path):
    with path.open(encoding="utf-8-sig",newline="") as f:return list(csv.DictReader(f))

def verify(root:Path,mapping:Path,reconciliation:Path,canonical:Path)->None:
    errors=[]; source_ids=set(); raw_counts={}
    for name,rel,key in SOURCES:
        p=root/rel
        if not p.is_file(): errors.append(f"continuity source missing: {rel}");continue
        data=rows(p);raw_counts[name]=len(data)
        for r in data:
            value=(r.get(key) or "").strip()
            if not value: errors.append(f"{rel}: blank {key}")
            else: source_ids.add(value)
    if not canonical.is_file():errors.append(f"canonical matrix missing: {canonical}");canonical_ids=set()
    else:canonical_ids={(r.get("requirement_id") or "").strip() for r in rows(canonical)}
    if len(canonical_ids)!=162:errors.append(f"canonical ID count must be 162, actual={len(canonical_ids)}")
    if not mapping.is_file():errors.append(f"continuity mapping missing: {mapping}"); mapped={}
    else:
        mapped={}
        for r in rows(mapping):
            rid=(r.get("record_id") or "").strip()
            if not rid:errors.append("mapping has blank record_id");continue
            if rid in mapped:errors.append(f"duplicate mapping record_id: {rid}")
            mapped[rid]=r
            targets=[v for v in (r.get("canonical_mapping") or "").split(";") if v]
            if not targets:errors.append(f"canonical mapping missing: {rid}")
            for target in targets:
                if target not in canonical_ids:errors.append(f"unknown canonical ID: {rid}->{target}")
            if (r.get("status") or "")!="승계":errors.append(f"invalid continuity status: {rid}")
            if "README/Manual" not in (r.get("notes") or ""):errors.append(f"README/Manual truth rule missing: {rid}")
    missing=source_ids-set(mapped);extra=set(mapped)-source_ids
    if missing:errors.append(f"source IDs missing from mapping: count={len(missing)} sample={sorted(missing)[:10]}")
    if extra:errors.append(f"mapping IDs absent from sources: count={len(extra)} sample={sorted(extra)[:10]}")
    if not reconciliation.is_file():errors.append(f"reconciliation missing: {reconciliation}")
    else:
        summary=json.loads(reconciliation.read_text(encoding="utf-8"))
        if summary.get("uniqueRecordCount")!=len(source_ids):errors.append("reconciliation uniqueRecordCount drift")
        if summary.get("sourceRawCounts")!=raw_counts:errors.append("reconciliation sourceRawCounts drift")
        if "README" not in summary.get("readmeManualPolicy",""):errors.append("reconciliation README/Manual policy missing")
    if errors:raise ValueError("\n".join(errors))

def main()->int:
    p=argparse.ArgumentParser();p.add_argument("--root",default=".");p.add_argument("--mapping",default="cpf-docs/quality/CPF_20260801_01_LEGACY_CONTINUITY_MAPPING.csv");p.add_argument("--reconciliation",default="cpf-docs/quality/CPF_20260801_01_LEGACY_CONTINUITY_RECONCILIATION.json");p.add_argument("--canonical",default="cpf-docs/quality/CPF_20260801_QA36_CANONICAL_162_REQUIREMENT_MATRIX.csv");a=p.parse_args();root=Path(a.root).resolve()
    try:verify(root,root/a.mapping,root/a.reconciliation,root/a.canonical)
    except ValueError as e:print(f"[FAIL] CPF legacy continuity\n{e}",file=sys.stderr);return 1
    print("[PASS] CPF legacy continuity sourceUnion=complete canonicalTargets=valid duplicateIds=merged readmeManualTruth=false")
    return 0
if __name__=="__main__":raise SystemExit(main())

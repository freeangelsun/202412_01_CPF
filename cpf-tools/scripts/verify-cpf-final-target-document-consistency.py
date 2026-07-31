#!/usr/bin/env python3
from __future__ import annotations
import argparse
import csv
import json
import re
import subprocess
from pathlib import Path

ALLOWED = {"완료","부분 구현","미구현","미검증","실패","재확인 필요"}

def read(root: Path, rel: str, failures: list[str]) -> str:
    p = root / rel
    if not p.is_file():
        failures.append(f"missing:{rel}")
        return ""
    return p.read_text(encoding="utf-8")

def count_csv(root: Path, rel: str, key: str, expected: int, failures: list[str]):
    p = root / rel
    if not p.is_file():
        failures.append(f"missing:{rel}")
        return []
    with p.open(encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))
    ids = [r.get(key,"").strip() for r in rows]
    if len(rows) != expected: failures.append(f"count:{rel}:{len(rows)}!={expected}")
    if len(ids) != len(set(ids)): failures.append(f"duplicate:{rel}:{key}")
    if any(not x for x in ids): failures.append(f"blank:{rel}:{key}")
    return rows

def main() -> int:
    ap=argparse.ArgumentParser();ap.add_argument("--root",default=".");a=ap.parse_args()
    root=Path(a.root).resolve();fail=[]

    final=read(root,"cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md",fail)
    continuity=read(root,"cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md",fail)
    partial=read(root,"cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md",fail)
    oss=read(root,"cpf-docs/governance/CPF_OSS_LICENSE_AND_SUPPLY_CHAIN_STANDARD.md",fail)
    adr=read(root,"cpf-docs/architecture/ADR_OSS_FIRST_PLATFORM_DIRECTION.md",fail)
    bvb=read(root,"cpf-docs/architecture/CPF_BUILD_VS_BUY_MATRIX.md",fail)
    stack=read(root,"cpf-docs/architecture/CPF_STACK_SUPPORT_AND_MIGRATION_DECISION.md",fail)
    props=read(root,"gradle/cpf-stack.properties",fail)
    current=read(root,"cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md",fail)
    state=read(root,"cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md",fail)
    docs=read(root,"cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md",fail)

    if "Canonical Requirement Count: **162개**" not in final:
        fail.append("final target canonical count missing")
    catalog_ids=set(re.findall(r"^\| `([A-Z][A-Z0-9-]+)` \|",final,re.M))
    if len(catalog_ids)!=162: fail.append(f"final target catalog ids:{len(catalog_ids)}!=162")
    if "Canonical Product Requirement | **162개**" not in continuity:
        fail.append("continuity canonical count mismatch")
    if "QA33 Remediation Requirement | **138개**" not in continuity:
        fail.append("continuity QA33 distinction missing")
    if "Legacy Alias | **8개**" not in continuity:
        fail.append("continuity alias count mismatch")
    for stale in ("Requirement 62/62","Mandatory Scenario 202/202","Defect 60/60",
                  "OSS Migration 23/23"):
        if stale in partial: fail.append(f"QA32 hardcoded completion remains:{stale}")
    if "각 최종 Release Artifact" not in oss or "ORT analyze + evaluate + report" not in oss:
        fail.append("supply-chain final artifact/ORT policy missing")
    if "APPROVED PRODUCT DIRECTION" not in adr:
        fail.append("ADR is not product-level approved")
    decision_ids=set(re.findall(r"^\| `OSS-MIG-(\d{3})` \|",bvb,re.M))
    if len(decision_ids)!=23: fail.append(f"build-vs-buy decision rows:{len(decision_ids)}!=23")
    if "QA32" in bvb.splitlines()[0]:
        fail.append("build-vs-buy title still QA32")
    expected_props={
        "javaVersion":"25","gradleVersion":"9.1.0","springBootVersion":"4.1.0",
        "springCloudVersion":"2025.1.2","springBatchVersion":"6.0.4","stackState":"TARGET"}
    for key,value in expected_props.items():
        if f"{key}={value}" not in props: fail.append(f"stack property mismatch:{key}")
        if value not in stack: fail.append(f"stack decision missing value:{key}={value}")
    if "`TARGET`과 `SUPPORTED_GA` 구분" not in stack:
        fail.append("TARGET vs GA distinction missing")
    if "QA31" in current or "9594c8d5" in current:
        fail.append("current request still QA31")
    if "QA33 Remediation Requirement: **138개**" not in current:
        fail.append("current request QA33 count missing")
    if "Canonical Product Requirement: `162`" not in state:
        fail.append("continuity state canonical count missing")
    if "`cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`" not in docs:
        fail.append("documentation standard full final target path missing")
    if "`CPF_FINAL_TARGET_REQUIREMENTS.md`" in docs:
        fail.append("documentation standard bare final target path remains")
    if "91_Gateway매뉴얼.md" in docs:
        fail.append("documentation standard wrong guide filename remains")
    if not (root/"cpf-docs/guides/91_게이트웨이매뉴얼.md").is_file():
        fail.append("actual Gateway guide missing")
    if (root/"cpf-docs/work/current/CPF_20260731_QA32_NEXT_DEVELOPMENT_REQUIREMENTS.md").exists():
        fail.append("superseded QA32 document remains in current")

    defects=count_csv(root,"cpf-docs/quality/CPF_20260731_QA33_DEFECT_REGISTER.csv","defect_id",113,fail)
    reqs=count_csv(root,"cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv","requirement_id",138,fail)
    scenarios=count_csv(root,"cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv","scenario_id",414,fail)
    inspections=count_csv(root,"cpf-docs/quality/CPF_20260731_QA33_SOURCE_INSPECTION_MATRIX.csv","inspection_id",115,fail)
    evidence=count_csv(root,"cpf-docs/quality/CPF_20260731_QA33_EVIDENCE_MATRIX.csv","evidence_id",28,fail)
    req_ids={r.get("requirement_id","") for r in reqs}
    counts={}
    for s in scenarios:
        rid=s.get("requirement_id","")
        if rid not in req_ids: fail.append(f"orphan scenario:{s.get('scenario_id')}:{rid}")
        counts[rid]=counts.get(rid,0)+1
    for rid in req_ids:
        if counts.get(rid)!=3: fail.append(f"scenario coverage:{rid}:{counts.get(rid,0)}")

    report={"status":"PASS" if not fail else "FAIL","failures":fail,
            "counts":{"canonicalRequirements":len(catalog_ids),"qa33Defects":len(defects),
                      "qa33Requirements":len(reqs),"qa33Scenarios":len(scenarios),
                      "sourceInspections":len(inspections),"evidenceRequirements":len(evidence)},
            "readmeGuidesModifiedByPackage":False}
    print(json.dumps(report,ensure_ascii=False,indent=2))
    return 0 if not fail else 1

if __name__=="__main__":
    raise SystemExit(main())

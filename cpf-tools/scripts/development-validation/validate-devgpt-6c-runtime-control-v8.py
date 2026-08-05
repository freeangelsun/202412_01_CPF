#!/usr/bin/env python3
from pathlib import Path
import csv, json, sys

BASELINE="09dd686c5ae0826594b9c5e1f871d95d95d3ce1c"
SESSION=Path("cpf-docs/work/current/development-session-results/CPF-V8-DEVGPT-6C-09dd686c/DEVGPT-6C/REV-001")
EXPECTED={"work_items":14,"requirements":338,"scenarios":950,"gates":16}

def rows(path):
    with path.open(encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))

errors=[]
if not SESSION.exists():
    errors.append(f"missing session directory: {SESSION}")
else:
    wi=rows(SESSION/"SESSION_SCOPE.csv")
    fr=rows(SESSION/"CPF_FR_SCOPE.csv")
    sc=rows(SESSION/"CPF_SC_SCOPE.csv")
    gates=rows(SESSION/"ENGINEERING_GATE_SCOPE.csv")
    rr=rows(SESSION/"DEVELOPMENT_REQUIREMENT_RESULT.csv")
    sr=rows(SESSION/"DEVELOPMENT_SCENARIO_RESULT.csv")
    checks=[
        ("work_items",len(wi)),("requirements",len(fr)),("scenarios",len(sc)),("gates",len(gates)),
        ("requirement_results",len(rr)),("scenario_results",len(sr))]
    for name,actual in checks:
        expected=EXPECTED.get(name.replace("_results",""),actual)
        if actual!=expected:
            errors.append(f"{name} expected={expected} actual={actual}")
    fr_ids={r["requirement_id"] for r in fr}
    sc_ids={r["scenario_id"] for r in sc}
    if len(fr_ids)!=len(fr): errors.append("duplicate requirement IDs")
    if len(sc_ids)!=len(sc): errors.append("duplicate scenario IDs")
    unassigned=[r["scenario_id"] for r in sc if r["linked_requirement_id"] not in fr_ids]
    if unassigned: errors.append(f"unassigned scenarios={len(unassigned)}")
    if {r["requirement_id"] for r in rr}!=fr_ids: errors.append("requirement result ID set mismatch")
    if {r["scenario_id"] for r in sr}!=sc_ids: errors.append("scenario result ID set mismatch")
    caps=rows(SESSION/"CAPABILITY_CONSUMER_COVERAGE.csv")
    if len(caps)!=54: errors.append(f"capability coverage expected=54 actual={len(caps)}")
    manifest=rows(SESSION/"SESSION_ARTIFACT_MANIFEST.csv")
    protected=("cpf-docs/deliverables/","cpf-docs/guides/","cpf-docs/environment/docker/",
               "cpf-tools/environment/docker-development-test/")
    touched=[r["path"] for r in manifest if r["path"].startswith(protected)]
    if touched: errors.append(f"protected paths touched={touched}")
    deletes=rows(SESSION/"DELETE_MANIFEST.csv")
    if deletes: errors.append(f"delete manifest not empty: {len(deletes)}")
    text="\n".join(p.read_text(encoding="utf-8-sig",errors="replace")
                    for p in SESSION.rglob("*") if p.is_file() and p.suffix in {".csv",".md",".json",".txt"})
    if "488" in text and "canonical_requirement_count_488" in text:
        errors.append("stale V6 semantic count found")
summary={
    "baseline_sha":BASELINE,
    "expected":EXPECTED,
    "errors":errors,
    "checkpoint_acceptance":{
        "capability_actual_applier":2,
        "capability_generic_only":4,
        "capability_missing_applier":48,
        "formal_java25":"UNVERIFIED",
        "db_full_lifecycle":"FAIL",
        "openapi_frontend":"FAIL",
        "crypto_payload_storage":"FAIL"
    },
    "package_integrity_pass":not errors
}
print(json.dumps(summary,ensure_ascii=False,indent=2))
sys.exit(0 if not errors else 1)

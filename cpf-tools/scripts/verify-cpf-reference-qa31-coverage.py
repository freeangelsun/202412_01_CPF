#!/usr/bin/env python3
"""Fail-closed structural/consumer coverage gate for QA31 CPF Reference EDU scope."""
from __future__ import annotations
import argparse, json, sys
from datetime import datetime, timezone
from pathlib import Path

COVERAGE = {
"QA31-EDU-001": ("cpf-reference/src/main/java/com/cpf/reference/config", "cpf-reference/src/test/java/com/cpf/reference/config"),
"QA31-EDU-002": ("cpf-reference/src/main/java/com/cpf/reference/header", "cpf-reference/src/test/java/com/cpf/reference/header"),
"QA31-EDU-003": ("cpf-reference/src/main/java/com/cpf/reference/validation", "cpf-reference/src/test/java/com/cpf/reference/validation"),
"QA31-EDU-004": ("cpf-reference/src/main/java/com/cpf/reference/servicecall", "cpf-reference/src/test/java/com/cpf/reference/servicecall"),
"QA31-EDU-005": ("cpf-reference/src/main/java/com/cpf/reference/failure", "cpf-reference/src/test/java/com/cpf/reference/failure"),
"QA31-EDU-006": ("cpf-reference/src/main/java/com/cpf/reference/servicecall", "cpf-reference/src/test/java/com/cpf/reference/servicecall"),
"QA31-EDU-007": ("cpf-reference/src/main/java/com/cpf/reference/idempotency", "cpf-reference/src/test/java/com/cpf/reference/idempotency"),
"QA31-EDU-008": ("cpf-reference/src/main/java/com/cpf/reference/crud", "cpf-reference/src/test/java/com/cpf/reference/crud"),
"QA31-EDU-009": ("cpf-reference/src/main/java/com/cpf/reference/failure", "cpf-reference/src/test/java/com/cpf/reference/failure"),
"QA31-EDU-010": ("cpf-reference/src/main/java/com/cpf/reference/failure", "cpf-reference/src/test/java/com/cpf/reference/failure"),
"QA31-EDU-011": ("cpf-reference/src/main/java/com/cpf/reference/pagination", "cpf-reference/src/test/java/com/cpf/reference/pagination"),
"QA31-EDU-012": ("cpf-reference/src/main/java/com/cpf/reference/transaction", "cpf-reference/src/test/java/com/cpf/reference/transaction"),
"QA31-EDU-013": ("cpf-reference/src/main/java/com/cpf/reference/attachment", "cpf-reference/src/test/java/com/cpf/reference/attachment"),
"QA31-EDU-014": ("cpf-reference/src/main/java/com/cpf/reference/archive", "cpf-reference/src/test/java/com/cpf/reference/archive"),
"QA31-EDU-015": ("cpf-reference/src/main/java/com/cpf/reference/messaging", "cpf-reference/src/test/java/com/cpf/reference/messaging"),
"QA31-EDU-016": ("cpf-reference/src/main/java/com/cpf/reference/messaging", "cpf-reference/src/test/java/com/cpf/reference/messaging"),
"QA31-EDU-017": ("cpf-reference/src/main/java/com/cpf/reference/telegram", "cpf-reference/src/test/java/com/cpf/reference/telegram"),
"QA31-EDU-018": ("cpf-reference/src/main/java/com/cpf/reference/external", "cpf-reference/src/test/java/com/cpf/reference/external"),
"QA31-EDU-019": ("cpf-reference/src/main/java/com/cpf/reference/security", "cpf-reference/src/test/java/com/cpf/reference/security"),
"QA31-EDU-020": ("cpf-reference/src/main/java/com/cpf/reference/audit", "cpf-reference/src/test/java/com/cpf/reference/audit"),
"QA31-EDU-021": ("cpf-reference/src/main/java/com/cpf/reference/config", "cpf-reference/src/test/java/com/cpf/reference/config"),
"QA31-EDU-022": ("cpf-reference/src/main/java/com/cpf/reference/logging", "cpf-reference/src/test/java/com/cpf/reference/logging"),
"QA31-EDU-023": ("cpf-reference/src/main/java/com/cpf/reference/external", "cpf-reference/src/test/java/com/cpf/reference/external"),
"QA31-EDU-024": ("cpf-reference/src/main/java/com/cpf/reference/batch", "cpf-reference/src/test/java/com/cpf/reference/batch"),
"QA31-EDU-025": ("cpf-reference/src/main/java/com/cpf/reference/batch", "cpf-reference/src/test/java/com/cpf/reference/batch"),
"QA31-EDU-026": ("cpf-reference/src/main/java/com/cpf/reference/centercut", "cpf-reference/src/test/java/com/cpf/reference/centercut"),
"QA31-EDU-027": ("cpf-tools/generator", "cpf-tools/verification"),
"QA31-EDU-028": ("cpf-reference/src/main/java", "cpf-reference/src/test/java"),
"QA31-EDU-029": ("cpf-tools/db/vendor/oracle", "cpf-tools/db/vendor/postgresql", "cpf-tools/db/vendor/mariadb"),
"QA31-EDU-030": ("cpf-reference/src/main/java/com/cpf/reference/failure", "cpf-reference/src/test/java/com/cpf/reference/failure"),
}

def now(): return datetime.now(timezone.utc).isoformat()
def has_product_file(path: Path) -> bool:
    if path.is_file(): return path.stat().st_size > 0
    if not path.is_dir(): return False
    return any(p.is_file() and p.stat().st_size > 0 and not p.name.upper().startswith("README") for p in path.rglob("*"))

def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument("--root",default="."); ap.add_argument("--output"); ap.add_argument("--source-sha",default="WORKTREE-OVERLAY")
    args=ap.parse_args(); root=Path(args.root).resolve(); started=now(); rows=[]
    for rid, paths in COVERAGE.items():
        details=[{"path":rel,"present":has_product_file(root/rel)} for rel in paths]
        rows.append({"requirementId":rid,"passed":all(x["present"] for x in details),"paths":details})
    # QA31 addition must be a real consumer and test, not only an SPI.
    consumer_specs={
      "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchFileProcessHandlerRegistry.java":
        ("FileProcessHandler", "require", "Duplicate FILE_PROCESS handler"),
      "cpf-batch/worker/src/main/java/com/cpf/batch/worker/SpringBatchWorkerStepHandler.java":
        ("implements BatchStepHandler", "fileHandlers.require", "handler.process", "files.claimForProcess", "moveFromProcessing"),
      "cpf-reference/src/main/java/com/cpf/reference/batch/file/csv/ReferenceCsvFileProcessHandler.java":
        ("implements FileProcessHandler", "FileProcessResult.completed"),
      "cpf-reference/src/test/java/com/cpf/reference/batch/file/csv/ReferenceCsvFileProcessHandlerTest.java":
        ("ReferenceCsvFileProcessHandler", "process"),
    }
    consumer_paths=[]
    for rel, markers in consumer_specs.items():
        path=root/rel
        text=path.read_text(encoding="utf-8",errors="replace") if path.is_file() else ""
        missing=[marker for marker in markers if marker not in text]
        consumer_paths.append({"path":rel,"present":path.is_file(),"missingMarkers":missing})
    legacy=(root/"cpf-batch/worker/src/main/java/com/cpf/batch/worker/JobPackDispatcher.java")
    consumer_paths.append({"path":legacy.relative_to(root).as_posix(),"present":legacy.is_file(),"expected":"absent"})
    consumer={"requirementId":"QA31-EDU-025-FILE-PROCESS-CONSUMER","passed":all(x["present"] and not x["missingMarkers"] for x in consumer_paths[:-1]) and not legacy.exists(),"paths":consumer_paths}
    rows.append(consumer)
    passed=all(x["passed"] for x in rows)
    report={"schemaVersion":1,"gate":"CPF_REFERENCE_QA31_COVERAGE","sourceSha":args.source_sha,"command":"verify-cpf-reference-qa31-coverage.py","startedAt":started,"finishedAt":now(),"exitCode":0 if passed else 1,"expected":"all EDU source/test/consumer paths present","actual":f"passed={sum(1 for x in rows if x['passed'])}/{len(rows)}","environment":{"runtime":"python3"},"profile":"structural-coverage","relatedIds":[x["requirementId"] for x in rows],"status":"PASS" if passed else "FAIL","rows":rows,"sensitiveDataRemoved":True}
    text=json.dumps(report,ensure_ascii=False,indent=2)+"\n"
    if args.output:
        out=Path(args.output); out.parent.mkdir(parents=True,exist_ok=True); out.write_text(text,encoding="utf-8")
    print(text,end=""); return report["exitCode"]
if __name__=="__main__": sys.exit(main())

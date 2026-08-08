#!/usr/bin/env python3
"""Actual OS process-kill/restart and multi-process lease qualification for CENTRAL-FINAL-036."""
from __future__ import annotations
import argparse, os, pathlib, shutil, subprocess, sys, tempfile, time


def compile_sources(root: pathlib.Path, classes: pathlib.Path) -> None:
    sources = [
        root / "cpf-reference/src/main/java/com/cpf/reference/batch/integrated/BatchAbcReferenceFlow.java",
        root / "cpf-reference/src/main/java/com/cpf/reference/batch/integrated/BatchAbcProcessWorker.java",
    ]
    missing = [str(p) for p in sources if not p.is_file()]
    if missing: raise RuntimeError(f"missing sources: {missing}")
    classes.mkdir(parents=True, exist_ok=True)
    cp = subprocess.run(["javac", "-encoding", "UTF-8", "-d", str(classes), *map(str, sources)], text=True, capture_output=True)
    if cp.returncode:
        raise RuntimeError(f"javac failed\n{cp.stdout}\n{cp.stderr}")


def worker(classes: pathlib.Path, state: pathlib.Path, tx: str, execution: str, attempt: int, owner: str, hold: bool):
    return subprocess.Popen([
        "java", "-cp", str(classes), "com.cpf.reference.batch.integrated.BatchAbcProcessWorker",
        str(state), tx, execution, str(attempt), owner, str(hold).lower()
    ], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)


def wait_marker(state: pathlib.Path, proc: subprocess.Popen, seconds: float = 15.0) -> None:
    marker = state / "checkpoint.marker"
    end = time.time() + seconds
    while time.time() < end:
        if marker.exists(): return
        if proc.poll() is not None:
            out = proc.stdout.read() if proc.stdout else ""
            raise RuntimeError(f"worker exited before checkpoint marker rc={proc.returncode}\n{out}")
        time.sleep(.05)
    raise RuntimeError("checkpoint marker timeout")


def load_props(path: pathlib.Path) -> dict[str,str]:
    out = {}
    for line in path.read_text(encoding="iso-8859-1").splitlines():
        if not line or line.startswith("#") or "=" not in line: continue
        k,v=line.split("=",1); out[k.strip()] = v.strip()
    return out


def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument("--root", required=True); ap.add_argument("--keep", action="store_true")
    a=ap.parse_args(); root=pathlib.Path(a.root).resolve()
    tmp=pathlib.Path(tempfile.mkdtemp(prefix="cpf-batch-kill-")); classes=tmp/"classes"; state=tmp/"state"
    try:
        compile_sources(root, classes)
        first=worker(classes,state,"TX-BATCH-KILL","EX-1",1,"NODE-A",True)
        wait_marker(state, first)
        # The first JVM owns the cross-process lease. A competing instance must fail closed.
        second=subprocess.run([
            "java","-cp",str(classes),"com.cpf.reference.batch.integrated.BatchAbcProcessWorker",
            str(state),"TX-BATCH-KILL","EX-COMPETE","2","NODE-B","false"
        ], text=True, capture_output=True, timeout=10)
        if second.returncode != 75 or "LEASE_BUSY" not in second.stdout:
            raise RuntimeError(f"multi-process lease did not fail closed rc={second.returncode}\n{second.stdout}\n{second.stderr}")
        first.kill(); first.wait(timeout=10)
        if first.returncode == 0:
            raise RuntimeError("OS kill did not terminate first worker")
        store=load_props(state/"store.properties"); remote=load_props(state/"remote.properties")
        if store.get("checkpoint") != "1" or "K1" not in store.get("committed",""):
            raise RuntimeError(f"durable checkpoint missing after kill: {store}")
        if remote.get("calls.K1") != "1" or "K1" not in remote.get("effects",""):
            raise RuntimeError(f"remote evidence missing after kill: {remote}")
        # restart uses same transactionId, new execution/attempt and resumes from checkpoint.
        restart=subprocess.run([
            "java","-cp",str(classes),"com.cpf.reference.batch.integrated.BatchAbcProcessWorker",
            str(state),"TX-BATCH-KILL","EX-2","2","NODE-C","false"
        ], text=True, capture_output=True, timeout=20)
        if restart.returncode != 0 or "state=SUCCESS" not in restart.stdout or "checkpoint=3" not in restart.stdout:
            raise RuntimeError(f"restart failed rc={restart.returncode}\n{restart.stdout}\n{restart.stderr}")
        store=load_props(state/"store.properties"); remote=load_props(state/"remote.properties")
        if store.get("checkpoint") != "3": raise RuntimeError(f"checkpoint did not finish: {store}")
        effects=set(filter(None,remote.get("effects","").split(',')))
        if effects != {"K1","K2","K3"}: raise RuntimeError(f"unexpected effects: {effects}")
        if any(remote.get(f"calls.{k}") != "1" for k in ("K1","K2","K3")):
            raise RuntimeError(f"duplicate side effect call detected: {remote}")
        # same-job re-execution must remain idempotent.
        again=subprocess.run([
            "java","-cp",str(classes),"com.cpf.reference.batch.integrated.BatchAbcProcessWorker",
            str(state),"TX-BATCH-KILL","EX-3","3","NODE-D","false"
        ], text=True, capture_output=True, timeout=20)
        if again.returncode != 0: raise RuntimeError(f"same-job replay failed: {again.stdout}\n{again.stderr}")
        remote2=load_props(state/"remote.properties")
        if any(remote2.get(f"calls.{k}") != "1" for k in ("K1","K2","K3")):
            raise RuntimeError(f"re-execution duplicated effects: {remote2}")
        print("[CPF][REFERENCE][BATCH-PROCESS-KILL][PASS] actualKill=true restart=true multiProcessLease=true duplicateEffects=0 transactionId=TX-BATCH-KILL executions=EX-1,EX-2,EX-3")
        return 0
    except Exception as e:
        print(f"[CPF][REFERENCE][BATCH-PROCESS-KILL][FAIL] {e}", file=sys.stderr); return 1
    finally:
        if not a.keep: shutil.rmtree(tmp, ignore_errors=True)

if __name__ == "__main__": raise SystemExit(main())

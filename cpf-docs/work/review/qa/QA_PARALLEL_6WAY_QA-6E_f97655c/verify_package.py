#!/usr/bin/env python3
from pathlib import Path
import argparse,csv,hashlib,json,subprocess
BASE="f97655c1299936a1101bc3ec10239265ec3b502e"; PKG_REL=Path("cpf-docs/work/review/qa/QA_PARALLEL_6WAY_QA-6E_f97655c")
def h(p):
 x=hashlib.sha256();
 with p.open("rb") as f:
  for b in iter(lambda:f.read(1048576),b""): x.update(b)
 return x.hexdigest()
def fail(m): print("FAIL:",m); raise SystemExit(1)
p=argparse.ArgumentParser(); p.add_argument("-Root","--root",default="."); p.add_argument("-ExpectedHead","--expected-head",default=""); a=p.parse_args(); root=Path(a.root).resolve(); pkg=root/PKG_REL
with (pkg/"REQUIREMENT_STATUS_PATCH_QA-6E.csv").open(encoding="utf-8-sig") as f: req=list(csv.DictReader(f))
with (pkg/"SCENARIO_STATUS_PATCH_QA-6E.csv").open(encoding="utf-8-sig") as f: sc=list(csv.DictReader(f))
if len(req)!=5093 or len(sc)!=7282: fail("count")
rids=[r["requirement_id"] for r in req]; sids=[r["scenario_id"] for r in sc]
if len(set(rids))!=5093 or len(set(sids))!=7282: fail("duplicate")
if any(r["linked_requirement_id"] not in set(rids) for r in sc): fail("orphan")
rc={"통과":0,"미통과":0,"미검증":0}
for r in req: rc[r["QA_검수결과"] or "미검증"]+=1
cc={"통과":0,"미통과":0,"미검증":0}
for r in sc: cc[r["qa_result"] or "미검증"]+=1
if rc!={"통과":1060,"미통과":2524,"미검증":1509}: fail(str(rc))
if cc!={"통과":2698,"미통과":3031,"미검증":1553}: fail(str(cc))
with (pkg/"DELETE_MANIFEST_QA-6E.csv").open(encoding="utf-8-sig") as f:
 if list(csv.DictReader(f)): fail("delete manifest")
m=json.loads((pkg/"PACKAGE_MANIFEST.json").read_text(encoding="utf-8"))
for e in m["files"]:
 q=root/e["path"]
 if not q.exists() or h(q)!=e["sha256"]: fail("manifest "+e["path"])
for line in (pkg/"FILE_HASHES.sha256").read_text().splitlines():
 if not line: continue
 d,r=line.split("  ",1); q=root/r
 if not q.exists() or h(q)!=d: fail("hash "+r)
if a.expected_head:
 try: head=subprocess.check_output(["git","-C",str(root),"rev-parse","HEAD"],text=True).strip()
 except Exception: fail("git head unavailable")
 if head!=a.expected_head: fail("HEAD mismatch "+head)
print(json.dumps({"status":"PASS","baseline_sha":BASE,"requirements":5093,"scenarios":7282,"requirement_status":rc,"scenario_status":cc,"manifest_files":len(m["files"]) },ensure_ascii=False,indent=2))

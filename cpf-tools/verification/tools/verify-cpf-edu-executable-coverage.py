#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path
FORBIDDEN=("cpf-reference/","manual-135","EDU-DEV-","EDU-BAT-","EDU-ADM-","EDU-BZA-","EDU-GW-","EDU-OPS-")
def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument("--root",default=".");a=ap.parse_args();r=Path(a.root).resolve();fail=[]
 p=r/"cpf-education/src/main/resources/education/cpf-education-canonical-35.json"
 try:d=json.loads(p.read_text(encoding="utf-8"))
 except Exception as e: print(f"[FAIL] catalog unreadable: {e}"); return 1
 rows=d.get("examples") or []
 if (d.get("onlineCount"),d.get("batchCount"),d.get("totalCount"),len(rows))!=(20,15,35,35): fail.append("catalog must contain 20 online + 15 batch = 35")
 ids=[str(x.get("id") or "") for x in rows]; pkgs=[str(x.get("package") or "") for x in rows]
 expected=[f"EDU-ONLINE-{i:02d}" for i in range(1,21)]+[f"EDU-BATCH-{i:02d}" for i in range(1,16)]
 if ids!=expected or len(ids)!=len(set(ids)): fail.append("catalog id uniqueness/format/order mismatch")
 if len(pkgs)!=len(set(pkgs)): fail.append("catalog package uniqueness mismatch")
 for row in rows:
  rid=str(row.get("id") or ""); pkg=str(row.get("package") or ""); cls=str(row.get("primaryClass") or ""); test=str(row.get("testClass") or "")
  raw=json.dumps(row,ensure_ascii=False)
  if any(x in raw for x in FORBIDDEN): fail.append(f"{rid}: retired path/id")
  if re.search(r"(?:Online|Batch)\d+|Example|Sample|Demo",cls): fail.append(f"{rid}: legacy primary class")
  pkgdir=r/"cpf-education/src/main/java"/Path(*pkg.split('.'))
  if not pkgdir.is_dir(): fail.append(f"{rid}: package missing {pkg}")
  elif not any(x.name==cls+".java" for x in pkgdir.rglob("*.java")): fail.append(f"{rid}: primary class missing {cls}")
  if not any(x.name==test+".java" for x in (r/"cpf-education/src/test/java").rglob("*.java")): fail.append(f"{rid}: test missing {test}")
 srcroot=r/"cpf-education/src/main/java/com/cpf/education"
 flat=list((srcroot/"online").glob("*.java"))+list((srcroot/"batch").glob("*.java"))
 if flat: fail.append(f"flat canonical source remains={len(flat)}")
 bad=[x for x in srcroot.rglob("*.java") if re.search(r"(?:Online|Batch)\d+|(?:Example|Sample|Demo)\.java$",x.name)]
 if bad: fail.append(f"numeric/example source remains={len(bad)}")
 internal=[x for x in srcroot.rglob("*.java") if ".internal." in x.read_text(encoding="utf-8",errors="ignore")]
 if internal: fail.append(f"internal import remains={len(internal)}")
 for e in sorted(set(fail)): print("[FAIL]",e)
 if fail:return 1
 print("[PASS] CPF EDU executable coverage features=35 online=20 batch=15 flat=0 numeric=0 internalImport=0 tests=35")
 return 0
if __name__=="__main__": raise SystemExit(main())

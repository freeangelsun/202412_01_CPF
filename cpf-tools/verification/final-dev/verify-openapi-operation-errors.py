#!/usr/bin/env python3
import argparse, copy, json, pathlib, tempfile
METHODS={"get","post","put","patch","delete","head","options","trace"}
BASE={"400","401","403","429","500","503"}
def verify(spec,prefix):
 f=[]; schemas=spec.get("components",{}).get("schemas",{})
 if "CpfApiError" not in schemas:f.append("CpfApiError schema missing")
 for path,item in spec.get("paths",{}).items():
  if not path.startswith(prefix):continue
  for method,op in item.items():
   if method not in METHODS or not isinstance(op,dict) or not op.get("operationId"):continue
   required=set(BASE);
   if "{" in path: required.add("404")
   if method in {"post","put","patch","delete"}: required.add("409")
   declared=set(op.get("x-cpf-applicable-error-statuses",[])); responses=op.get("responses",{})
   if not required.issubset(declared):f.append(f"{op['operationId']}: applicability missing {sorted(required-declared)}")
   for code in required:
    if code not in responses:f.append(f"{op['operationId']}: response {code} missing")
 return f
def main():
 ap=argparse.ArgumentParser();ap.add_argument("--root",default=".");ap.add_argument("--self-test",action="store_true");a=ap.parse_args();root=pathlib.Path(a.root)
 pairs=[(root/"cpf-admin/frontend/openapi/cpf-openapi.json","/adm/api/"),(root/"cpf-biz-admin/frontend/openapi/cpf-openapi.json","/api/bza/")]
 for p,prefix in pairs:
  spec=json.loads(p.read_text(encoding="utf-8")); f=verify(spec,prefix)
  if f: raise SystemExit("\n".join(f))
 if a.self_test:
  spec=json.loads(pairs[0][0].read_text(encoding="utf-8")); victim=next(o for item in spec["paths"].values() for m,o in item.items() if m in METHODS and isinstance(o,dict) and o.get("operationId")); victim["responses"].pop("503",None)
  if not verify(spec,"/adm/api/"):raise SystemExit("mutation was not killed")
 print("[CPF][OPENAPI][OPERATION-ERROR][PASS] modules=ADM,BZA mutationKilled="+str(a.self_test).lower())
if __name__=="__main__":main()

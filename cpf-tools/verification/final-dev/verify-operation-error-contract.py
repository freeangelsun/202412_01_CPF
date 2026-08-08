#!/usr/bin/env python3
import argparse,json
from pathlib import Path
ap=argparse.ArgumentParser();ap.add_argument("--root",default=".");ap.add_argument("--self-test",action="store_true");a=ap.parse_args();root=Path(a.root).resolve();errors=[];total=0
METHODS={"get","post","put","patch","delete"}
def validate(d,rel):
 global total
 local=[];schemas=d.get("components",{}).get("schemas",{})
 if "CpfApiError" not in schemas: local.append(rel+": CpfApiError missing")
 for path,item in d.get("paths",{}).items():
  for method,op in item.items():
   if method.lower() not in METHODS or not isinstance(op,dict):continue
   if not (path.startswith("/adm/api/") or path.startswith("/api/bza/")):continue
   total+=1;rs=op.get("responses",{});declared=set(op.get("x-cpf-applicable-error-statuses") or [])
   required={"400","401","403","429","500","503"}
   if "{" in path:required.add("404")
   if method.lower() in {"post","put","patch","delete"}:required.add("409")
   for code in sorted(required):
    if code not in rs:local.append(f"{rel}:{method}:{path}:missing:{code}")
    if code not in declared:local.append(f"{rel}:{method}:{path}:applicability-missing:{code}")
    body=rs.get(code,{}).get("content",{}).get("application/json",{}).get("schema",{})
    if body.get("$ref")!="#/components/schemas/CpfApiError":local.append(f"{rel}:{method}:{path}:error-schema:{code}")
 return local
for rel in ("cpf-admin/frontend/openapi/cpf-openapi.json","cpf-biz-admin/frontend/openapi/cpf-openapi.json"):
 d=json.loads((root/rel).read_text(encoding="utf-8"));errors.extend(validate(d,rel))
if a.self_test:
 rel="cpf-admin/frontend/openapi/cpf-openapi.json";d=json.loads((root/rel).read_text(encoding="utf-8"));victim=next(op for path,item in d["paths"].items() if path.startswith("/adm/api/") for m,op in item.items() if m in METHODS and isinstance(op,dict));victim["responses"].pop("503",None)
 if not validate(d,rel):errors.append("self-test mutation survived")
if errors:
 print("FAIL operation error contract");print("\n".join(errors[:100]));raise SystemExit(1)
print(f"PASS operation error contract operations={total} mutationKilled={str(a.self_test).lower()}")

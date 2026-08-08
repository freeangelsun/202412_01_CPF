#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, shutil, tempfile
from pathlib import Path

RETIRED={"bzaBackofficeFindApprovals","bzaBackofficeFindApproval","bzaBackofficeCreateApproval","bzaBackofficeActApproval"}
GENERATED=[
 "cpf-biz-admin/frontend/src/generated/cpf-api.ts",
 "cpf-biz-admin/frontend/src/generated/orval/cpf-api.ts",
 "cpf-biz-admin/frontend/src/generated/cpf-operation-contract.ts",
 "cpf-biz-admin/frontend/src/generated/bza-route-operation-contract.ts",
]

def verify(root:Path)->list[str]:
    errors=[]
    spec_path=root/"cpf-biz-admin/frontend/openapi/cpf-openapi.json"
    if not spec_path.is_file(): return [f"missing BZA OpenAPI spec: {spec_path}"]
    spec=json.loads(spec_path.read_text(encoding="utf-8"))
    active={op.get("operationId") for item in spec.get("paths",{}).values() for method,op in item.items() if method.lower() in {"get","post","put","patch","delete"} and isinstance(op,dict)}
    if RETIRED & active: errors.append("retired operation remains active: "+",".join(sorted(RETIRED&active)))
    controller_path=root/"cpf-biz-admin/src/main/java/com/cpf/bizadmin/backoffice/controller/BzaBackofficeController.java"
    if not controller_path.is_file(): errors.append("BZA backoffice controller missing")
    else:
        controller=controller_path.read_text(encoding="utf-8")
        if controller.count("@Hidden") < 4 or "HttpStatus.GONE" not in controller: errors.append("backend hidden+410 compatibility incomplete")
    for rel in GENERATED:
        gp=root/rel
        if not gp.is_file(): errors.append(f"generated contract missing: {rel}"); continue
        txt=gp.read_text(encoding="utf-8")
        for oid in RETIRED:
            if oid in txt: errors.append(f"{rel}: retired client symbol {oid}")
    for path,item in spec.get("paths",{}).items():
        for method,op in item.items():
            if method.lower() not in {"get","post","put","patch","delete"} or not isinstance(op,dict): continue
            rs=op.get("responses",{})
            for code in ("401","403","429","500","503"):
                if code not in rs: errors.append(f"{method} {path} missing {code}")
            if "{" in path and "404" not in rs: errors.append(f"{method} {path} missing 404")
            if method.lower() in {"post","put","patch","delete"}:
                for code in ("409","422"):
                    if code not in rs: errors.append(f"{method} {path} missing {code}")
    if "CpfApiError" not in spec.get("components",{}).get("schemas",{}): errors.append("CpfApiError schema missing")
    return errors

def self_test(root:Path)->list[str]:
    failures=[]
    with tempfile.TemporaryDirectory(prefix="cpf-bza-openapi-mutation-") as td:
        temp=Path(td)
        needed=["cpf-biz-admin/frontend/openapi/cpf-openapi.json","cpf-biz-admin/src/main/java/com/cpf/bizadmin/backoffice/controller/BzaBackofficeController.java",*GENERATED]
        for rel in needed:
            src=root/rel; dst=temp/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
        spec=json.loads((temp/needed[0]).read_text(encoding="utf-8"))
        first=next(op for item in spec["paths"].values() for m,op in item.items() if m.lower() in {"get","post","put","patch","delete"})
        first.get("responses",{}).pop("403",None)
        (temp/needed[0]).write_text(json.dumps(spec,ensure_ascii=False),encoding="utf-8")
        if not verify(temp): failures.append("missing-403 mutation survived")
    return failures

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument("--root",type=Path,default=Path(".")); ap.add_argument("--self-test",action="store_true"); a=ap.parse_args()
    root=a.root.resolve(); errors=verify(root)
    if not errors and a.self_test: errors.extend(self_test(root))
    if errors:
        print("[CPF][BZA-OPENAPI][FAIL] "+"; ".join(errors[:30])); return 1
    spec=json.loads((root/"cpf-biz-admin/frontend/openapi/cpf-openapi.json").read_text(encoding="utf-8"))
    active={op.get("operationId") for item in spec.get("paths",{}).values() for m,op in item.items() if m.lower() in {"get","post","put","patch","delete"} and isinstance(op,dict)}
    print(f"[CPF][BZA-OPENAPI][PASS] activeOperations={len(active)} retired=4 standardErrors=bound generatedClient=clean selfTest={str(a.self_test).lower()}")
    return 0

if __name__=="__main__": raise SystemExit(main())

#!/usr/bin/env python3
"""CPF ADM/Gateway management boundary + Backoffice(MBW) business-domain verifier."""
from __future__ import annotations
import argparse, json, re
from pathlib import Path

MGMT={"ADM":"cpf-admin/src/main/java","GATEWAY":"cpf-gateway/src/main/java"}
BACKOFFICE="cpf-backoffice/online/src/main/java"
ENDPOINT=re.compile(r"@(Get|Post|Put|Delete|Patch|Request)Mapping(?:\s*\(|\s*$)")
TX=re.compile(r"@CpfOnlineTransaction\s*\(.*?operationId\s*=\s*\"([^\"]+)\"",re.S)
OP=re.compile(r"@Operation\s*\(.*?operationId\s*=\s*\"([^\"]+)\"",re.S)

def rows(root:Path, rel:str):
    p=root/rel
    return [] if not p.exists() else [(f,f.read_text(encoding='utf-8',errors='ignore')) for f in p.rglob('*.java')]

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--json-out'); a=ap.parse_args(); root=Path(a.root).resolve()
    failures=[]; metrics={}
    for label,rel in MGMT.items():
        js=rows(root,rel); ctrls=[(p,t) for p,t in js if p.stem.endswith('Controller')]
        if not js: failures.append(f'{label} source missing'); continue
        tx=[str(p.relative_to(root)) for p,t in ctrls if re.search(r'(?m)^\s*@CpfOnlineTransaction(?:\s*\(|\s*$)',t)]
        raw_headers=[str(p.relative_to(root)) for p,t in ctrls if 'X-Transaction-Id' in t and 'X-Target-Operation-Id' in t]
        internal=[str(p.relative_to(root)) for p,t in js if re.search(r'import\s+com\.cpf\.core\.(?:internal|impl)\.',t)]
        metrics[label]={'controllerCount':len(ctrls),'businessTransactionAnnotationOnManagementController':len(tx),'rawCanonicalHeaderAssembly':len(raw_headers),'directCpfCoreInternalImports':len(internal)}
        if tx: failures.append(f'{label} management controller must not be @CpfOnlineTransaction: {tx[:10]}')
        if raw_headers: failures.append(f'{label} management controller must not assemble business canonical headers: {raw_headers[:10]}')
        if internal: failures.append(f'{label} direct cpf-core internal coupling: {internal[:10]}')

    js=rows(root,BACKOFFICE); ctrls=[(p,t) for p,t in js if p.stem.endswith('Controller')]
    if not js: failures.append('BACKOFFICE source missing')
    endpoint_count=0; tx_count=0; mismatches=[]; non_mbw=[]; internal=[]; cross=[]
    for p,t in ctrls:
        endpoint_count += len(ENDPOINT.findall(t))
        txs=TX.findall(t); ops=OP.findall(t); tx_count += len(txs)
        # Every business transaction annotation must share the canonical OpenAPI operation id and MBW namespace.
        for opid in txs:
            if not opid.startswith('MBW_'): non_mbw.append(f'{p.relative_to(root)}:{opid}')
        if txs and ops:
            # Compare operation ids present in the class, not annotation order assumptions.
            missing=[x for x in txs if x not in ops]
            if missing: mismatches.append(f'{p.relative_to(root)}:{missing}')
        elif txs and not ops:
            mismatches.append(f'{p.relative_to(root)}:missing @Operation')
    for p,t in js:
        if re.search(r'import\s+com\.cpf\.core\.(?:internal|impl)\.',t): internal.append(str(p.relative_to(root)))
        if re.search(r'import\s+com\.cpf\.(?:member|external)\..*\.(?:internal|repository)\.',t): cross.append(str(p.relative_to(root)))
    metrics['BACKOFFICE']={'controllerCount':len(ctrls),'mappedEndpointCount':endpoint_count,'cpfOnlineTransactionCount':tx_count,'operationMismatchCount':len(mismatches),'nonMbwOperationCount':len(non_mbw),'directCpfCoreInternalImports':len(internal),'businessDomainImplementationImports':len(cross)}
    if endpoint_count and tx_count == 0: failures.append('BACKOFFICE business endpoints are not using @CpfOnlineTransaction')
    if mismatches: failures.append(f'BACKOFFICE @CpfOnlineTransaction/OpenAPI operation mismatch: {mismatches[:12]}')
    if non_mbw: failures.append(f'BACKOFFICE operationId must use MBW namespace: {non_mbw[:12]}')
    if internal: failures.append(f'BACKOFFICE direct cpf-core internal coupling: {internal[:12]}')
    if cross: failures.append(f'BACKOFFICE direct other-domain implementation/repository coupling: {cross[:12]}')

    result={'status':'PASS' if not failures else 'FAIL','failures':failures,'metrics':metrics,
            'boundary':'ADM/Gateway are platform management APIs. cpf-backoffice is an optional prebuilt Business Domain and follows the same Canonical transaction/operation/public-starter contract as generated domains.'}
    print('CPF_NXT3_ADM_BACKOFFICE_FRAMEWORK_GATE='+result['status']); print(json.dumps(result,ensure_ascii=False,indent=2))
    if a.json_out: Path(a.json_out).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    raise SystemExit(0 if not failures else 1)
if __name__=='__main__': main()

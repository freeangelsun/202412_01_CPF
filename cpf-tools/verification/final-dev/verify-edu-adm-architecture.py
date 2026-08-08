#!/usr/bin/env python3
from __future__ import annotations
import argparse, re, sys, tempfile, shutil
from pathlib import Path

NON_EXEC={1,5,6,8,9,10,11,12,13,14,15,16,17}
EXEC={2,3,4,7}
PACKAGES={
1:'reuse',2:'query',3:'command',4:'approval',5:'asyncoperation',6:'partialrecovery',7:'customscreen',8:'search',9:'detail',10:'bulk',11:'configuration',12:'incident',13:'evidence',14:'topology',15:'correlation',16:'notification',17:'session'}

def source(root:Path,n:int)->Path:
    return root/f'cpf-reference/src/main/java/com/cpf/reference/optional/operations/{PACKAGES[n]}/EduAdm{n:02d}Handler.java'

def verify(root:Path)->list[str]:
    errors=[]
    for n in sorted(NON_EXEC):
        p=source(root,n)
        if not p.exists(): errors.append(f'missing non-exec metadata {p}'); continue
        t=p.read_text(encoding='utf-8')
        for bad in ('extends AbstractEduCapabilityHandler','consumerBinding()','buildBusinessResult(','EduConsumerBinding('):
            if bad in t: errors.append(f'EDU-ADM-{n:02d} remains executable via {bad}')
        for required in ('EduAdmRedirectMetadata','CPF_ADM_OPERATOR','false'):
            if required not in t: errors.append(f'EDU-ADM-{n:02d} redirect missing {required}')
    for n in sorted(EXEC):
        p=source(root,n)
        if not p.exists(): errors.append(f'missing retained handler {p}'); continue
        t=p.read_text(encoding='utf-8')
        if 'extends AbstractEduCapabilityHandler' not in t: errors.append(f'EDU-ADM-{n:02d} is not executable')
        if 'CPF_ADM_OPERATOR' not in t: errors.append(f'EDU-ADM-{n:02d} role mismatch')
        if 'CPF_REFERENCE_PLATFORM_OPERATOR' in t: errors.append(f'EDU-ADM-{n:02d} stale role remains')
    c=root/'cpf-reference/src/main/java/com/cpf/reference/optional/operations/config/ReferenceOperationsCapabilityContributor.java'
    if not c.exists(): errors.append('missing ReferenceOperationsCapabilityContributor')
    else:
        t=c.read_text(encoding='utf-8')
        found={int(x) for x in re.findall(r'new EduAdm(\d\d)Handler\(\)',t)}
        if found != EXEC: errors.append(f'contributor registrations expected={sorted(EXEC)} actual={sorted(found)}')
    return errors

def self_test(root:Path)->list[str]:
    errs=[]
    with tempfile.TemporaryDirectory() as td:
        m=Path(td)/'repo'; shutil.copytree(root,m)
        p=source(m,1); t=p.read_text(encoding='utf-8'); p.write_text(t.replace('public final class EduAdm01Handler {','public final class EduAdm01Handler extends AbstractEduCapabilityHandler {'),encoding='utf-8')
        if not verify(m): errs.append('mutation executable inheritance was not detected')
    with tempfile.TemporaryDirectory() as td:
        m=Path(td)/'repo'; shutil.copytree(root,m)
        p=source(m,2); t=p.read_text(encoding='utf-8'); p.write_text(t.replace('CPF_ADM_OPERATOR','CPF_REFERENCE_PLATFORM_OPERATOR'),encoding='utf-8')
        if not verify(m): errs.append('mutation stale retained role was not detected')
    return errs

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,required=True); ap.add_argument('--self-test',action='store_true'); a=ap.parse_args()
    errors=verify(a.root)
    if a.self_test: errors+=self_test(a.root)
    if errors:
        for e in errors: print('FAIL:',e)
        return 1
    print('PASS: EDU ADM architecture — 13 non-executable redirects, retained 02/03/04/07 CPF_ADM_OPERATOR, exact contributor registration')
    return 0
if __name__=='__main__': sys.exit(main())

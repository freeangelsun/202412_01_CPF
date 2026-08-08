#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, shutil, tempfile
from pathlib import Path

MANIFEST = Path('cpf-tools/db/metadata/bza-permission-manifest.json')
PAGE = Path('cpf-biz-admin/frontend/src/features/approval-simulation/ApprovalSimulationPage.vue')
FILTER_TEST = Path('cpf-biz-admin/src/test/java/com/cpf/bizadmin/auth/filter/BzaApiAuthFilterTest.java')
ORVAL = Path('cpf-biz-admin/frontend/src/generated/orval/cpf-api.ts')


def verify(root: Path) -> list[str]:
    failures: list[str] = []
    try:
        manifest = json.loads((root / MANIFEST).read_text(encoding='utf-8'))
    except Exception as exc:
        return [f'manifest unreadable: {exc}']
    rules = manifest.get('actionRules') or []
    dedicated = [r for r in rules if str(r.get('method','')).upper() == 'POST' and r.get('pathPattern') == 'approvals/simulate']
    if len(dedicated) != 1 or dedicated[0].get('actionCode') != 'SIMULATE':
        failures.append('POST approvals/simulate must resolve to dedicated SIMULATE action')
    generic = next((i for i,r in enumerate(rules) if str(r.get('method','')).upper()=='POST' and r.get('pathPattern')=='*/**'), None)
    dedicated_i = next((i for i,r in enumerate(rules) if str(r.get('method','')).upper()=='POST' and r.get('pathPattern')=='approvals/simulate'), None)
    if generic is None or dedicated_i is None or dedicated_i >= generic:
        failures.append('dedicated SIMULATE rule must precede generic POST wildcard')

    page = (root / PAGE).read_text(encoding='utf-8') if (root / PAGE).exists() else ''
    if 'bzaApprovalPolicySimulate' not in page or '../../generated/orval/cpf-api' not in page:
        failures.append('ApprovalSimulationPage must consume generated Orval bzaApprovalPolicySimulate')
    if 'bzaApi(' in page:
        failures.append('ApprovalSimulationPage must not use raw bzaApi for simulation')
    if 'hasBzaPermission("APPROVAL", "SIMULATE")' not in page:
        failures.append('ApprovalSimulationPage must explicitly gate APPROVAL:SIMULATE')

    orval = (root / ORVAL).read_text(encoding='utf-8') if (root / ORVAL).exists() else ''
    if 'export const bzaApprovalPolicySimulate' not in orval:
        failures.append('generated Orval client is missing bzaApprovalPolicySimulate')

    test = (root / FILTER_TEST).read_text(encoding='utf-8') if (root / FILTER_TEST).exists() else ''
    required = [
        'approvalSimulationRequiresDedicatedSimulatePermission',
        'approvalSimulationDirectCallIsDeniedWithoutSimulatePermission',
        'authorize("Bearer token", "APPROVAL", "SIMULATE")',
        'assertThat(response.getStatus()).isEqualTo(403)',
    ]
    for token in required:
        if token not in test:
            failures.append(f'backend direct-call role-matrix negative coverage missing: {token}')
    return failures


def self_test(root: Path) -> list[str]:
    cases = []
    with tempfile.TemporaryDirectory(prefix='cpf-bza-sim-mutation-') as td:
        temp = Path(td)
        for rel in (MANIFEST, PAGE, FILTER_TEST, ORVAL):
            (temp / rel).parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(root / rel, temp / rel)
        data=json.loads((temp/MANIFEST).read_text(encoding='utf-8'))
        data['actionRules']=[r for r in data['actionRules'] if r.get('pathPattern')!='approvals/simulate']
        (temp/MANIFEST).write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
        if not verify(temp): cases.append('manifest-rule-removal mutation survived')
    with tempfile.TemporaryDirectory(prefix='cpf-bza-sim-mutation-') as td:
        temp=Path(td)
        for rel in (MANIFEST, PAGE, FILTER_TEST, ORVAL):
            (temp/rel).parent.mkdir(parents=True,exist_ok=True); shutil.copy2(root/rel,temp/rel)
        p=(temp/PAGE).read_text(encoding='utf-8').replace('bzaApprovalPolicySimulate({','bzaApi("/api/bza/approvals/simulate", {')
        (temp/PAGE).write_text(p,encoding='utf-8')
        if not verify(temp): cases.append('raw-client mutation survived')
    with tempfile.TemporaryDirectory(prefix='cpf-bza-sim-mutation-') as td:
        temp=Path(td)
        for rel in (MANIFEST, PAGE, FILTER_TEST, ORVAL):
            (temp/rel).parent.mkdir(parents=True,exist_ok=True); shutil.copy2(root/rel,temp/rel)
        t=(temp/FILTER_TEST).read_text(encoding='utf-8').replace('approvalSimulationDirectCallIsDeniedWithoutSimulatePermission','approvalSimulationDirectCallNegativeRemoved')
        (temp/FILTER_TEST).write_text(t,encoding='utf-8')
        if not verify(temp): cases.append('backend-negative-test mutation survived')
    return cases


def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,default=Path('.')); ap.add_argument('--self-test',action='store_true'); args=ap.parse_args()
    failures=verify(args.root)
    if not failures and args.self_test: failures.extend(self_test(args.root))
    if failures:
        for f in failures: print(f'[CPF][BZA-SIM][FAIL] {f}')
        return 1
    print('[CPF][BZA-SIM][PASS] permission=APPROVAL:SIMULATE generatedClient=orval directCall=403 selfTest=' + ('true' if args.self_test else 'not-run'))
    return 0
if __name__=='__main__': raise SystemExit(main())

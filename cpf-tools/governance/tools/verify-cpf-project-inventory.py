#!/usr/bin/env python3
"""Fail-closed validation for the generated CPF project inventory."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse, csv, datetime as dt, json
from pathlib import Path


def rows(path: Path):
    with path.open(encoding="utf-8-sig", newline="") as f: return list(csv.DictReader(f))


def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('--inventory-dir',type=Path,required=True); ap.add_argument('--policy',type=Path,required=True); ap.add_argument('--waivers',type=Path,required=True); ap.add_argument('--release',action='store_true'); a=ap.parse_args()
    d=a.inventory_dir; policy=json.loads(a.policy.read_text(encoding='utf-8')); today=dt.date.today(); errors=[]; warnings=[]
    required=['cpf-module-file-inventory.csv','cpf-public-contract-inventory.csv','cpf-configuration-inventory.csv','cpf-database-inventory.csv','cpf-frontend-inventory.csv','cpf-requirement-reverse-trace.csv','cpf-project-inventory-summary.json']
    for n in required:
        if not (d/n).is_file(): errors.append(f'missing inventory artifact: {n}')
    if errors: print('\n'.join('[FAIL] '+e for e in errors)); return 1
    waiver_rows=rows(a.waivers); active=set()
    for w in waiver_rows:
        try: expiry=dt.date.fromisoformat(w['expires_on'])
        except Exception: errors.append(f"invalid waiver expiry: {w.get('waiver_id','')}"); continue
        if expiry < today: errors.append(f"expired waiver: {w.get('waiver_id','')}")
        elif not w.get('owner') or not w.get('reason') or not w.get('approved_by'): errors.append(f"incomplete waiver: {w.get('waiver_id','')}")
        else: active.add((w.get('category',''),w.get('path_or_symbol','')))
    file_rows=rows(d/'cpf-module-file-inventory.csv')
    for r in file_rows:
        if r['owner']=='UNOWNED' and ('unowned',r['path']) not in active: errors.append(f"unowned tracked file: {r['path']}")
        low=r['path'].lower()
        if r['kind']=='database':
            for token in policy['unsupportedDatabaseTokens']:
                if token in low and ('unsupported-db',r['path']) not in active: errors.append(f"unsupported DB token in official product path: {r['path']}")
    for r in rows(d/'cpf-public-contract-inventory.csv'):
        if r['direct_internal_imports'] and ('internal-import',r['path']) not in active: errors.append(f"public contract imports internal package: {r['path']} -> {r['direct_internal_imports']}")
        if r['contract_type']=='controller' and r.get('controller_transport','http')=='http' and not r['http_mappings']: warnings.append(f"HTTP controller without captured HTTP mapping: {r['path']}")
    for r in rows(d/'cpf-requirement-reverse-trace.csv'):
        if r['development_status']=='완료' and r['role'] in {'source','consumer','test'} and r['path_exists']!='true': errors.append(f"completed requirement has missing {r['role']} path: {r['requirement_id']} {r['path']}")
        if r['verification_status']=='완료' and r['role']=='evidence' and r['path_exists']!='true': errors.append(f"verified requirement has missing evidence path: {r['requirement_id']} {r['path']}")
    summary=json.loads((d/'cpf-project-inventory-summary.json').read_text(encoding='utf-8'))
    if summary['fileCount']<=0: errors.append('empty project inventory')
    if a.release and warnings: errors.extend('release warning: '+w for w in warnings)
    for w in warnings: print('[WARN]',w)
    for e in errors: print('[FAIL]',e)
    if errors: return 1
    print(f"[PASS] CPF project inventory files={summary['fileCount']} public={summary['publicContractCount']} db={summary['databaseFileCount']} frontend={summary['frontendFileCount']} reverseTrace={summary['reverseTraceRows']}")
    return 0
if __name__=='__main__': raise SystemExit(main())

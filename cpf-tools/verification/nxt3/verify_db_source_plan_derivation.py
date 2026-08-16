#!/usr/bin/env python3
"""
Legacy vendor/source plan이 Canonical DB에서 파생된 호환 입력인지 fail-closed로 검증한다.

DB 설치·검증 도구가 별도 Vendor Source를 독립 정본으로 사용하지 않도록 Canonical Authority와
3개 Vendor 파생 Source의 의미 정합성을 확인한다.
"""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
VENDORS=('mariadb','postgresql','oracle')
def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ns=ap.parse_args(); root=Path(ns.root).resolve(); errors=[]
    plan=json.loads((root/'cpf-tools/db/config/database-source-plan.json').read_text(encoding='utf-8'))
    policy=plan.get('policy',{})
    if policy.get('canonicalAuthority')!='cpf-tools/db/canonical/**': errors.append('CANONICAL_AUTHORITY_NOT_DECLARED')
    if policy.get('currentSnapshotAuthority')!='cpf-tools/db/generated/current/{vendor}': errors.append('CURRENT_SNAPSHOT_AUTHORITY_DRIFT')
    if policy.get('vendorSourceRole')!='DERIVED_COMPATIBILITY_INPUT': errors.append('VENDOR_SOURCE_ROLE_NOT_DERIVED')
    if policy.get('directVendorSourceEditing')!='FORBIDDEN': errors.append('DIRECT_VENDOR_SOURCE_EDIT_NOT_FORBIDDEN')
    canonical=json.loads((root/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
    expected={str(t.get('targetTableName') or t.get('name')).upper() for t in canonical.get('tables',[]) if t.get('productionDefault',True) and t.get('targetPhysicalDatabase')!='referenceFixture'}
    for vendor in VENDORS:
        vp=plan.get(vendor,{})
        if vp.get('sourceRole')!='DERIVED_COMPATIBILITY_INPUT': errors.append(f'SOURCE_ROLE_DRIFT:{vendor}')
        src=root/str(vp.get('sourceRoot',''))
        if not src.is_dir(): errors.append(f'SOURCE_ROOT_MISSING:{vendor}'); continue
        names=set()
        for fn in vp.get('emptyInstallFiles',[]):
            p=src/fn
            if not p.is_file(): errors.append(f'EMPTY_INSTALL_FILE_MISSING:{vendor}:{fn}'); continue
            txt=p.read_text(encoding='utf-8',errors='ignore')
            names.update(m.group(1).upper() for m in re.finditer(r'(?im)CREATE\s+TABLE(?:\s+IF\s+NOT\s+EXISTS)?\s+[`"]?([A-Z0-9_]+)',txt))
        missing=sorted(expected-names)
        for name in missing: errors.append(f'CANONICAL_TABLE_MISSING_FROM_DERIVED_SOURCE:{vendor}:{name}')
        pack=json.loads((root/f'cpf-tools/db/vendor/{vendor}/pack.json').read_text(encoding='utf-8'))
        raw=json.dumps(pack,ensure_ascii=False)
        if 'generated/current' not in raw and 'CANONICAL_JSON_GENERATED_VENDOR_PACK' not in raw:
            errors.append(f'PACK_CURRENT_AUTHORITY_MISSING:{vendor}')
    status='PASS' if not errors else 'FAIL'
    print(f'CPF_DB_SOURCE_PLAN_DERIVATION={status} canonicalTables={len(expected)} vendors={len(VENDORS)} errors={len(errors)}')
    for e in errors[:200]: print(e)
    return 0 if not errors else 1
if __name__=='__main__': raise SystemExit(main())

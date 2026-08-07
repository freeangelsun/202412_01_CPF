#!/usr/bin/env python3
from pathlib import Path
import hashlib, json, re, sys
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
vendors=('mariadb','postgresql','oracle')
roles=('source/19_approval_integrity_r6.sql','install/08_approval_integrity_r6.sql','migration/V104__approval_integrity_r6.sql','rollback/R104__approval_integrity_r6.sql','verify/104_verify_approval_integrity_r6.sql','runtime/adm/approval_integrity_queries.sql')
errors=[]; normalized={}
for vendor in vendors:
    root=R/'cpf-tools/db/vendor'/vendor
    pack=root/'pack.json'
    try: data=json.loads(pack.read_text(encoding='utf-8'))
    except Exception as e: errors.append(f'{vendor}: invalid pack.json: {e}'); continue
    entry=data.get('approvalIntegrityR6Pack')
    if not isinstance(entry,dict) or entry.get('officialVendor') is not True: errors.append(f'{vendor}: approvalIntegrityR6Pack missing/invalid')
    paths=[entry.get('canonicalSource'),entry.get('freshInstall'),entry.get('migration'),entry.get('rollback'),entry.get('verify'),entry.get('runtimeQueries')]
    if sorted(paths)!=sorted(roles): errors.append(f'{vendor}: pack paths differ expected={roles} actual={paths}')
    normalized[vendor]={}
    for role in roles:
        p=root/role
        if not p.is_file(): errors.append(f'{vendor}: missing {role}'); continue
        raw=p.read_text(encoding='utf-8',errors='replace')
        if not raw.strip(): errors.append(f'{vendor}: empty {role}')
        upper=raw.upper()
        normalized[vendor][role]=set(re.findall(r'\b(?:ADM_APPROVAL_POLICY_HISTORY|POLICY_CODE|POLICY_VERSION|CHANGE_REASON|BEFORE_HASH|AFTER_HASH|OPERATOR_ID|CREATED_AT)\b',upper))
        if role.startswith(('source/','install/','migration/')):
            for token in ('ADM_APPROVAL_POLICY_HISTORY','POLICY_CODE','POLICY_VERSION','CHANGE_REASON','BEFORE_HASH','AFTER_HASH','OPERATOR_ID'):
                if token not in upper: errors.append(f'{vendor}: {role} missing {token}')
        if role.startswith('rollback/') and 'DROP' not in upper: errors.append(f'{vendor}: rollback has no DROP')
        if role.startswith('verify/') and not any(x in upper for x in ('RAISE','SIGNAL','THROW')): errors.append(f'{vendor}: verify has no assertion signal')
for role in roles:
    sets=[normalized.get(v,{}).get(role,set()) for v in vendors]
    if all(sets) and any(s!=sets[0] for s in sets[1:]): errors.append(f'DB3 semantic token parity mismatch: {role}: {sets}')
for e in errors: print('FAIL',e)
if errors: raise SystemExit(1)
print(f'PASS DB3 approval integrity lifecycle vendors={len(vendors)} roles={len(roles)}')

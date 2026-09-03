#!/usr/bin/env python3
"""Verify Current Development Harness work-item progress from the single Current Registry."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,csv,json,re
from collections import Counter
from pathlib import Path

REQUIRED=("work_item_id","developer_status","verification_status","overall_status","source_identity","item_role")
DEV_STATES={"완료","미완료","부분 구현","미구현","실패","재확인 필요","해당 없음","SOURCE_FIXED","VERIFICATION_PENDING","BLOCKED_EXTERNAL","NOT_EXECUTED","UNKNOWN"}
VERIFY_STATES={"완료","미완료","미검증","실패","재확인 필요","해당 없음","VERIFICATION_PENDING","BLOCKED_EXTERNAL","NOT_EXECUTED","UNKNOWN"}
OVERALL_STATES={"완료","부분 구현","미구현","미검증","실패","재확인 필요","해당 없음","VERIFICATION_PENDING","BLOCKED_EXTERNAL","NOT_EXECUTED","UNKNOWN"}

def load(path:Path):
    if not path.is_file(): raise ValueError(f"registry missing: {path}")
    with path.open(encoding='utf-8-sig',newline='') as f:
        reader=csv.DictReader(f); rows=list(reader); fields=tuple(reader.fieldnames or ())
    missing=[c for c in REQUIRED if c not in fields]
    if missing: raise ValueError("unsupported Current Registry schema; missing="+",".join(missing))
    if not rows: raise ValueError("empty Current Registry")
    ids=[(r.get('work_item_id') or '').strip() for r in rows]
    if any(not x for x in ids): raise ValueError('blank work_item_id')
    dup=sorted(k for k,v in Counter(ids).items() if v>1)
    if dup: raise ValueError('duplicate work_item_id='+','.join(dup))
    failures=[]
    for r in rows:
        wid=r['work_item_id']
        for col,allowed in (("developer_status",DEV_STATES),("verification_status",VERIFY_STATES),("overall_status",OVERALL_STATES)):
            value=(r.get(col) or '').strip()
            if value not in allowed: failures.append(f"{wid}:{col}={value!r}")
        sid=(r.get('source_identity') or '').strip().lower()
        if not re.fullmatch(r'[0-9a-f]{64}',sid): failures.append(f"{wid}:invalid_source_identity={sid!r}")
        if (r.get('overall_status') or '').strip()=='완료' and (r.get('verification_status') or '').strip()!='완료':
            failures.append(f"{wid}:overall_complete_without_verification_complete")
    if failures: raise ValueError('; '.join(failures[:50]))
    return rows

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--ledger'); ap.add_argument('--csv'); ap.add_argument('--expected-canonical',type=int); ap.add_argument('--json-output'); ns=ap.parse_args()
    root=Path(ns.root).resolve(); raw=ns.ledger or ns.csv or 'cpf-docs/governance/development-harness/current/CURRENT_WORK_ITEM_REGISTRY.csv'; path=Path(raw); path=path if path.is_absolute() else root/path
    try: rows=load(path)
    except Exception as e:
        print('REQUIREMENT_PROGRESS_GATE=FAIL'); print('REQUIREMENT_PROGRESS_ERROR='+str(e)); return 1
    expected=ns.expected_canonical
    if expected is None:
        execution=sum(1 for r in rows if (r.get('item_role') or '').strip()=='ROOT_CAUSE_EXECUTION')
        tracking=sum(1 for r in rows if (r.get('item_role') or '').strip()!='ROOT_CAUSE_EXECUTION')
        # WP-R16.01/02(ADM mandatory Admin Route Provider Composition, Canonical Config Owner)
        # 등록으로 Root Cause Execution 이 17 -> 19 가 되었고, 2026-09-03 사용자 Steering 3건
        # (WP-R17.01 Shell 조립성 / WP-R17.02 운영자 선택 마스킹 / WP-R17.03 운영자 구성 로그 항목)
        # 등록으로 19 -> 22 가 되었다.
        if (tracking,execution)!=(394,23):
            print(f'REQUIREMENT_PROGRESS_GATE=FAIL\nREQUIREMENT_PROGRESS_ERROR=current_registry_shape={tracking}+{execution} expected=394+23'); return 1
        expected=417
    if len(rows)!=expected:
        print(f'REQUIREMENT_PROGRESS_GATE=FAIL\nREQUIREMENT_PROGRESS_ERROR=current_registry_count={len(rows)} expected={expected}'); return 1
    result={'schema':'CPF_CURRENT_WORK_ITEM_REGISTRY_V1','rows':len(rows),'overall':dict(Counter((r.get('overall_status') or '').strip() for r in rows)),'development':dict(Counter((r.get('developer_status') or '').strip() for r in rows)),'verification':dict(Counter((r.get('verification_status') or '').strip() for r in rows))}
    print(json.dumps(result,ensure_ascii=False,sort_keys=True))
    if ns.json_output:
        out=Path(ns.json_output); out=out if out.is_absolute() else root/out; out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('REQUIREMENT_PROGRESS_GATE=PASS'); return 0
if __name__=='__main__': raise SystemExit(main())
